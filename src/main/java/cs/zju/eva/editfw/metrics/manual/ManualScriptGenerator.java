package cs.zju.eva.editfw.metrics.manual;

import cs.zju.config.MyConfig;
import cs.zju.eva.editfw.metrics.accuracy.StmtRecord;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.framework.edit.EvalCommitAnalysisWithEdit;
import cs.zju.framework.edit.EvalComparisonResultWithEdit;
import cs.zju.stm.edit.tokenedit.actions.StmtEditActionGroup;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ManualScriptGenerator {
    private static final String[] projects = {
             "activemq", "junit4", "commons-io", "commons-lang","commons-math",
                        "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    // "activemq", "junit4", "commons-io", "commons-lang","commons-math",
    //            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"

    private static Map<String, Set<Integer>> gtProjectStmtIdxMap;
    private static Map<String, Set<Integer>> mtdProjectStmtIdxMap;
    private static Map<String, Set<Integer>> ijmProjectStmtIdxMap;

    private static final String COMMIT_ID = "commitId";
    private static final String FILE_PATH = "srcFilePath";
    private static final String STMT_TYPE = "stmtType";
    private static final String SRC_START_POS = "srcStartPos";
    private static final String DST_START_POS = "dstStartPos";
    private static final String SHORT_STMT_TOKEN = "shortStmtTokenCount";
    private static final String CEC = "CEC";
    private static final String SDC = "SDC";
    private static final String MMTC = "MMTC";

    public static void main(String[] args) throws Exception {
        gtProjectStmtIdxMap = new HashMap<>();
        mtdProjectStmtIdxMap = new HashMap<>();
        ijmProjectStmtIdxMap = new HashMap<>();

        List<String> cecScript = new ArrayList<>();
        List<String> sdcScript = new ArrayList<>();
        List<String> mmtcScript = new ArrayList<>();

        List<String[]> allScriptRecords = new ArrayList<>();
        for (String project: projects){
            gtProjectStmtIdxMap.put(project, new HashSet<>());
            mtdProjectStmtIdxMap.put(project, new HashSet<>());
            ijmProjectStmtIdxMap.put(project, new HashSet<>());
        }

        for (String project: projects){
            cecScript.addAll(generateCECCheckScript(project,  "gt", allScriptRecords));
            cecScript.addAll(generateCECCheckScript(project, "mtdiff", allScriptRecords));
            cecScript.addAll(generateCECCheckScript(project, "ijm", allScriptRecords));
        }

        for (String project: projects){
            sdcScript.addAll(generateSDCCheckScript(project, "gt", allScriptRecords));
            sdcScript.addAll(generateSDCCheckScript(project, "mtdiff", allScriptRecords));
            sdcScript.addAll(generateSDCCheckScript(project, "ijm", allScriptRecords));
        }

        for (String project: projects){
            mmtcScript.addAll(generateMMTCCheckScript(project, "gt", allScriptRecords));
            mmtcScript.addAll(generateMMTCCheckScript(project, "mtdiff", allScriptRecords));
            mmtcScript.addAll(generateMMTCCheckScript(project, "ijm", allScriptRecords));
        }

        writeScript(cecScript, "CEC");
        writeScript(sdcScript, "SDC");
        writeScript(mmtcScript, "MMTC");

        String csvPath = PathResolver.getManualAnalysisCsvPath();
        CsvOperationsUtil.writeCSV(csvPath, getScriptRecordHeaders(), allScriptRecords);
    }

    private static void writeScript(List<String> scriptList, String metric) throws IOException {
        String manualScriptPath = PathResolver.getManualAnalysisScriptPath(metric);
        FileUtils.writeLines(new File(manualScriptPath), scriptList);
    }

    private static void addRecordIdxToMap(String mapMethod, String project, int index){
        if (mapMethod.equals("gt"))
            gtProjectStmtIdxMap.get(project).add(index);
        if (mapMethod.equals("mtdiff"))
            mtdProjectStmtIdxMap.get(project).add(index);
        if (mapMethod.equals("ijm"))
            ijmProjectStmtIdxMap.get(project).add(index);
    }

    private static boolean hasBeenGeneratedAsScript(String mapMethod, String project, int index){
        if (mapMethod.equals("gt"))
            return gtProjectStmtIdxMap.get(project).contains(index);
        if (mapMethod.equals("mtdiff"))
            return mtdProjectStmtIdxMap.get(project).contains(index);
        if (mapMethod.equals("ijm"))
            return ijmProjectStmtIdxMap.get(project).contains(index);
        return false;
    }

    private static List<String> generateCECCheckScript(String project, String mapMethod, List<String[]> allScriptRecords) throws Exception {
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "ALL");
        CsvDataExtractor extractor = new CsvDataExtractor(csvPath, StmtRecord.getHeaders());
        List<String[]> records = extractor.getRecords();

        List<String> ret = new ArrayList<>();
        int count = 0;
        int index = 0;

        EvalCommitAnalysisWithEdit analysis = null;
        String commitId = null;
        String filePath = null;
        for (String[] record: records){
            if (hasBeenGeneratedAsScript(mapMethod, project, index)){
                index ++;
                continue;
            }
            double cec = extractor.getColDoubleVal(record, CEC);
            int tokenCount = Integer.parseInt(extractor.getColVal(record, SHORT_STMT_TOKEN));
            if (tokenCount > 3 && cec == 0){
                String[] scriptRecord = getRecordFromStmtRecord("CEC", project, mapMethod, record, extractor);
                allScriptRecords.add(scriptRecord);
                String commitId2 = extractor.getColVal(record, COMMIT_ID);
                String srcFilePath2 = extractor.getColVal(record, FILE_PATH);
                int srcStartPos = Integer.parseInt(extractor.getColVal(record, SRC_START_POS));
                int dstStartPos = Integer.parseInt(extractor.getColVal(record, DST_START_POS));
                String stmtType = extractor.getColVal(record, STMT_TYPE);
                if (!commitId2.equals(commitId) || !srcFilePath2.equals(filePath)) {
                    commitId = commitId2;
                    filePath = srcFilePath2;
                    analysis = performAnalysis(project, commitId, mapMethod, filePath);
                }
                String url = MyConfig.getCommitUrl(project, commitId);
                ret.add("===============================================");
                ret.add(mapMethod);
                ret.add(url);
                ret.add(filePath);
                ret.add("\n");
                StmtEditActionGroup group = getStmtEditActionGroup(analysis, srcStartPos, dstStartPos, stmtType);
                ret.add(group.toString());
                ret.add("\n");
                count ++;

                addRecordIdxToMap(mapMethod, project, index);
            }
            index ++;
            if (count == 20)
                break;
        }
        return ret;
    }

    private static List<String> generateSDCCheckScript(String project, String mapMethod, List<String[]> allScriptRecords) throws Exception{
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "ALL");
        CsvDataExtractor extractor = new CsvDataExtractor(csvPath, StmtRecord.getHeaders());
        List<String[]> records = extractor.getRecords();

        List<String> ret = new ArrayList<>();
        int count = 0;
        int index = 0;

        EvalCommitAnalysisWithEdit analysis = null;
        String commitId = null;
        String filePath = null;
        for (String[] record: records){
            if (hasBeenGeneratedAsScript(mapMethod, project, index)){
                index ++;
                continue;
            }
            double sdc = extractor.getColDoubleVal(record, SDC);
            if (sdc == 1){
                String[] scriptRecord = getRecordFromStmtRecord("SDC", project, mapMethod, record, extractor);
                allScriptRecords.add(scriptRecord);
                String commitId2 = extractor.getColVal(record, COMMIT_ID);
                String srcFilePath2 = extractor.getColVal(record, FILE_PATH);
                int srcStartPos = Integer.parseInt(extractor.getColVal(record, SRC_START_POS));
                int dstStartPos = Integer.parseInt(extractor.getColVal(record, DST_START_POS));
                String stmtType = extractor.getColVal(record, STMT_TYPE);
                if (!commitId2.equals(commitId) || !srcFilePath2.equals(filePath)) {
                    commitId = commitId2;
                    filePath = srcFilePath2;
                    analysis = performAnalysis(project, commitId, mapMethod, filePath);
                }
                String url = MyConfig.getCommitUrl(project, commitId);
                ret.add("===============================================");
                ret.add(mapMethod);
                ret.add(url);
                ret.add(filePath);
                ret.add("\n");
                StmtEditActionGroup group = getStmtEditActionGroup(analysis, srcStartPos, dstStartPos, stmtType);
                ret.add(group.toString());
                ret.add("\n");
                count ++;

                addRecordIdxToMap(mapMethod, project, index);
            }
            index ++;
            if (count == 20)
                break;
        }
        return ret;
    }

    private static List<String> generateMMTCCheckScript(String project, String mapMethod, List<String[]> allScriptRecords)
            throws Exception{
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "ALL");
        CsvDataExtractor extractor = new CsvDataExtractor(csvPath, StmtRecord.getHeaders());
        List<String[]> records = extractor.getRecords();

        List<String> ret = new ArrayList<>();
        int count = 0;
        int index = 0;

        EvalCommitAnalysisWithEdit analysis = null;
        String commitId = null;
        String filePath = null;
        for (String[] record: records){
            if (hasBeenGeneratedAsScript(mapMethod, project, index)){
                index ++;
                continue;
            }
            double mmtc = extractor.getColDoubleVal(record, MMTC);
            if (mmtc == 1){
                String[] scriptRecord = getRecordFromStmtRecord("MMTC", project, mapMethod, record, extractor);
                allScriptRecords.add(scriptRecord);
                String commitId2 = extractor.getColVal(record, COMMIT_ID);
                String srcFilePath2 = extractor.getColVal(record, FILE_PATH);
                int srcStartPos = Integer.parseInt(extractor.getColVal(record, SRC_START_POS));
                int dstStartPos = Integer.parseInt(extractor.getColVal(record, DST_START_POS));
                String stmtType = extractor.getColVal(record, STMT_TYPE);
                if (!commitId2.equals(commitId) || !srcFilePath2.equals(filePath)) {
                    commitId = commitId2;
                    filePath = srcFilePath2;
                    analysis = performAnalysis(project, commitId, mapMethod, filePath);
                }
                String url = MyConfig.getCommitUrl(project, commitId);
                ret.add("===============================================");
                ret.add(mapMethod);
                ret.add(url);
                ret.add(filePath);
                ret.add("\n");
                StmtEditActionGroup group = getStmtEditActionGroup(analysis, srcStartPos, dstStartPos, stmtType);
                ret.add(group.toString());
                ret.add("\n");
                count ++;

                addRecordIdxToMap(mapMethod, project, index);
            }
            index ++;
            if (count == 20)
                break;
        }
        return ret;
    }

    private static StmtEditActionGroup getStmtEditActionGroup(EvalCommitAnalysisWithEdit analysis, int srcStartPos, int dstStartPos,
                                                              String stmtType){
        EvalComparisonResultWithEdit result = analysis.getOriginResultList().get(0);
        List<StmtEditActionGroup> groups = result.getStmtEditGroupList();
        for (StmtEditActionGroup g: groups){
            int curSrcStartPos = g.getSrcStmt() != null ? g.getSrcStmt().getPos() : -1;
            int curDstStartPos = g.getDstStmt() != null ? g.getDstStmt().getPos() : -1;
            if (curSrcStartPos == srcStartPos && curDstStartPos == dstStartPos && stmtType.equals(g.getStmtType()))
                return g;
        }
        throw new RuntimeException("cannot find the context");
    }

    private static EvalCommitAnalysisWithEdit performAnalysis(String project, String commitId, String mapMethod, String filePath){
        Set<String> filePathToAnalyze = new HashSet<>();
        filePathToAnalyze.add(filePath);
        EvalCommitAnalysisWithEdit commitAnalysis = new EvalCommitAnalysisWithEdit(project, commitId, mapMethod, filePathToAnalyze);
        return commitAnalysis;
    }

    private static String[] getRecordFromStmtRecord(String metric, String project, String mapMethod,
                                                    String[] record, CsvDataExtractor extractor){
        List<String> ret = new ArrayList<>();
        ret.add(metric);
        ret.add(project);
        ret.add(mapMethod);
        String[] headers = {
                "commitId", "srcFilePath", "stmtType", "stmtChangeType", "srcStartLine",
                "dstStartLine", "edits", "MMTC", "SDC", "CEC"
        };
        for (String header: headers){
            ret.add(extractor.getColVal(record, header));
        }
        ret.add("");
        return ret.toArray(new String[ret.size()]);
    }

    private static String[] getScriptRecordHeaders(){
        String[] headers = {
                "metric", "project", "mapMethod", "commitId", "srcFilePath", "stmtType", "stmtChangeType", "srcStartLine",
                "dstStartLine", "edits", "MMTC", "SDC", "CEC", "Reason"
        };
        return headers;

    }
}
