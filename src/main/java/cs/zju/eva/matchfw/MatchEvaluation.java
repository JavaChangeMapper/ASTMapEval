package cs.zju.eva.matchfw;

import cs.zju.framework.match.EvalASTMatchForCommit;
import cs.zju.framework.match.InconsistentMatch;
import cs.zju.gitops.CommitOps;
import cs.zju.gitops.GitUtils;
import cs.zju.stm.match.StmtMatchDiffDesc;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.Pair;
import cs.zju.utils.PathResolver;
import cs.zju.utils.TimeUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class MatchEvaluation {

    private static final Timestamp afterDate = TimeUtil.string2Time("1990-01-01 00:00:00.000");
    private static final Timestamp beforeDate = TimeUtil.string2Time("2019-01-01 00:00:00.000");
    private static final String[] methods = {"gt", "mtdiff", "ijm"};

    private static String project;
    private static Map<Pair<String, String>, List<String[]>> methodRecordMap = new HashMap<>();
    private static List<String[]> processingCommitRecords = new ArrayList<>();
    private static Set<String> processedCommits;
    private static Map<String, Set<String>> commitFileMap;


    public static void init(String project){
        MatchEvaluation.project = project;
        commitFileMap = getProcessedCommitsInCsv(project, true);
        processedCommits = new HashSet<>(getProcessedCommitsInCsv(project, false).keySet());
        methodRecordMap = new HashMap<>();
        for (int i = 0; i < methods.length - 1; i ++){
            String method1 = methods[i];
            for (int j = i + 1; j < methods.length; j++){
                String method2 = methods[j];
                if (method1.equals(method2))
                    continue;
                methodRecordMap.put(new Pair<>(method1, method2), new ArrayList<>());
            }
        }
        processingCommitRecords = new ArrayList<>();
    }

    private static void addRecord(Map<String, List<InconsistentMatch>> fileMatchMap){
        for (String filePath: fileMatchMap.keySet()){
            List<InconsistentMatch> inconsistentMatchList = fileMatchMap.get(filePath);
            for (InconsistentMatch m: inconsistentMatchList){
                methodRecordMap.get(new Pair<>(m.getCurAlgorithm(), m.getCompareAlgorithm())).addAll(m.getMatchDiffRecords());
            }
        }
    }

    private static void filterRecords(List<String[]> records){
        int size = records.size();
        for (int i = size - 1; i >= 0; i --){
            String[] record = records.get(i);
            if (!record[record.length - 1].equals("3") || !record[record.length - 2].equals("3")){
                records.remove(i);
            }
        }
    }

    private static void flushCsvRecord(int idx) throws IOException {
        String[] headers = StmtMatchDiffDesc.getHeaders();
        System.out.println(methodRecordMap.keySet());
        for (Pair<String, String> algorithmPair: methodRecordMap.keySet()){
            String algorithm1 = algorithmPair.first;
            String algorithm2 = algorithmPair.second;
            List<String[]> records = methodRecordMap.get(algorithmPair);
//            filterRecords(records);
            String filePath = PathResolver.getStmtMatchDiffResultCsvPath(project, algorithm1, algorithm2, idx);
            CsvOperationsUtil.writeCSV(filePath, headers, records);
        }

        String filePath = PathResolver.getStmtMatchDiffRecordCsvPath(project, idx);
        CsvOperationsUtil.writeCSV(filePath, RunningRecord.getHeaders(), processingCommitRecords);

        for (int i = 0; i < methods.length - 1; i ++){
            String method1 = methods[i];
            for (int j = i + 1; j < methods.length; j++){
                String method2 = methods[j];
                if (method1.equals(method2))
                    continue;
                methodRecordMap.put(new Pair<>(method1, method2), new ArrayList<>());
            }
        }
        processingCommitRecords = new ArrayList<>();
    }

    public static void run() throws IOException {
        List<String> commitIds = GitUtils.getAllCommitIds(project);
        int idx = 0;
        for (String commitId: commitIds){
            if (isProcessed(commitId)){
                idx ++;
                continue;
            }
            RevCommit tmp = GitUtils.getCommitObjById(project, commitId);
            if (CommitOps.getCommitTime(tmp) < afterDate.getTime() / 1000)
                continue;
            if (CommitOps.getCommitTime(tmp) > beforeDate.getTime() / 1000)
                continue;

            System.out.println(commitId);

            try{
                Set<String> filesToAnalyze = commitFileMap.get(commitId);
                EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, commitId, methods, filesToAnalyze);
                Map<String, List<InconsistentMatch>> fileInconsistentMatchMap = eval.getInconsistentMatchMap();
                addRecord(fileInconsistentMatchMap);
                Map<String, Map<String, Integer>> actionNumMap = eval.getActionNumMap();
                Map<String, Map<String, Double>> phase1TimeMap = eval.getPhase1TimeMap();
                Map<String, Map<String, Double>> phase2TimeMap = eval.getPhase2TimeMap();

                for (String filePath: eval.getAnalyzedFiles()) {
                    RunningRecord record = new RunningRecord(commitId, filePath, actionNumMap.get(filePath),
                            phase1TimeMap.get(filePath), phase2TimeMap.get(filePath));
                    processingCommitRecords.add(record.toCsvRecord());
                }


            } catch (Exception e){
                System.out.println("HELLO: " + commitId);
                e.printStackTrace();
            }

            idx ++;

            if (idx != 0 && idx % 100 == 0){
                int tmpIdx = idx/100;
                flushCsvRecord(tmpIdx);
            }

        }
        int tmpIdx = idx / 100 + 1;
        flushCsvRecord(tmpIdx);
    }

    private static Map<String, Set<String>> getProcessedCommitsInCsv(String project, boolean isBackup) {
        Map<String, Set<String>> ret = new HashMap<>();
        int idx = 1;
        try {
            while (true) {
                Map<String, Set<String>> processedCommits = getProcessedCommitsInCsvIdx(project, idx, isBackup);
                if (processedCommits.size() == 0)
                    break;
                ret.putAll(processedCommits);
                idx++;
            }
        } catch (Exception e){
            throw new RuntimeException("read csv error");
        }
        return ret;
    }


    private static Map<String, Set<String>> getProcessedCommitsInCsvIdx(String project, int idx, boolean isBackup) throws Exception{
        Map<String, Set<String>> ret = new HashMap<>();
        String[] headers = RunningRecord.getHeaders();
        String resultCsvPath = PathResolver.getStmtMatchDiffRecordCsvPath(project, idx);
        if (isBackup) {
            headers = CacheRunningRecord.getHeaders();
            resultCsvPath = PathResolver.getStmtMatchDiffRecordBackupCsvPath(project, idx);
        }

        if (!new File(resultCsvPath).exists())
            return new HashMap<>();
        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, headers);
        for (String[] record: records){
            String commitId = record[0];
            String filePath = record[1];
            if (!ret.containsKey(commitId))
                ret.put(commitId, new HashSet<>());
            ret.get(commitId).add(filePath);
        }
        return ret;
    }

    private static boolean isProcessed(String commitId){
        return processedCommits.contains(commitId);
    }
}
