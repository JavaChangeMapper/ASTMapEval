package cs.zju.eva.matchfw.result;


import cs.zju.eva.matchfw.RunningRecord;
import cs.zju.eva.utils.FileRevision;
import java.util.*;

public class ResultAnalysis {
    private static final String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static String[] algorithms = {"gt", "mtdiff", "ijm"};

    public static void main(String[] args){
        int fileRevisionNum = 0;
        for (String project: projects){
            List<RunningRecord> fileRevisionRecords = AnalysisUtils.getAllFileRevisionRecords(project);
            fileRevisionNum += fileRevisionRecords.size();
            System.out.println(project);
            System.out.println("revision num: " + fileRevisionRecords.size());
            System.out.println("commit num: " + AnalysisUtils.getCommitNumber(fileRevisionRecords));
            System.out.println();

            StmtMatchGroup.init();
            for (int i = 0; i < algorithms.length - 1; i++) {
                String algorithm1 = algorithms[i];
                System.out.println(algorithm1);
                Map<FileRevision, Set<StmtMatchGroup>> allErrorRecordsMap = new HashMap<>();
                for (int j = i + 1; j < algorithms.length; j++){
                    String algorithm2 = algorithms[j];
                    if (algorithm1.equals(algorithm2))
                        continue;
                    System.out.println(algorithm2);
                    Map<FileRevision, List<ComparisonRecord>> fileRecordsMap = new HashMap<>();
                    Map<FileRevision, List<ComparisonRecord>> fileErrorRecordsMap = new HashMap<>();
                    List<FileRevision> revisions = new ArrayList<>();
                    AnalysisUtils.calAllRecords(project, algorithm1, algorithm2, revisions,
                            fileRecordsMap, fileErrorRecordsMap, false);

                    System.out.println(fileRecordsMap.size());
                    System.out.println(fileErrorRecordsMap.size());
                    combineErrorRecords(allErrorRecordsMap, fileErrorRecordsMap);
                }

                System.out.println(allErrorRecordsMap.size());
                System.out.println(getNumOfErrorStmtMatchGroups(allErrorRecordsMap));
            }
        }
        System.out.println(fileRevisionNum);
    }

    private static int getNumOfErrorStmtMatchGroups(Map<FileRevision, Set<StmtMatchGroup>> allErrorRecordsMap){
        int ret = 0;
        for (FileRevision fr: allErrorRecordsMap.keySet()){
            ret += allErrorRecordsMap.get(fr).size();
        }
        return ret;
    }

    private static void combineErrorRecords(Map<FileRevision, Set<StmtMatchGroup>> allErrorRecordsMap,
                                            Map<FileRevision, List<ComparisonRecord>> errorRecordsMap){
        for (FileRevision fr: errorRecordsMap.keySet()){
            if (!allErrorRecordsMap.containsKey(fr))
                allErrorRecordsMap.put(fr, new HashSet<>());
            List<ComparisonRecord> recordList = errorRecordsMap.get(fr);
            for (ComparisonRecord record: recordList){
                StmtMatchGroup smg = StmtMatchGroup.getStmtMatchGroup(record);
                allErrorRecordsMap.get(fr).add(smg);
            }
        }
    }
}
