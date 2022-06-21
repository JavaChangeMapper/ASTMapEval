package cs.zju.stm.edit.parent;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.utils.Pair;

import java.util.List;

public class ParentMatchSimCalculator {
    private ITree srcStmt;
    private ITree dstStmt;
    private MappingStore newMs;

    public ParentMatchSimCalculator(MappingStore newMs, ITree srcStmt, ITree dstStmt){
        this.newMs = newMs;
        this.srcStmt = srcStmt;
        this.dstStmt = dstStmt;
    }

    public double parentMatchScore(){
        if (newMs == null)
            return 1.0;
        return parentMatchScore(srcStmt, dstStmt);
    }

    private double parentMatchScore(ITree srcStmt, ITree dstStmt){
        if (srcStmt.isRoot() && dstStmt.isRoot())
            return 1;
        ITree srcParentStmt = srcStmt.getParent();
        if (newMs.isSrcMapped(srcParentStmt)){
            ITree dstParentStmt = dstStmt.getParent();
            ITree mappedDstParent = newMs.getDstForSrc(srcParentStmt);
            if (dstParentStmt == mappedDstParent) {
                return 1;
            }
        }
        return 0;
    }

    public double ancestorMatchBoolScore(){
        if (parentMatchScore() == 0) {
            return 0;
        }
        List<ITree> srcParents = srcStmt.getParents();
        List<ITree> dstParents = dstStmt.getParents();
        if (srcParents.size() != dstParents.size())
            return 0;
        for (int i = 0; i < srcParents.size(); i++){
            if (parentMatchScore(srcParents.get(i), dstParents.get(i)) == 0) {
                return 0;
            }
        }
        return 1;
    }

    public double parentMatchContinuousScore(){
        List<ITree> srcParents = srcStmt.getParents();
        List<ITree> dstParents = dstStmt.getParents();

        for (int i = 0; i < srcParents.size(); i++){
            ITree srcP = srcParents.get(i);
            ITree mappedP = newMs.getDstForSrc(srcP);
            if (mappedP == null)
                continue;
            int indexOfP = dstParents.indexOf(mappedP);
            if (indexOfP != -1)
                return 2.0 / (i + indexOfP + 2);
        }
        throw new RuntimeException("Cannot find mapped ancestor, possible?");
    }

    public Pair<ITree, ITree> locateAncestorWithParentNotMatch(){
        if (parentMatchScore() == 0)
            return new Pair<>(srcStmt, dstStmt);
        List<ITree> srcParents = srcStmt.getParents();
        List<ITree> dstParents = dstStmt.getParents();
        for (int i = 0; i < srcParents.size(); i++){
            if (parentMatchScore(srcParents.get(i), dstParents.get(i)) == 0)
                return new Pair<>(srcParents.get(i), dstParents.get(i));
        }
        return null;
    }
}
