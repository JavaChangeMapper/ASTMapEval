package cs.zju.stm;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.treeutils.GumTreeUtil;
import cs.zju.treeutils.RangeCalculation;

import java.util.*;

public class MatchingCreator {

    private MappingStore originalMs;
    private double originRunTime;

    public MatchingCreator(ITree srcRoot, ITree dstRoot,
                           String matcherId, MappingStore supplementaryMatching) {

        long start = System.currentTimeMillis();
        originalMs = GumTreeUtil.getTreeMappings(srcRoot, dstRoot, supplementaryMatching, matcherId);
        long end = System.currentTimeMillis();
        originRunTime = end - start;
    }

    public MappingStore getOriginalMs() {
        return originalMs;
    }

    public double getOriginRunTime() {
        return originRunTime;
    }

    public void setSupplementaryMatching(MappingStore matching){
        for (Mapping m: matching){
            ITree src = m.first;
            ITree dst = m.second;
            if (originalMs.isSrcMapped(src))
                originalMs.removeMapping(src, originalMs.getDstForSrc(src));
            if (originalMs.isDstMapped(dst))
                originalMs.removeMapping(originalMs.getSrcForDst(dst), dst);
            originalMs.addMapping(src, dst);
        }
    }
}
