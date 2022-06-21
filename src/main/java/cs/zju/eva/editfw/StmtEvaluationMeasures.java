package cs.zju.eva.editfw;

import cs.zju.stm.edit.tokenedit.actions.StmtEditActionGroup;

import java.util.ArrayList;
import java.util.List;

public class StmtEvaluationMeasures {
    private String mapMethod;
    private String project;
    private String commitId;
    private String srcFilePath;
    private String stmtType;
    private String changeType;
    private int srcStartLine;
    private int dstStartLine;
    private int srcStartPos;
    private int dstStartPos;
    private int srcStmtTokenCount;
    private int dstStmtTokenCount;
    private int edits;
    private int parentMatched;
    private int moved;
    private int communicatedStmts;
    private int numOfInsert;
    private int numOfDelete;
    private int numOfMoveFrom;
    private int numOfMoveTo;
    private int numOfInStmtMove;
    private int numOfUpdate;
    private int numOfEditInconsistentWithDiff;
    private int numOfSameTokens;
    private int hasLowQualityMove;

    public String getMapMethod() {
        return mapMethod;
    }

    public String getProject() {
        return project;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public String getStmtType() {
        return stmtType;
    }

    public StmtEvaluationMeasures(StmtEditActionGroup group, String project,
                                  String mapMethod, String commitId, String srcFilePath){
        this.project = project;
        this.mapMethod = mapMethod;
        this.commitId = commitId;
        this.srcFilePath = srcFilePath;
        this.stmtType = group.getStmtType();
        this.changeType = group.getStmtChangeType();
        this.srcStartLine = group.getSrcStmtStartLine();
        this.dstStartLine = group.getDstStmtStartLine();
        this.srcStmtTokenCount = group.getNumOfSrcTokens();
        this.dstStmtTokenCount = group.getNumOfDstTokens();
        this.edits = group.getActions().size();
        this.parentMatched = group.isParentMatched() ? 1 : 0;
        this.moved = group.isMoved() ? 1 : 0;
        this.communicatedStmts = group.getCommunicatedStmts();
        this.numOfInsert = group.getNumOfInsert();
        this.numOfDelete = group.getNumOfDelete();
        this.numOfInStmtMove = group.getNumOfInStmtMove();
        this.numOfMoveFrom = group.getNumOfMoveFrom();
        this.numOfMoveTo = group.getNumOfMoveTo();
        this.numOfUpdate = group.getNumOfUpdate();
        this.numOfEditInconsistentWithDiff = group.getEditInconsistentWithDiff().size();
        this.numOfSameTokens = group.getNumOfSameTokens();
        this.hasLowQualityMove = group.isHasLowQualityMove() ? 1 : 0;

        this.srcStartPos = group.getSrcStmt() != null ? group.getSrcStmt().getPos() : -1;
        this.dstStartPos = group.getDstStmt() != null ? group.getDstStmt().getPos() : -1;
    }

    public String[] toCsvRecord(){
        List<String> record = new ArrayList<>();
        record.add(mapMethod);
        record.add(project);
        record.add(commitId);
        record.add(srcFilePath);
        record.add(stmtType);
        record.add(changeType);
        record.add(Integer.toString(srcStartLine));
        record.add(Integer.toString(dstStartLine));
        record.add(Integer.toString(srcStartPos));
        record.add(Integer.toString(dstStartPos));
        record.add(Integer.toString(srcStmtTokenCount));
        record.add(Integer.toString(dstStmtTokenCount));
        record.add(Integer.toString(edits));
        record.add(Integer.toString(parentMatched));
        record.add(Integer.toString(moved));
        record.add(Integer.toString(numOfInsert));
        record.add(Integer.toString(numOfDelete));
        record.add(Integer.toString(numOfMoveFrom));
        record.add(Integer.toString(numOfMoveTo));
        record.add(Integer.toString(numOfUpdate));
        record.add(Integer.toString(numOfInStmtMove));
        record.add(Integer.toString(communicatedStmts));
        record.add(Integer.toString(numOfSameTokens));
        record.add(Integer.toString(numOfEditInconsistentWithDiff));
        record.add(Integer.toString(hasLowQualityMove));
        return record.toArray(new String[record.size()]);
    }

    public static String[] getHeaders(){
        String[] headers = {
                "mapMethod", "project", "commitId", "srcFilePath", "stmtType",
                "changeType", "srcStartLine", "dstStartLine",
                "srcStartPos", "dstStartPos",
                "srcStmtWordCount", "dstStmtWordCount", "edits", "parentMatched", "moved",
                "numOfInsert", "numOfDelete", "numOfMoveFrom", "numOfMoveTo", "numOfUpdate", "numOfInStmtMove",
                "communicatedStmts", "numOfSameWords", "numOfEditsIWD", "hasLowQualityMove"
        };
        return headers;
    }
}
