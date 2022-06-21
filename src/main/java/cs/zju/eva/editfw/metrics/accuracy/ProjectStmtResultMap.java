package cs.zju.eva.editfw.metrics.accuracy;

import cs.zju.eva.editfw.StmtEvaluationMeasures;
import cs.zju.eva.utils.FileRevision;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.eva.statistics.StatisticCalculator;
import cs.zju.utils.PathResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectStmtResultMap {
    private Map<FileRevision, FileRevisionStmts> revStmtsMap = new HashMap<>();

    private static final String COMMIT_ID = "commitId";
    private static final String FILE_PATH = "srcFilePath";
    private static final String STMT_TYPE = "stmtType";
    private static final String STMT_CHANGE_TYPE = "changeType";
    private static final String SRC_START_LINE = "srcStartLine";
    private static final String DST_START_LINE = "dstStartLine";
    private static final String EDIT = "edits";
    private static final String MOVE_FROM_EDIT = "numOfMoveFrom";
    private static final String MOVE_TO_EDIT = "numOfMoveTo";
    private static final String SRC_STMT_TOKEN_COUNT = "srcStmtWordCount";
    private static final String DST_STMT_TOKEN_COUNT = "dstStmtWordCount";
    private static final String COMMUNICATIONS = "communicatedStmts";
    private static final String SAME_WORD = "numOfSameWords";
    private static final String EDIT_IWD = "numOfEditsIWD";
    private static final String HAS_LOW_QUALITY_MOVE = "hasLowQualityMove";

    private static final String SRC_START_POS = "srcStartPos";
    private static final String DST_START_POS = "dstStartPos";

    private double lowerMADRatio;

    public ProjectStmtResultMap(String project, String mapMethod) throws Exception {
        String stmtCsvPath = PathResolver.getCombineStmtResultCsvPath(project, mapMethod);
        CsvDataExtractor extractor = new CsvDataExtractor(stmtCsvPath, StmtEvaluationMeasures.getHeaders());
        List<String[]> records = extractor.getRecords();

        for (String[] record: records){
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            String stmtType = extractor.getColVal(record, STMT_TYPE);
            String stmtChangeType = extractor.getColVal(record, STMT_CHANGE_TYPE);
            int srcStartLine = Integer.parseInt(extractor.getColVal(record, SRC_START_LINE));
            int dstStartLine = Integer.parseInt(extractor.getColVal(record, DST_START_LINE));
            int srcStartPos = Integer.parseInt(extractor.getColVal(record, SRC_START_POS));
            int dstStartPos = Integer.parseInt(extractor.getColVal(record, DST_START_POS));
            int edit = Integer.parseInt(extractor.getColVal(record, EDIT));
            int moveFromEdit = Integer.parseInt(extractor.getColVal(record, MOVE_FROM_EDIT));
            int moveToEdit = Integer.parseInt(extractor.getColVal(record, MOVE_TO_EDIT));

            boolean badCommunication = Integer.parseInt(extractor.getColVal(record, COMMUNICATIONS)) >= 2;
            int numOfSameWord = Integer.parseInt(extractor.getColVal(record, SAME_WORD));
            int lengthOfSrcStmt = Integer.parseInt(extractor.getColVal(record, SRC_STMT_TOKEN_COUNT));
            int lengthOfDstStmt = Integer.parseInt(extractor.getColVal(record, DST_STMT_TOKEN_COUNT));
            boolean inconsistentWithGitDiff = Integer.parseInt(extractor.getColVal(record, EDIT_IWD)) > 0;
            boolean hasLowQualityMove = Integer.parseInt(extractor.getColVal(record, HAS_LOW_QUALITY_MOVE)) > 0;


            FileRevision fr = new FileRevision(commitId, filePath);
            StmtRecord stmt = new StmtRecord(fr, srcStartLine, dstStartLine, srcStartPos, dstStartPos,
                    stmtChangeType, stmtType, edit,
                    moveFromEdit + moveToEdit, badCommunication, hasLowQualityMove, inconsistentWithGitDiff,
                    Math.min(lengthOfSrcStmt, lengthOfDstStmt), numOfSameWord);
            if (!revStmtsMap.containsKey(fr))
                revStmtsMap.put(fr, new FileRevisionStmts());

            revStmtsMap.get(fr).addStmt(stmt);
        }

        calLowerMADRatioList();
    }

    public double getLowerMADRatio() {
        return lowerMADRatio;
    }

    private void calLowerMADRatioList(){
        List<Double> ratios = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            FileRevisionStmts curStmts = revStmtsMap.get(fr);
            ratios.addAll(curStmts.getSameTokenRatioList());
        }
        double[] ratioArray = new double[ratios.size()];
        for (int i = 0; i < ratios.size(); i++){
            ratioArray[i] = ratios.get(i);
        }
        lowerMADRatio = StatisticCalculator.getLowerMAD(ratioArray);
    }

    public void doCompare(ProjectStmtResultMap stmtResultMap){
        for (FileRevision fr: revStmtsMap.keySet()){
            FileRevisionStmts curStmts = revStmtsMap.get(fr);
            FileRevisionStmts comparisonStmts = stmtResultMap.revStmtsMap.get(fr);
            curStmts.doCompare(comparisonStmts);
        }
    }

    public List<StmtRecord> getStmtsTestedByCEC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            ret.addAll(revStmtsMap.get(fr).getAllContextsTestedByCEC());
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsByCEC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            ret.addAll(revStmtsMap.get(fr).getSuspiciousStmtsByCEC(getLowerMADRatio()));
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsBySDC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            ret.addAll(revStmtsMap.get(fr).getSuspiciousStmtsBySDC());
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsByMMTC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            ret.addAll(revStmtsMap.get(fr).getSuspiciousStmtsByMMTC());
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmts(){
        List<StmtRecord> ret = new ArrayList<>();
        for (FileRevision fr: revStmtsMap.keySet()){
            ret.addAll(revStmtsMap.get(fr).getAllSuspiciousStmts(lowerMADRatio));
        }
        return ret;
    }
}
