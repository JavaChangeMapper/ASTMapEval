package cs.zju.treeutils;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.utils.Pair;

import java.util.List;

public class GumTreeEditAction {
    private String srcFilePath;
    private String dstFilePath;
    private GumTreeEditType editType;
    private ITree srcNode = null;
    private ITree dstNode = null;
    private Action action;

    private RangeCalculation srcRc;
    private RangeCalculation dstRc;
    private Pair<Integer, Integer> srcLineRange = null;
    private Pair<Integer, Integer> dstLineRange = null;
    private Pair<Integer, Integer> srcOffsetRange = null;
    private Pair<Integer, Integer> dstOffsetRange = null;

    public GumTreeEditAction(Action action, MappingStore ms,
                             String srcFilePath, String dstFilePath,
                             RangeCalculation srcRc, RangeCalculation dstRc){
        this.action = action;
        extractActionInfo(ms);
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;
        this.srcRc = srcRc;
        this.dstRc = dstRc;
        calCodeRange();
    }

    private GumTreeEditAction(String srcFilePath, String dstFilePath,
                              RangeCalculation srcRc, RangeCalculation dstRc){
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;
        this.srcRc = srcRc;
        this.dstRc = dstRc;
    }

    private void calCodeRange(){
        srcLineRange = srcRc.getLineRangeOfNode(srcNode);
        dstLineRange = dstRc.getLineRangeOfNode(dstNode);
        if (srcNode != null)
            srcOffsetRange = new Pair<>(srcNode.getPos(), srcNode.getEndPos());
        if (dstNode != null)
            dstOffsetRange = new Pair<>(dstNode.getPos(), dstNode.getEndPos());
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public String getDstFilePath() {
        return dstFilePath;
    }

    // Create an insert action
    public static GumTreeEditAction createInsertGumTreeEditAction(ITree node, String srcFilePath,
                                                                  String dstFilePath,
                                                                  RangeCalculation srcRc,
                                                                  RangeCalculation dstRc) {
        GumTreeEditAction ret = new GumTreeEditAction(srcFilePath, dstFilePath, srcRc, dstRc);
        ret.editType = GumTreeEditType.INSERT;
        ret.srcNode = null;
        ret.dstNode = node;
        ret.calCodeRange();
        return ret;
    }

    // create a delete action
    public static GumTreeEditAction creatDeleteGumTreeEditAction(ITree node, String srcFilePath,
                                                                 String dstFilePath,
                                                                 RangeCalculation srcRc,
                                                                 RangeCalculation dstRc){
        GumTreeEditAction ret = new GumTreeEditAction(srcFilePath, dstFilePath, srcRc, dstRc);
        ret.editType = GumTreeEditType.DELETE;
        ret.dstNode = null;
        ret.srcNode = node;
        ret.calCodeRange();
        return ret;
    }

    public ITree getSrcNode() {
        return srcNode;
    }

    public ITree getDstNode() {
        return dstNode;
    }

    public GumTreeEditType getEditType() {
        return editType;
    }

    public Pair<Integer, Integer> getSrcLineRange() {
        return srcLineRange;
    }

    public Pair<Integer, Integer> getDstLineRange() {
        return dstLineRange;
    }

    public Pair<Integer, Integer> getSrcOffsetRange() {
        return srcOffsetRange;
    }

    public Pair<Integer, Integer> getDstOffsetRange() {
        return dstOffsetRange;
    }

    private void extractActionInfo(MappingStore ms){
        if (action instanceof Insert){
            editType = GumTreeEditType.INSERT;
            dstNode = action.getNode();
            srcNode = null;
        } else if (action instanceof Delete){
            editType = GumTreeEditType.DELETE;
            srcNode = action.getNode();
            dstNode = null;
        } else if (action instanceof Move){
            editType = GumTreeEditType.MOVE_TREE;
            srcNode = action.getNode();
            dstNode = ms.getDstForSrc(srcNode);
        } else {
            if (!(action instanceof Update))
                throw new RuntimeException("Unknown edit operation!");
            editType = GumTreeEditType.UPDATE;
            srcNode = action.getNode();
            dstNode = ms.getDstForSrc(srcNode);
        }
        assignIgnoreType();
    }

    public List<Integer> getPreviousFileContentLineNumbers(){
        return srcRc.getContentLineNumbersFromRange(srcLineRange);
    }

    public List<Integer> getNextFileContentLineNumbers(){
        return dstRc.getContentLineNumbersFromRange(dstLineRange);
    }

    private static boolean checkIgnoreNodeType(ITree node){
        ITree tempNode = node;
        while (tempNode != null) {
            if (CheckJDTNodeType.isImportDec(node))
                return true;
            if (CheckJDTNodeType.isJavaDoc(node))
                return true;
            if (CheckJDTNodeType.isPackageDec(node))
                return true;
            tempNode = tempNode.getParent();
        }
        return false;
    }

    private void assignIgnoreType() {
        if ((dstNode != null && checkIgnoreNodeType(dstNode)) ||
                (srcNode != null && checkIgnoreNodeType(srcNode)))
            this.editType = GumTreeEditType.SHOULD_IGNORE;
    }

    public String toString(String srcFileContent, String destFileContent) {
        String ret = "Type: " + editType + "\n";
        if (srcNode != null){
            ret += "src: \n";
            ret += srcNode.toString();
            ret += "NodeType: " + CheckJDTNodeType.getITreeNodeTypeName(srcNode) + "\n";
            ret += "[" + getPreviousFileContentLineNumbers() + "]\n";
            ret += "Position: " + "[" + srcNode.getPos() + ", " + srcNode.getEndPos() + "]\n";
            ret += srcFileContent.substring(srcNode.getPos(), srcNode.getEndPos()) + "\n";
        }
        if (dstNode != null){
            ret += "dest: \n";
            ret += dstNode.toString();
            ret += "NodeType: " + CheckJDTNodeType.getITreeNodeTypeName(dstNode) + "\n";
            ret += "[" + getNextFileContentLineNumbers() + "]\n";
            ret += "Position: " + "[" + dstNode.getPos() + ", " + dstNode.getEndPos() + "]\n";
            ret += destFileContent.substring(dstNode.getPos(), dstNode.getEndPos()) + "\n";
        }
        return ret;
    }
}
