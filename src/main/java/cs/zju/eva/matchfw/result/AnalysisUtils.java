package cs.zju.eva.matchfw.result;

import cs.zju.eva.matchfw.RunningRecord;
import cs.zju.eva.utils.FileRevision;
import cs.zju.framework.match.EvalASTMatchForCommit;
import cs.zju.framework.match.InconsistentMatch;
import cs.zju.stm.match.StmtMatchDiffDesc;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AnalysisUtils {

    public static List<String[]> getAllCSVRecords(String project, String algorithm1, String algorithm2,
                                                  boolean originalHeaders) {
        List<String[]> ret = new ArrayList<>();
        int idx = 1;
        try {
            while (true) {
                List<String[]> records = getAllRecordsIdx(project, algorithm1, algorithm2, idx, originalHeaders);
                if (records == null)
                    break;
                ret.addAll(records);
                idx++;
            }
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("read csv error");
        }
        return ret;
    }


    private static String[] getOriginalHeaders(){
        String[] originalHeaders = {
                "commitId", "filePath", "algorithm", "stmtType", "srcCharPos",
                "dstCharPos", "srcStmtLine", "dstStmtLine", "compareAlgorithm",
                "diffType", "srcStmtLine2", "dstStmtLine2", "srcStmtLine3", "dstStmtLine3",
                "ratioOfIdenticalToken", "betterAlgorithm", "errorType"
        };
        return originalHeaders;
    }

    private static List<String[]> getAllRecordsForUserStudy(String project, String algorithm1, String algorithm2) throws Exception {
        String[] headers = StmtMatchDiffDesc.getHeaders();
        String resultCsvPath = PathResolver.getUserStudyRunningPath(project, algorithm1, algorithm2);
        if (!new File(resultCsvPath).exists())
            return null;
        return CsvOperationsUtil.getCSVData(resultCsvPath, headers);
    }

    private static List<String[]> getAllRecordsIdx(String project, String algorithm1, String algorithm2,
                                                   int idx, boolean originalHeaders) throws Exception{
        String[] headers = StmtMatchDiffDesc.getHeaders();
        if (originalHeaders)
            headers = getOriginalHeaders();
        String resultCsvPath = PathResolver.getStmtMatchDiffResultCsvPath(project, algorithm1, algorithm2, idx);

        if (!new File(resultCsvPath).exists())
            return null;
        return CsvOperationsUtil.getCSVData(resultCsvPath, headers);
    }

    public static List<FileRevision> getAllFileRevisions(List<RunningRecord> records){
        List<FileRevision> revisions = new ArrayList<>();
        for (RunningRecord r: records) {
            FileRevision fr = new FileRevision(r.getCommitId(), r.getFilePath());
            revisions.add(fr);
        }
        return revisions;
    }

    public static List<RunningRecord> getAllFileRevisionRecords(String project){
        List<RunningRecord> ret = new ArrayList<>();
        int idx = 1;
        try {
            while (true) {
                List<String[]> records = getFileRevisionRecordIdx(project, idx);
                if (records == null)
                    break;
                if (records.size() == 0) {
                    idx ++;
                    continue;
                }
                for (String[] record: records)
                    ret.add(RunningRecord.fromCsvRecord(record));
                idx++;
            }
        } catch (Exception e){
            throw new RuntimeException("read csv error");
        }
        return ret;
    }

    private static List<String[]> getFileRevisionRecordIdx(String project, int idx) throws Exception {
        String[] headers = RunningRecord.getHeaders();
        String recordCsvPath = PathResolver.getStmtMatchDiffRecordCsvPath(project, idx);

        if (!new File(recordCsvPath).exists())
            return null;
        return CsvOperationsUtil.getCSVData(recordCsvPath, headers);
    }

    public static void calAllRecordsForUserStudy(String project, String algorithm1, String algorithm2,
                                                 Map<FileRevision, List<ComparisonRecord>> allRecordMap) throws Exception {
        List<String[]> allCsvRecords = getAllRecordsForUserStudy(project, algorithm1, algorithm2);
        for (String[] record: allCsvRecords){
            ComparisonRecord cr = new ComparisonRecord(record, false, true);
            FileRevision fr = cr.getFileRevision();
            if (!allRecordMap.containsKey(fr))
                allRecordMap.put(fr, new ArrayList<>());
            allRecordMap.get(fr).add(cr);
        }
    }

    public static void calAllRecords(String project, String algorithm1, String algorithm2,
                                     Map<FileRevision, List<ComparisonRecord>> allRecordMap){
        List<String[]> allCsvRecords = getAllCSVRecords(project, algorithm1, algorithm2, false);
        for (String[] record: allCsvRecords){
            ComparisonRecord cr = new ComparisonRecord(record, false, true);
            FileRevision fr = cr.getFileRevision();
            if (!allRecordMap.containsKey(fr))
                allRecordMap.put(fr, new ArrayList<>());
            allRecordMap.get(fr).add(cr);
        }
    }

    public static void calAllRecords(String project, String algorithm1, String algorithm2,
                                      List<FileRevision> fileRevisions,
                                      Map<FileRevision, List<ComparisonRecord>> allRecordMap,
                                      Map<FileRevision, List<ComparisonRecord>> errorRecordMap,
                                     boolean originalHeaders){
        List<String[]> allCsvRecords = getAllCSVRecords(project, algorithm1, algorithm2, originalHeaders);
        for (String[] record: allCsvRecords){
            ComparisonRecord cr = new ComparisonRecord(record, false, !originalHeaders);
            FileRevision fr = cr.getFileRevision();
            if (!allRecordMap.containsKey(fr)) {
                allRecordMap.put(fr, new ArrayList<>());
                fileRevisions.add(fr);
            }
            allRecordMap.get(fr).add(cr);
            if (cr.getSrcStmtOrTokenError() == 1 || cr.getDstStmtOrTokenError() == 1){
                if (!errorRecordMap.containsKey(fr))
                    errorRecordMap.put(fr, new ArrayList<>());
                errorRecordMap.get(fr).add(cr);
            }
        }
    }

    public static int getCommitNumber(List<RunningRecord> records){
        Set<String> commits = new HashSet<>();
        for (RunningRecord record: records){
            commits.add(record.getCommitId());
        }
        return commits.size();
    }

    public static void generateManualAnalysisScriptAndCsv(String project, String method1, String method2,
                                                           List<FileRevision> revisions, int fileRevisionNum) throws IOException {
        String[] mapMethods = {method1, method2};
        List<String[]> allRecords = new ArrayList<>();
        List<String> allDiffStrs = new ArrayList<>();
        int i = 0;
        for (FileRevision revision: revisions){
            EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, revision, mapMethods);
            Map<String, List<InconsistentMatch>> map = eval.getInconsistentMatchMap();
            InconsistentMatch im = map.get(revision.second).get(0);
            List<String[]> csvRecords = im.getMatchDiffRecords();
            List<String> diffStrs = im.getMatchDiffStrs();
            allRecords.addAll(csvRecords);
            allDiffStrs.addAll(diffStrs);
            i++;
            if (fileRevisionNum != -1 && i == fileRevisionNum)
                break;
        }

        String csvPath = PathResolver.getStmtMatchManualAnalysisCsvPath(project, method1, method2);
        String scriptPath = PathResolver.getStmtMatchManualAnalysisScriptPath(project, method1, method2);
        CsvOperationsUtil.writeCSV(csvPath, StmtMatchDiffDesc.getHeaders(), allRecords);
        FileUtils.writeLines(new File(scriptPath), allDiffStrs);
    }

    public static void generateUserStudyApproachResult(String project, String algorithm1, String algorithm2,
                                                       Set<FileRevision> revisions) throws IOException {
        String[] mapMethods = {algorithm1, algorithm2};
        List<String[]> allRecords = new ArrayList<>();
        for (FileRevision revision: revisions){
            EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, revision, mapMethods);
            Map<String, List<InconsistentMatch>> map = eval.getInconsistentMatchMap();
            InconsistentMatch im = map.get(revision.second).get(0);
            List<String[]> csvRecords = im.getMatchDiffRecords();
            allRecords.addAll(csvRecords);
        }

        String csvPath = PathResolver.getUserStudyRunningPath(project, algorithm1, algorithm2);
        CsvOperationsUtil.writeCSV(csvPath, StmtMatchDiffDesc.getHeaders(), allRecords);
    }

}
