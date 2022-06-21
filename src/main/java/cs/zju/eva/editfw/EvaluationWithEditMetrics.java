package cs.zju.eva.editfw;

import cs.zju.eva.utils.EvalCommitFileInfo;
import cs.zju.gitops.CommitOps;
import cs.zju.gitops.GitUtils;
import cs.zju.framework.edit.EvalCommitAnalysisWithEdit;
import cs.zju.framework.edit.EvalComparisonResultWithEdit;
import cs.zju.stm.edit.tokenedit.actions.StmtEditActionGroup;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import cs.zju.utils.TimeUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

public class EvaluationWithEditMetrics {
    private static final Timestamp afterDate = TimeUtil.string2Time("1990-01-01 00:00:00.000");
    private static final Timestamp beforeDate = TimeUtil.string2Time("2019-01-01 00:00:00.000");
    private static final String[] methods = {"gt", "mtdiff", "ijm"};

    private static Map<String, EvalCommitAnalysisWithEdit> methodResultMap = new HashMap<>();
    private static Map<String, Set<Integer>> srcStartLineMap;
    private static Map<String, Set<Integer>> dstStartLineMap;
    private static String project;
    private static Set<String> gtProcessedCommits;
    private static Set<String> mtdProcessedCommits;
    private static Set<String> ijmProcessedCommits;

    private static Map<String, Set<String>> evalCommitFiles;

    private static Map<String, List<String[]>> allMethodFileRevisionRecordMap;
    private static Map<String, List<String[]>> allMethodStmtRecordMap;


    public static void init(String project){
        EvaluationWithEditMetrics.project = project;
        gtProcessedCommits = getProcessedCommitsInCsv(project, "gt");
        mtdProcessedCommits = getProcessedCommitsInCsv(project, "mtdiff");
        ijmProcessedCommits = getProcessedCommitsInCsv(project, "ijm");
        try {
            evalCommitFiles = EvalCommitFileInfo.getProcessedCommitFilesInCsv(project, "mtdiff");
        } catch (Exception e){
            evalCommitFiles = null;
        }
    }

    private static void initAllRecordMap(){
        allMethodFileRevisionRecordMap = new HashMap<>();
        allMethodStmtRecordMap = new HashMap<>();
        for (String method: methods){
            allMethodFileRevisionRecordMap.put(method, new ArrayList<>());
            allMethodStmtRecordMap.put(method, new ArrayList<>());
        }
    }


    private static void calAnalysis(String commitId, String matcherMethod,
                                    Set<String> filesToAnalyze){
        EvalCommitAnalysisWithEdit analysis = new EvalCommitAnalysisWithEdit(project, commitId, matcherMethod, filesToAnalyze);
        methodResultMap.put(matcherMethod, analysis);
    }

    private static void calLineSetForEvaluation(){
        srcStartLineMap = new HashMap<>();
        dstStartLineMap = new HashMap<>();
        for (String matcherMethod: methodResultMap.keySet()){
            EvalCommitAnalysisWithEdit analysis = methodResultMap.get(matcherMethod);
            List<EvalComparisonResultWithEdit> resultList = analysis.getOriginResultList();
            for (EvalComparisonResultWithEdit result: resultList){
                String srcFilePath = result.getSrcFilePath();
                if (!srcStartLineMap.containsKey(srcFilePath))
                    srcStartLineMap.put(srcFilePath, new HashSet<>());
                if (!dstStartLineMap.containsKey(srcFilePath))
                    dstStartLineMap.put(srcFilePath, new HashSet<>());
                List<StmtEditActionGroup> groups = result.getStmtEditGroupList();
                if (groups != null){
                    for (StmtEditActionGroup group: groups){
                        if (group.getStmtChangeType() == null)
                            continue;
                        if (group.getSrcStmtStartLine() != -1)
                            srcStartLineMap.get(srcFilePath).add(group.getSrcStmtStartLine());
                        if (group.getDstStmtStartLine() != -1)
                            dstStartLineMap.get(srcFilePath).add(group.getDstStmtStartLine());
                    }
                }
            }
        }
    }

    private static void assignRecordMap(String matcherMethod){
        String originMapMethod = matcherMethod;
        EvalCommitAnalysisWithEdit analysis = methodResultMap.get(originMapMethod);
        List<String[]> originRecords = getRecords(analysis, originMapMethod);
        List<String[]> originStmtRecords = getStmtRecords(analysis, originMapMethod);

        if (originRecords == null)
            return;

        allMethodFileRevisionRecordMap.get(originMapMethod).addAll(originRecords);
        allMethodStmtRecordMap.get(originMapMethod).addAll(originStmtRecords);
    }

    private static void writeCurRecordCsv(int tmpIdx) throws Exception{
        String[] headers = FileRevisionMeasures.getHeaders();
        String[] stmtHeaders = StmtEvaluationMeasures.getHeaders();
        for (String method: methods){
            writeCsv(method, tmpIdx, headers, allMethodFileRevisionRecordMap.get(method));
            writeStmtEvalCsv(method, tmpIdx, stmtHeaders, allMethodStmtRecordMap.get(method));
        }
        initAllRecordMap();
    }

    private static boolean isEmptyRecord(){
        return allMethodFileRevisionRecordMap.get("gt").isEmpty();
//        return allMethodFileRevisionRecordMap.get("ijm").isEmpty();
    }

    public static void run() throws Exception {
        List<String> commitIds = GitUtils.getAllCommitIds(project);
        initAllRecordMap();

        int idx = 0;
        for (String commitId : commitIds) {
            boolean cond = evalCommitFiles != null && evalCommitFiles.size() > 0
                    && !evalCommitFiles.containsKey(commitId);
            if (cond)
                continue;

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
            try {
                Set<String> filesToAnalyze = null;
                if (evalCommitFiles != null && evalCommitFiles.size() > 0)
                    filesToAnalyze = evalCommitFiles.get(commitId);

                calAnalysis(commitId, "gt", filesToAnalyze);
                calAnalysis(commitId, "mtdiff", filesToAnalyze);
                calAnalysis(commitId, "ijm", filesToAnalyze);

                calLineSetForEvaluation();

                assignRecordMap("gt");
                assignRecordMap("mtdiff");
                assignRecordMap("ijm");

                idx ++;
                if (idx != 0 && idx % 100 == 0){
                    int tmpIdx = idx / 100;
                    writeCurRecordCsv(tmpIdx);
                }
            } catch (Exception e) {
                System.out.println("HELLO: " + commitId);
            }
        }

        if (!isEmptyRecord()){
            int tmpIdx = idx / 100 + 1;
            writeCurRecordCsv(tmpIdx);
        }

        combineData();
    }

    private static void combineData() throws Exception {
        for (String method: methods) {
            CombineCsvData.combineCsvData(project, method, false);
            CombineCsvData.combineCsvData(project, method, true);
        }
    }

    private static void writeCsv(String mapMethod, int tmpIdx, String[] headers,
                                 List<String[]> allRecords) throws Exception {
        String resultCsvPath = PathResolver.getResultCsv(project, mapMethod, tmpIdx);
        CsvOperationsUtil.writeCSV(resultCsvPath, headers, allRecords);
    }

    private static void writeStmtEvalCsv(String mapMethod, int tmpIdx, String[] headers,
                                         List<String[]> allRecords) throws Exception {
        String resultCsvPath = PathResolver.getStmtResultCsv(project, mapMethod, tmpIdx);
        CsvOperationsUtil.writeCSV(resultCsvPath, headers, allRecords);
    }

    private static List<String[]> getRecords(EvalCommitAnalysisWithEdit analysis, String mapMethod){
        List<String[]> records = getEvaluationRecords(analysis, mapMethod);
        if (records.size() == 0)
            return null;
        return records;
    }

    private static List<String[]> getStmtRecords(EvalCommitAnalysisWithEdit analysis, String mapMethod){
        List<EvalComparisonResultWithEdit> resultList = analysis.getOriginResultList();
        List<String[]> ret = new ArrayList<>();
        String commitId = analysis.getCommitId();
        for (EvalComparisonResultWithEdit result: resultList){
            List<StmtEditActionGroup> groups = result.getStmtEditGroupList();
            String srcFilePath = result.getSrcFilePath();
            Set<Integer> srcStartLines = srcStartLineMap.get(srcFilePath);
            Set<Integer> dstStartLines = dstStartLineMap.get(srcFilePath);
            if (groups != null){
                for (StmtEditActionGroup group: groups){
                    if (!srcStartLines.contains(group.getSrcStmtStartLine()) &&
                            !dstStartLines.contains(group.getDstStmtStartLine()))
                        continue;
                    StmtEvaluationMeasures sem = new StmtEvaluationMeasures(group, project, mapMethod,
                            commitId, srcFilePath);
                    ret.add(sem.toCsvRecord());
                }
            }
        }
        return ret;
    }

    private static List<String[]> getEvaluationRecords(EvalCommitAnalysisWithEdit analysis,
                                                       String mapMethod){
        List<EvalComparisonResultWithEdit> resultList = analysis.getOriginResultList();

        List<String[]> ret = new ArrayList<>();
        for (EvalComparisonResultWithEdit result: resultList){
            FileRevisionMeasures frm = new FileRevisionMeasures(result);
            String[] record = frm.toCsvRecord();
            if (record == null)
                continue;
            ret.add(record);
        }
        return ret;
    }

    private static Set<String> getProcessedCommitsInCsv(String project, String mapMethod) {
        Set<String> ret = new HashSet<>();
        int idx = 1;
        try {
            while (true) {
                Set<String> processedCommits = getProcessedCommitsInCsvIdx(project, mapMethod, idx);
                if (processedCommits.size() == 0)
                    break;
                ret.addAll(processedCommits);
                idx++;
            }
        } catch (Exception e){
            throw new RuntimeException("read csv error");
        }
        return ret;
    }

    private static Set<String> getProcessedCommitsInCsvIdx(String project, String mapMethod, int idx) throws Exception{
        Set<String> ret = new HashSet<>();
        String[] headers = FileRevisionMeasures.getHeaders();

        String resultCsvPath = PathResolver.getResultCsv(project, mapMethod, idx);
        if (!new File(resultCsvPath).exists())
            return new HashSet<>();

        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, headers);
        for (String[] record: records){
            ret.add(record[2]);
        }
        return ret;
    }

    private static boolean isProcessed(String commitId){
        return gtProcessedCommits.contains(commitId) &&
                mtdProcessedCommits.contains(commitId) &&
                ijmProcessedCommits.contains(commitId);
    }
}
