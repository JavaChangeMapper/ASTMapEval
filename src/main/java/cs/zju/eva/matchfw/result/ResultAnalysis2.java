package cs.zju.eva.matchfw.result;


import cs.zju.eva.matchfw.RunningRecord;
import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.io.IOException;
import java.util.*;

public class ResultAnalysis2 {
    private static final String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static String[] algorithms = {"gt", "mtdiff", "ijm"};

    public static void main(String[] args) throws IOException {
        int fileRevisionNum = 0;
        int commitNum = 0;
        for (String project: projects){
            Map<FileRevision, Set<StmtObj>> revisionStmtMap = new HashMap<>();
            Map<FileRevision, List<StmtMapEvalRecord>> revisionRecordMap = new HashMap<>();
            List<RunningRecord> fileRevisionRecords = AnalysisUtils.getAllFileRevisionRecords(project);
            List<FileRevision> revisions = AnalysisUtils.getAllFileRevisions(fileRevisionRecords);
            fileRevisionNum += fileRevisionRecords.size();
            commitNum += AnalysisUtils.getCommitNumber(fileRevisionRecords);
            System.out.println(project);
            System.out.println("revision num: " + fileRevisionRecords.size());
            System.out.println("commit num: " + AnalysisUtils.getCommitNumber(fileRevisionRecords));

            StmtMatchGroup.init();
            for (int i = 0; i < algorithms.length - 1; i++) {
                String algorithm1 = algorithms[i];
                for (int j = i + 1; j < algorithms.length; j++){
                    String algorithm2 = algorithms[j];
                    if (algorithm1.equals(algorithm2))
                        continue;
                    System.out.println(algorithm1 + " <=> " + algorithm2);
                    Map<FileRevision, List<ComparisonRecord>> fileRecordsMap = new HashMap<>();
                    AnalysisUtils.calAllRecords(project, algorithm1, algorithm2, fileRecordsMap);
                    calFileRevisionStatistics(fileRecordsMap, algorithm1.toUpperCase());
                    calFileRevisionStatistics(fileRecordsMap, algorithm2.toUpperCase());
                    setRevisionStmtMap(project, fileRecordsMap, revisionStmtMap);
                }
            }

            setRevisionStmtRecordMap(revisionStmtMap, revisionRecordMap);

            calAllAlgorithmFileRevisionStatistics(revisionRecordMap, fileRevisionRecords.size());

            List<StmtMapEvalRecord> allStmtRecords = new ArrayList<>();
            for (FileRevision fr: revisions){
                List<StmtMapEvalRecord> records = revisionRecordMap.get(fr);
                if (records == null)
                    continue;
                allStmtRecords.addAll(records);
            }

            List<String[]> csvRecords = new ArrayList<>();
            for (StmtMapEvalRecord record: allStmtRecords){
                if (record != null)
                    csvRecords.add(record.toCsvRecords());
            }

            System.out.println();

            String csvPath = PathResolver.getStmtMatchResultCsvPath(project);
            CsvOperationsUtil.writeCSV(csvPath, StmtMapEvalRecord.getHeaders(), csvRecords);
        }

        System.out.println(fileRevisionNum);
        System.out.println(commitNum);
    }

    private static void calAllAlgorithmFileRevisionStatistics(Map<FileRevision, List<StmtMapEvalRecord>> revisionStmtMap,
                                                              double numberOfFileRevision){
        int gtInaccurateStmtNum = 0;
        Set<FileRevision> gtInaccurateFrSet = new HashSet<>();
        int mtdInaccurateStmtNum = 0;
        Set<FileRevision> mtdInaccurateFrSet = new HashSet<>();
        int ijmInaccurateStmtNum = 0;
        Set<FileRevision> ijmInaccurateFrSet = new HashSet<>();

        for (FileRevision fr: revisionStmtMap.keySet()){
            List<StmtMapEvalRecord> records = revisionStmtMap.get(fr);
            for (StmtMapEvalRecord record: records){
                if (record.getGtInaccurate() == 1) {
                    gtInaccurateStmtNum ++;
                    gtInaccurateFrSet.add(fr);
                }
                if (record.getMtdInaccurate() == 1){
                    mtdInaccurateStmtNum ++;
                    mtdInaccurateFrSet.add(fr);
                }
                if (record.getIjmInaccurate() == 1){
                    ijmInaccurateStmtNum ++;
                    ijmInaccurateFrSet.add(fr);
                }
            }
        }

        System.out.println("GT INACCURATE STMT: " + gtInaccurateStmtNum);
        System.out.println("GT INACCURATE FR: " + gtInaccurateFrSet.size());
        System.out.println("GT INACCURATE FR RATE: " + gtInaccurateFrSet.size() / numberOfFileRevision);
        System.out.println("MTD INACCURATE STMT: " + mtdInaccurateStmtNum);
        System.out.println("MTD INACCURATE FR: " + mtdInaccurateFrSet.size());
        System.out.println("MTD INACCURATE FR RATE: " + mtdInaccurateFrSet.size() / numberOfFileRevision);
        System.out.println("IJM INACCURATE STMT: " + ijmInaccurateStmtNum);
        System.out.println("IJM INACCURATE FR: " + ijmInaccurateFrSet.size());
        System.out.println("IJM INACCURATE FR RATE: " + ijmInaccurateFrSet.size() / numberOfFileRevision);
    }

    private static void calFileRevisionStatistics(Map<FileRevision, List<ComparisonRecord>> recordMap, String algorithm){
        Set<FileRevision> revisions = new HashSet<>();
        int inaccurateStmtNum = 0;
        for (FileRevision fr: recordMap.keySet()){
            List<ComparisonRecord> recordList = recordMap.get(fr);
            for (ComparisonRecord record: recordList){
                if (record.getSrcStmtOrTokenError() == 1 && record.getAlgorithm().equals(algorithm)) {
                    inaccurateStmtNum++;
                    revisions.add(record.getFileRevision());
                }
                if (record.getDstStmtOrTokenError() == 1 && record.getAlgorithm().equals(algorithm)) {
                    inaccurateStmtNum++;
                    revisions.add(record.getFileRevision());
                }
            }
        }

        System.out.println(algorithm + ": " + inaccurateStmtNum);
        System.out.println(algorithm + ": " + revisions.size());
    }

    private static void setRevisionStmtMap(String project, Map<FileRevision, List<ComparisonRecord>> allRecordMap,
                                           Map<FileRevision, Set<StmtObj>> revisionStmtMap){
        for (FileRevision fr: allRecordMap.keySet()){
            List<ComparisonRecord> recordList = allRecordMap.get(fr);
            if (!revisionStmtMap.containsKey(fr))
                revisionStmtMap.put(fr, new HashSet<>());
            for (ComparisonRecord record: recordList){
                StmtMapEvalRecord sr = StmtMapEvalRecord.getRecordForStmt(project, record, true);
                if (!sr.getStmtObj().isNull())
                    revisionStmtMap.get(fr).add(sr.getStmtObj());
                StmtMapEvalRecord sr2 = StmtMapEvalRecord.getRecordForStmt(project, record, false);
                if (!sr2.getStmtObj().isNull())
                    revisionStmtMap.get(fr).add(sr2.getStmtObj());
            }
        }
    }

    private static void setRevisionStmtRecordMap(Map<FileRevision, Set<StmtObj>> revisionStmtMap,
                                                 Map<FileRevision, List<StmtMapEvalRecord>> revisionStmtRecordMap){
        for (FileRevision fr: revisionStmtMap.keySet()){
            Set<StmtObj> objs = revisionStmtMap.get(fr);
            List<StmtObj> objList = getOrderedStmtObjs(objs);
            revisionStmtRecordMap.put(fr, new ArrayList<>());
            for (StmtObj obj: objList){
                revisionStmtRecordMap.get(fr).add(StmtMapEvalRecord.getRecordForStmtObj(obj));
            }
        }
    }

    private static List<StmtObj> getOrderedStmtObjs(Set<StmtObj> objs){
        List<StmtObj> objList = new ArrayList<>(objs);
        Collections.sort(objList, new Comparator<StmtObj>() {
            @Override
            public int compare(StmtObj o1, StmtObj o2) {
                if (o1.isSrc() && !o2.isSrc())
                    return 1;
                if (!o1.isSrc() && o2.isSrc())
                    return -1;
                return o1.getStartPos() - o2.getStartPos();
            }
        });
        return objList;
    }
}
