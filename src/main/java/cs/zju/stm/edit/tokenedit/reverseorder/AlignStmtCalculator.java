package cs.zju.stm.edit.tokenedit.reverseorder;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.treeutils.CheckJDTNodeType;

import java.util.*;


/**
 * Statement 之间互换位置可能影响程序的执行，因此需要查看这种互换位置
 */

public class AlignStmtCalculator {
    private ITree srcParent;
    private ITree dstParent;
    private MappingStore ms;

    private List<LcsSiblingMove> moveList;

    public AlignStmtCalculator(MappingStore ms, ITree srcParent, ITree dstParent){
        this.srcParent = srcParent;
        this.dstParent = dstParent;
        this.ms = ms;
    }

    private boolean needCalInterSiblingStmtGroupMove(){
        if (srcParent == null || dstParent == null)
            return false;
        if (ms.getDstForSrc(srcParent) != dstParent)
            return false;
        for (ITree t: srcParent.getChildren()){
            if (CheckJDTNodeType.getITreeNodeTypeName(t).endsWith("Declaration"))
                return false;
            if (!CheckJDTNodeType.isStatementNode(t))
                return false;
        }
        for (ITree t: dstParent.getChildren()){
            if (CheckJDTNodeType.getITreeNodeTypeName(t).endsWith("Declaration"))
                return false;
            if (!CheckJDTNodeType.isStatementNode(t))
                return false;
        }
        return true;
    }

    public double interSiblingStmtMove(){
        if (needCalInterSiblingStmtGroupMove()){
            List<ITree> srcStmts = srcParent.getChildren();
            List<ITree> dstStmts = dstParent.getChildren();
            // Use as cache
            Map<ITree, Integer> dstStmtIdxMap = new HashMap<>();
            for (int i = 0; i < dstStmts.size(); i++){
                dstStmtIdxMap.put(dstStmts.get(i), i);
            }
            List<Integer> dstIndexesOfSrc = new ArrayList<>();
            for (ITree srcT: srcStmts) {
                ITree dstT = ms.getDstForSrc(srcT);
                if (!dstStmtIdxMap.containsKey(dstT))
                    continue;
                int dstIndex = dstStmtIdxMap.get(dstT);
                dstIndexesOfSrc.add(dstIndex);
            }
//            ReverseListOrder rm = new ReverseListOrder(dstIndexesOfSrc, dstStmts.size());
            LcsReverseOrder rm = new LcsReverseOrder(dstIndexesOfSrc, dstStmts.size());
            moveList = rm.getReverseOrderMoveList();
            return rm.getMoveEdits();
        }
        return 0;
    }

    public List<AlignStmts> getAlignedSrcStmtGroups(){
//        List<AlignStmtGroup> ret = new ArrayList<>();
        List<AlignStmts> ret = new ArrayList<>();
        if (needCalInterSiblingStmtGroupMove()) {
            List<ITree> dstStmts = dstParent.getChildren();
            if (moveList != null) {
                for (LcsSiblingMove move : moveList) {
//                    List<Integer> leftList = move.leftList;
//                    List<Integer> rightList = move.rightList;
//                    List<ITree> srcStmtsLeft = getMappedSrcStmts(dstStmts, leftList);
//                    List<ITree> srcStmtsRight = getMappedSrcStmts(dstStmts, rightList);
                    List<Integer> movedList = move.movedTargetIdxes;
                    List<ITree> movedStmts = getMappedSrcStmts(dstStmts, movedList);
                    String moveType = move.moveType;
//                    ret.add(new AlignStmtGroup(srcStmtsLeft, srcStmtsRight));
                    ret.add(new AlignStmts(movedStmts, moveType));
                }
            }
        }
        return ret;
    }

    public AlignStmts getAlignStmtGroup(ITree srcStmt){
//        List<AlignStmtGroup> groups = getAlignedSrcStmtGroups();
        List<AlignStmts> groups = getAlignedSrcStmtGroups();
//        for (AlignStmtGroup g: groups){
//            if (g.allStmts.contains(srcStmt))
//                return g;
//        }
        for (AlignStmts g: groups){
            if (g.alignedStmtSet.contains(srcStmt))
                return g;
        }
        return null;
    }

    private List<ITree> getMappedSrcStmts(List<ITree> dstStmts, List<Integer> idxes){
        List<ITree> ret = new ArrayList<>();
        for (int idx: idxes){
            ITree dstStmt = dstStmts.get(idx);
            ITree srcStmt = ms.getSrcForDst(dstStmt);
            ret.add(srcStmt);
            ret.addAll(getAllSubStmts(srcStmt));
        }
        return ret;
    }

    private List<ITree> getAllSubStmts(ITree stmt){
        List<ITree> ret = new ArrayList<>();
        for (ITree t: stmt.preOrder()){
            if (t == stmt)
                continue;
            if (CheckJDTNodeType.isStatementNode(t))
                ret.add(t);
        }
        return ret;
    }

    public static class AlignStmts{
        public List<ITree> alignedStmts;
        public String moveDirection;
        public Set<ITree> alignedStmtSet;

        public AlignStmts(List<ITree> alignedStmts, String moveDirection){
             this.alignedStmts = alignedStmts;
             this.moveDirection = moveDirection;
             alignedStmtSet = new HashSet<>(alignedStmts);
        }
    }

    @Deprecated
    public static class AlignStmtGroup {
        public List<ITree> leftStmts;
        public List<ITree> rightStmts;
        public Set<ITree> allStmts;

        public AlignStmtGroup(List<ITree> leftStmts, List<ITree> rightStmts){
            this.leftStmts = leftStmts;
            this.rightStmts = rightStmts;
            this.allStmts = new HashSet<>();
            allStmts.addAll(leftStmts);
            allStmts.addAll(rightStmts);
        }

        @Override
        public String toString() {
            return "AlignStmtGroup{" +
                    "alignedStmts=" + leftStmts +
                    ", stmtsNotMoved=" + rightStmts +
                    '}';
        }
    }
}
