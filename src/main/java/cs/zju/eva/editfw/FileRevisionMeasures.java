package cs.zju.eva.editfw;

import cs.zju.framework.edit.EvalComparisonResultWithEdit;

import java.util.ArrayList;
import java.util.List;

public class FileRevisionMeasures {
    private String mapMethod;
    private String project;
    private String commitId;
    private String srcFilePath;
    private double mapTime;
    private double treeActionGenerateTime;
    private double wordActionGenerateTime;
    private int numOfSrcTreeNode;
    private int numOfDstTreeNode;
    private int srcToDstAmbiguousMappingCount;
    private int dstToSrcAmbiguousMappingCount;
    private int numOfActionsWithAmbiguity;
    private int numOfActionsWithoutAmbiguity;
    private int numOfEdits;
    private int numOfEditedStmts;
    private int numOfModifiedStmts;
    private int numOfMovedStmts;
    private int lowQualityMapCount;
    private int numOfEditedStmtsWithoutBlock;
    private int numOfModifiedStmtsWithoutBlock;
    private int numOfMovedStmtsWithoutBlock;

    public String getMapMethod() {
        return mapMethod;
    }

    public String getProject() {
        return project;
    }

    public String getCommitId() {
        return commitId;
    }

    public FileRevisionMeasures(EvalComparisonResultWithEdit result){
        this.project = result.getProject();
        this.mapMethod = result.getMapMethod();
        this.commitId = result.getCommitId();
        this.srcFilePath = result.getSrcFilePath();
        this.mapTime = result.getMapTime();
        this.treeActionGenerateTime = result.getTreeActionGenerateTime();
        this.wordActionGenerateTime = result.getWordActionGenerateTime();
        this.numOfSrcTreeNode = (int) result.getSrcTreeNodeCount();
        this.numOfDstTreeNode = (int) result.getDstTreeNodeCount();
        this.srcToDstAmbiguousMappingCount = result.getSrcToDstAmbiguosCount();
        this.dstToSrcAmbiguousMappingCount = result.getDstToSrcAmbiguousCount();
        this.numOfActionsWithAmbiguity = (int) result.getAmbiguousActionCount();
        this.numOfActionsWithoutAmbiguity = (int) result.getUnambiguousActionCount();
        this.numOfEdits = (int) result.getWordEdits();
        this.numOfEditedStmts = (int) result.getEditedStmtCount(false);
        this.numOfMovedStmts = (int) result.getMoveStmtCount(false);
        this.lowQualityMapCount = (int) result.getMeaninglessMoveCount();
        this.numOfModifiedStmts = (int) result.getChangedStmtCount(false);
        this.numOfEditedStmtsWithoutBlock = (int) result.getEditedStmtCount(true);
        this.numOfModifiedStmtsWithoutBlock = (int) result.getChangedStmtCount(true);
        this.numOfMovedStmtsWithoutBlock = (int) result.getMoveStmtCount(true);
    }

    public String[] toCsvRecord(){
        if (numOfSrcTreeNode == 0 || numOfDstTreeNode == 0)
            return null;

        List<String> record = new ArrayList<>();
        record.add(mapMethod);
        record.add(project);
        record.add(commitId);
        record.add(srcFilePath);
        record.add(Double.toString(mapTime));
        record.add(Double.toString(treeActionGenerateTime));
        record.add(Double.toString(wordActionGenerateTime));
        record.add(Integer.toString(numOfSrcTreeNode));
        record.add(Integer.toString(numOfDstTreeNode));
        record.add(Integer.toString(srcToDstAmbiguousMappingCount));
        record.add(Integer.toString(dstToSrcAmbiguousMappingCount));
        record.add(Integer.toString(numOfActionsWithAmbiguity));
        record.add(Integer.toString(numOfActionsWithoutAmbiguity));
        record.add(Integer.toString(numOfEdits));
        record.add(Integer.toString(numOfEditedStmts));
        record.add(Integer.toString(numOfMovedStmts));
        record.add(Integer.toString(numOfModifiedStmts));
        record.add(Integer.toString(lowQualityMapCount));
        record.add(Integer.toString(numOfEditedStmtsWithoutBlock));
        record.add(Integer.toString(numOfMovedStmtsWithoutBlock));
        record.add(Integer.toString(numOfModifiedStmtsWithoutBlock));
        return record.toArray(new String[record.size()]);
    }

    public static String[] getHeaders(){
        String[] headers = {
                "mapMethod", "project", "commitId", "srcFilePath", "mapTime",
                "treeActionTime", "wordActionTime",
                "numOfSrcNode", "numOfDstNode", "srcToDstAmbiguity", "dstToSrcAmbiguity",
                "numOfAmbiguousAction", "numOfNonAmbiguousCount", "numOfEdits",
                "numOfEditedStmts", "numOfMoveStmts",
                "numOfModifiedStmts", "lowQualityMapCount", "numOfEditedStmtsWithoutBlock",
                "numOfMoveStmtsWithoutBlock", "numOfModifiedStmtsWithoutBlock"
        };
        return headers;
    }
}
