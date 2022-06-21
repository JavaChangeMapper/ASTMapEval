package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.edit.parent.ParentMatchSimCalculator;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.TokenRange;
import cs.zju.stm.edit.tokenedit.reverseorder.AlignStmtCalculator;
import cs.zju.treeutils.CheckJDTNodeType;
import cs.zju.treeutils.RangeCalculation;


import java.util.*;

public class StmtEditActionGroup {
    private ITree srcStmt;
    private ITree dstStmt;
    private boolean isMoved = false;
    private boolean isAligned = false;
    private AlignStmtCalculator.AlignStmts alignSiblings;
    private List<TokenEditAction> actions;
    private List<String> srcTokens;
    private List<String> dstTokens;

    private String stmtType;
    private int srcStmtStartLine = -1;
    private int dstStmtStartLine = -1;
    private boolean leftMoved = false;

    private boolean parentMatched = false;
    private int numOfSrcTokens = 0;
    private int numOfDstTokens = 0;
    private int numOfOutsideWordsMovedAndUpdate = 0;

    private int communicatedStmts;
    private List<TokenEditAction> editInconsistentWithDiff;
    private int numOfSameTokens = 0;
    private boolean hasLowQualityMove = false;

    public StmtEditActionGroup(ITree srcStmt, ITree dstStmt, MappingStore stmtMs,
                               Map<ITree, List<TokenEditAction>> srcStmtEditMap,
                               Map<ITree, List<TokenEditAction>> dstStmtEditMap){
        this.srcStmt = srcStmt;
        this.dstStmt = dstStmt;
        if (srcStmt != null && dstStmt != null) {
            ParentMatchSimCalculator calculator1 = new ParentMatchSimCalculator(stmtMs, srcStmt, dstStmt);
            if (calculator1.ancestorMatchBoolScore() == 0.0)
                isMoved = true;
            if (calculator1.parentMatchScore() == 1.0)
                parentMatched = true;
            ITree srcParent = srcStmt.getParent();
            ITree dstParent = dstStmt.getParent();
            AlignStmtCalculator calculator2 = new AlignStmtCalculator(stmtMs, srcParent, dstParent);
            calculator2.interSiblingStmtMove();
            alignSiblings = calculator2.getAlignStmtGroup(srcStmt);
        }
        if (srcStmt != null)
            stmtType = CheckJDTNodeType.getITreeNodeTypeName(srcStmt);
        else
            stmtType = CheckJDTNodeType.getITreeNodeTypeName(dstStmt);
        if (srcStmt != null)
            actions = srcStmtEditMap.get(srcStmt);
        else
            actions = dstStmtEditMap.get(dstStmt);
        isAligned = alignSiblings != null;
        isMoved = isMoved || alignSiblings != null;
        communicatedStmts = calCommunicatedStmts(stmtMs);
    }

    public Set<TokenRange> getChangedSrcRanges(){
        Set<TokenRange> ret = new HashSet<>();
        for (TokenEditAction action: actions){
            if (action.getSrcRanges() != null)
                ret.addAll(action.getSrcRanges());
        }
        return ret;
    }

    public Set<TokenRange> getChangedDstRanges(){
        Set<TokenRange> ret = new HashSet<>();
        for (TokenEditAction action: actions){
            if (action.getDstRanges() != null)
                ret.addAll(action.getDstRanges());
        }
        return ret;
    }

    public void calStmtInfo(TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap, MappingStore stmtMs){
        List<TokenRange> srcTokenRanges = srcTreeWordMap.getTokenRangesOfNode(srcStmt);
        List<TokenRange> dstTokenRanges = dstTreeWordMap.getTokenRangesOfNode(dstStmt);
        this.srcTokens = srcTreeWordMap.getTokensByRanges(srcTokenRanges);
        this.dstTokens = dstTreeWordMap.getTokensByRanges(dstTokenRanges);
        if (srcStmt != null) {
            this.srcStmtStartLine = srcTreeWordMap.getLineRangeOfStmt(srcStmt).first;
            this.numOfSrcTokens = srcTokens.size();
        }
        if (dstStmt != null) {
            this.dstStmtStartLine = dstTreeWordMap.getLineRangeOfStmt(dstStmt).first;
            this.numOfDstTokens = dstTokenRanges.size();
            for (TokenRange range: dstTokenRanges){
                TokenRange originSrcRange = TreeTokensMap.findMappedRange(stmtMs, range, false,
                        srcTreeWordMap, dstTreeWordMap);
                ITree stmt = srcTreeWordMap.getStmtOfTokenRange(originSrcRange);
                if (originSrcRange != null && stmt == srcStmt){
                    String srcWord = srcTreeWordMap.getTokenByRange(originSrcRange);
                    String dstWord = dstTreeWordMap.getTokenByRange(range);
                    if (srcWord.equals(dstWord))
                        numOfSameTokens++;
                }

                if (stmt != srcStmt && originSrcRange != null){
                    String srcWord = srcTreeWordMap.getTokenByRange(originSrcRange);
                    String dstWord = dstTreeWordMap.getTokenByRange(range);
                    if (!srcWord.equals(dstWord))
                        this.numOfOutsideWordsMovedAndUpdate += 1.0;
                }
            }
        }
    }


//    private void calAlignment(){
//        if (alignGroup != null){
//            if (alignGroup.leftStmts.size() < alignGroup.rightStmts.size())
//                leftMoved = true;
//            else if (alignGroup.leftStmts.size() > alignGroup.rightStmts.size())
//                leftMoved = false;
//            else {
//                boolean leftModified = false;
//                boolean rightModified = false;
//                for (ITree stmt: alignGroup.leftStmts) {
//                    if (srcStmtEditMap.containsKey(stmt)) {
//                        if (srcStmtEditMap.get(stmt).size() > 0)
//                            leftModified = true;
//                    }
//                }
//                for (ITree stmt: alignGroup.rightStmts){
//                    if (srcStmtEditMap.containsKey(stmt))
//                        if (srcStmtEditMap.get(stmt).size() > 0)
//                            rightModified = true;
//                }
//                if (!rightModified)
//                    leftMoved = true;
//                else if (!leftModified)
//                    leftMoved = false;
//                else
//                    leftMoved = true;
//            }
//
//        }
//    }

    public boolean isMoved() {
        return isMoved;
    }

    public boolean isAligned() {
        return isAligned;
    }

    public boolean isParentMatched() {
        return parentMatched;
    }

    public int getNumOfSrcTokens() {
        return numOfSrcTokens;
    }

    public int getNumOfDstTokens() {
        return numOfDstTokens;
    }

    public ITree getSrcStmt() {
        return srcStmt;
    }

    public ITree getDstStmt() {
        return dstStmt;
    }

    public int getSrcStmtStartLine() {
        return srcStmtStartLine;
    }

    public int getDstStmtStartLine() {
        return dstStmtStartLine;
    }

    public int getCommunicatedStmts() {
        return communicatedStmts;
    }

    public int getNumOfSameTokens() {
        return numOfSameTokens;
    }

    public void setHasLowQualityMove(){
        this.hasLowQualityMove = true;
    }

    public boolean isHasLowQualityMove(){
        return hasLowQualityMove;
    }

    public List<TokenEditAction> calEditsInconsistentWithDiff(Set<Integer> srcUnchangedLines,
                                                              Set<Integer> dstUnchangedLines,
                                                              RangeCalculation srcRc,
                                                              RangeCalculation dstRc){
        List<TokenEditAction> actions = getActions();
        List<TokenEditAction> ret = new ArrayList<>();
        if (actions != null && actions.size() > 0){
            for (TokenEditAction action: actions){
                List<TokenRange> srcRanges = action.getSrcRanges();
                boolean inconsistent = false;
                if (srcRanges != null){
                    for (TokenRange range: srcRanges){
                        int line = srcRc.getLineNumberOfPos(range.first);
                        if (srcUnchangedLines.contains(line)){
                            inconsistent = true;
                            break;
                        }
                    }
                }
                if (inconsistent) {
                    ret.add(action);
                    continue;
                }
                List<TokenRange> dstRanges = action.getDstRanges();
                if (dstRanges != null){
                    for (TokenRange range: dstRanges){
                        int line = dstRc.getLineNumberOfPos(range.first);
                        if (dstUnchangedLines.contains(line)){
                            inconsistent = true;
                            break;
                        }
                    }
                }
                if (inconsistent) {
                    ret.add(action);
                }
            }
        }
        this.editInconsistentWithDiff = ret;
        return ret;
    }

    public List<TokenEditAction> getEditInconsistentWithDiff() {
        return editInconsistentWithDiff;
    }

    private int calCommunicatedStmts(MappingStore ms){
        Set<ITree> relatedSrcStmts = new HashSet<>();
        int count = 0;
        for (TokenEditAction action: actions){
            if (action instanceof TokenMoveFromStmt){
                ITree src = action.getSrcStmt();
                relatedSrcStmts.add(src);
            }
        }
        count += relatedSrcStmts.size();
        for (TokenEditAction action: actions){
            if (action instanceof TokenMoveToStmt){
                ITree dst = action.getDstStmt();
                ITree mappedSrc = ms.getSrcForDst(dst);
                if (mappedSrc != null && !relatedSrcStmts.contains(mappedSrc))
                    count ++;
            }
        }
        return count;
    }

    public void checkMoveFromAncestorStmts(Set<ITree> movedSrcStmts){
        if (srcStmt == null)
            return;
        if (isMoved)
            return;
        for (ITree stmt: srcStmt.getParents()){
            if (movedSrcStmts.contains(stmt)) {
                isMoved = true;
                break;
            }
        }
    }

    private int getNumOfEditWithType(String type){
        int count = 0;
        if (actions != null) {
            for (TokenEditAction action : actions) {
                if (action.getType().equals(type))
                    count ++;
            }
        }
        return count;
    }

    public int getNumOfInsert(){
        return getNumOfEditWithType("INSERT");
    }

    public int getNumOfDelete(){
        return getNumOfEditWithType("DELETE");
    }

    public int getNumOfUpdate(){
        return getNumOfEditWithType("UPDATE");
    }

    public int getNumOfInStmtMove(){
        return getNumOfEditWithType("IN STMT MOVE");
    }

    public int getNumOfMoveFrom(){
        return getNumOfEditWithType("MOVE FROM");
    }

    public int getNumOfMoveTo(){
        return getNumOfEditWithType("MOVE TO");
    }

    public String getStmtChangeType() {
        if (srcStmt == null)
            return "Insert";
        else if (dstStmt == null)
            return "Delete";
        boolean isUpdate = actions.size() > 0;
        if ((isMoved || isAligned) && isUpdate) {
            return "Move & Update";
        } else if (isUpdate) {
            return "Update";
        } else if (isMoved || isAligned) {
            return "Move";
        }
        return null;
    }

    public List<TokenEditAction> getActions() {
        return actions;
    }

    public String getStmtType() {
        return stmtType;
    }

    public boolean isIdenticalActions(StmtEditActionGroup group){
        if (actions.size() != group.actions.size())
            return false;
        for (int i = 0; i < actions.size(); i++){
            if (!actions.get(i).isIdenticalTo(group.actions.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String ret = "--------------------------------------\n";
        String stmtChangeType = getStmtChangeType();

        if (stmtChangeType == null){
            ret += "No Edit: " + stmtType;
            ret += " (Src:" + srcStmtStartLine + " => " + "Dst:" + dstStmtStartLine + ")\n";
        }

        if (stmtChangeType != null){
            ret += stmtChangeType + ": " + stmtType;
            if (stmtChangeType.equals("Delete")){
                ret += " (Src:" + srcStmtStartLine + ")";
            } else if (stmtChangeType.equals("Add")){
                ret += " (Dst:" + dstStmtStartLine + ")";
            } else {
                ret += " (Src:" + srcStmtStartLine + " => " + "Dst:" + dstStmtStartLine + ")";
            }
            ret += "\n";
        }

        if (actions != null) {
            for (TokenEditAction action : actions) {
                ret += action.getName() + "\n";
            }
        }

        return ret;
    }
}
