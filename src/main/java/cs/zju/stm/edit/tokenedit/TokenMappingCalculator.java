package cs.zju.stm.edit.tokenedit;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;
import cs.zju.utils.Pair;

import java.util.*;

public class TokenMappingCalculator {
    private TokenRange srcTokenRange;
    private TokenRange dstTokenRange;
    private TreeTokensMap srcTreeWordMap;
    private TreeTokensMap dstTreeWordMap;
    private ITree srcStmt;
    private ITree dstStmt;
    private ITree assumeSrcStmt = null;
    private ITree assumeDstStmt = null;
    private ITree srcWordTree;
    private ITree dstWordTree;
    private List<TokenRange> srcTokenRanges;
    private List<TokenRange> dstTokenRanges;
    private MappingStore ms;

    private int togetherMoveRangeNum = 0;
    private int togetherRangeNumInSameStmt = 0;
    private int mapPriority;

    // Use as cache
    private Map<TokenRange, Integer> srcRangeIdxMap = null;
    private Map<TokenRange, Integer> dstRangeIdxMap = null;


    public TokenMappingCalculator(MappingStore ms, TokenRange srcTokenRange, TokenRange dstTokenRange,
                                  TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap){
        this.ms = ms;
        this.srcTokenRange = srcTokenRange;
        this.dstTokenRange = dstTokenRange;
        this.srcTreeWordMap = srcTreeWordMap;
        this.dstTreeWordMap = dstTreeWordMap;
        this.srcStmt = srcTreeWordMap.getStmtOfTokenRange(srcTokenRange);
        this.dstStmt = dstTreeWordMap.getStmtOfTokenRange(dstTokenRange);
        this.srcWordTree = srcTreeWordMap.getTokenRangeTreeMap().get(srcTokenRange);
        this.dstWordTree = dstTreeWordMap.getTokenRangeTreeMap().get(dstTokenRange);
        if (srcStmt != null)
            this.srcTokenRanges = srcTreeWordMap.getTokenRangesOfNode(srcStmt);
        if (dstStmt != null)
            this.dstTokenRanges = dstTreeWordMap.getTokenRangesOfNode(dstStmt);
    }

    private void initRangeIdxMap(){
        if (srcRangeIdxMap != null && dstRangeIdxMap != null)
            return;
        srcRangeIdxMap = new HashMap<>();
        dstRangeIdxMap = new HashMap<>();
        if (srcTokenRanges != null) {
            for (int i = 0; i < srcTokenRanges.size(); i++)
                srcRangeIdxMap.put(srcTokenRanges.get(i), i);
        }
        if (dstTokenRanges != null) {
            for (int i = 0; i < dstTokenRanges.size(); i++)
                dstRangeIdxMap.put(dstTokenRanges.get(i), i);
        }
    }

    public void setAssumeSrcStmt(ITree assumeSrcStmt) {
        this.assumeSrcStmt = assumeSrcStmt;
    }

    public void setAssumeDstStmt(ITree assumeDstStmt) {
        this.assumeDstStmt = assumeDstStmt;
    }

    public boolean isMappedToSameStmt(){
        if (assumeSrcStmt != null || assumeDstStmt != null){
            return assumeSrcStmt == srcStmt && assumeDstStmt == dstStmt;
        }
        return ms.isSrcMapped(srcStmt) && ms.getDstForSrc(srcStmt) == dstStmt;
    }

    public boolean isSameContent() {
        String srcWordContent = srcTreeWordMap.getTokenByRange(srcTokenRange);
        String dstWordContent = dstTreeWordMap.getTokenByRange(dstTokenRange);
        if (srcWordContent != null && dstWordContent != null)
            return srcWordContent.equals(dstWordContent);
        return false;
    }

    private boolean isWithLeftNeighborWord(TokenRange srcRange, TokenRange dstRange){
        initRangeIdxMap();
        int srcIndex = srcRangeIdxMap.get(srcRange);
        int dstIndex = dstRangeIdxMap.get(dstRange);
        if (srcIndex > 0 && dstIndex > 0){
            TokenRange srcLeftRange = srcTokenRanges.get(srcIndex - 1);
            TokenRange mappedDstLeftRange = TreeTokensMap.findMappedRange(ms, srcLeftRange, true,
                    srcTreeWordMap, dstTreeWordMap);
            if (mappedDstLeftRange == null)
                return false;
            if (!dstRangeIdxMap.containsKey(mappedDstLeftRange))
                return false;
            else
                return dstRangeIdxMap.get(mappedDstLeftRange) == dstIndex - 1;
        }
        return false;
    }

    private boolean isWithRightNeighborWord(TokenRange srcRange, TokenRange dstRange){
        initRangeIdxMap();
        int srcIndex = srcRangeIdxMap.get(srcRange);
        int dstIndex = dstRangeIdxMap.get(dstRange);
        if (srcIndex < srcTokenRanges.size() - 1 && dstIndex < dstTokenRanges.size() - 1){
            TokenRange srcRightRange = srcTokenRanges.get(srcIndex + 1);
            TokenRange mappedDstRightRange = TreeTokensMap.findMappedRange(ms, srcRightRange, true,
                    srcTreeWordMap, dstTreeWordMap);
            if (mappedDstRightRange == null)
                return false;
            if (!dstRangeIdxMap.containsKey(mappedDstRightRange))
                return false;
            else
                return dstRangeIdxMap.get(mappedDstRightRange) == dstIndex + 1;
        }
        return false;
    }

    private Pair<List<TokenRange>, List<TokenRange>> getTogetherMappedWordRangesInLeft(boolean checkMoveToOtherStmt){
        initRangeIdxMap();
        TokenRange tmpSrcTokenRangeToLeft = srcTokenRange;
        TokenRange tmpDstTokenRangeToLeft = dstTokenRange;
        List<TokenRange> ret1 = new LinkedList<>();
        List<TokenRange> ret2 = new LinkedList<>();
        if (checkMoveToOtherStmt && isMappedToSameStmt())
            return new Pair<>(ret1, ret2);
        while(isWithLeftNeighborWord(
                tmpSrcTokenRangeToLeft,
                tmpDstTokenRangeToLeft)){
            int srcIndex = srcRangeIdxMap.get(tmpSrcTokenRangeToLeft);
            int dstIndex = dstRangeIdxMap.get(tmpDstTokenRangeToLeft);
            TokenRange srcLeftRange = srcTokenRanges.get(srcIndex - 1);
            TokenRange dstLeftRange = dstTokenRanges.get(dstIndex - 1);
            tmpSrcTokenRangeToLeft = srcLeftRange;
            tmpDstTokenRangeToLeft = dstLeftRange;
            ret1.add(0, tmpSrcTokenRangeToLeft);
            ret2.add(0, tmpDstTokenRangeToLeft);
        }
        return new Pair<>(ret1, ret2);
    }

    private Pair<List<TokenRange>, List<TokenRange>> getTogetherMappedWordRangesInRight(boolean checkMoveToOtherStmt){
        initRangeIdxMap();
        TokenRange tmpSrcTokenRangeToRight = srcTokenRange;
        TokenRange tmpDstTokenRangeToRight = dstTokenRange;
        List<TokenRange> ret1 = new LinkedList<>();
        List<TokenRange> ret2 = new LinkedList<>();
        if (checkMoveToOtherStmt && isMappedToSameStmt())
            return new Pair<>(ret1, ret2);

        while (isWithRightNeighborWord(
                tmpSrcTokenRangeToRight,
                tmpDstTokenRangeToRight)){
            int srcIndex = srcRangeIdxMap.get(tmpSrcTokenRangeToRight);
            int dstIndex = dstRangeIdxMap.get(tmpDstTokenRangeToRight);
            TokenRange srcRightRange = srcTokenRanges.get(srcIndex + 1);
            TokenRange dstRightRange = dstTokenRanges.get(dstIndex + 1);
            tmpSrcTokenRangeToRight = srcRightRange;
            tmpDstTokenRangeToRight = dstRightRange;
            ret1.add(tmpSrcTokenRangeToRight);
            ret2.add(tmpDstTokenRangeToRight);
        }
        return new Pair<>(ret1, ret2);
    }

    private Pair<List<TokenRange>, List<TokenRange>> getConsecutiveWordRangesInSameStmt(){
        List<TokenRange> ret1 = new ArrayList<>();
        List<TokenRange> ret2 = new ArrayList<>();
        if (isMappedToSameStmt()) {
            Pair<List<TokenRange>, List<TokenRange>> leftRanges =
                    getTogetherMappedWordRangesInLeft(false);
            ret1.addAll(leftRanges.first);
            ret2.addAll(leftRanges.second);
            ret1.add(srcTokenRange);
            ret2.add(dstTokenRange);
            Pair<List<TokenRange>, List<TokenRange>> rightRanges =
                    getTogetherMappedWordRangesInRight(false);
            ret1.addAll(rightRanges.first);
            ret2.addAll(rightRanges.second);
        }
        return new Pair<>(ret1, ret2);
    }

    public Pair<List<TokenRange>, List<TokenRange>> getTogetherMovedWordRanges(){
        List<TokenRange> ret1 = new ArrayList<>();
        List<TokenRange> ret2 = new ArrayList<>();
        if (!isMappedToSameStmt()){
            Pair<List<TokenRange>, List<TokenRange>> leftRanges = getTogetherMappedWordRangesInLeft(true);
            ret1.addAll(leftRanges.first);
            ret2.addAll(leftRanges.second);
            ret1.add(srcTokenRange);
            ret2.add(dstTokenRange);
            Pair<List<TokenRange>, List<TokenRange>> rightRanges = getTogetherMappedWordRangesInRight(true);
            ret1.addAll(rightRanges.first);
            ret2.addAll(rightRanges.second);
        }
        return new Pair<>(ret1, ret2);
    }


    // 是否需要考虑相同的content？
    public boolean isLowQualityMapping(ConsecutiveTokenRangeCache rangeCache, boolean considerSameContent){
        if (srcStmt == null || dstStmt == null)
            return false;

        boolean mappedInSameStmt = isMappedToSameStmt();
        if (mappedInSameStmt)
            return false;
        if (considerSameContent) {
            boolean isSameContent = isSameContent();
            if (isSameContent)
                return false;
        }
        boolean condition1 = rangeCache.containSrcRangeInSameStmt(srcTokenRange);
        boolean condition2 = rangeCache.containMovedSrcRange(srcTokenRange);

        if (!condition1 && !condition2){
            Pair<List<TokenRange>, List<TokenRange>> togetherMovedRanges = getTogetherMovedWordRanges();
            Pair<List<TokenRange>, List<TokenRange>> togetherRangesInStmt = getConsecutiveWordRangesInSameStmt();
            rangeCache.addNewWordRanges(togetherRangesInStmt.first, togetherMovedRanges.first,
                    togetherMovedRanges.second);
        }

        if (rangeCache.containMovedSrcRange(srcTokenRange)){
            togetherRangeNumInSameStmt = 0;
            togetherMoveRangeNum = rangeCache.getSizeOfTogetherMovedRanges();
            if (togetherMoveRangeNum >= 2)
                return false;
        }

        return true;
    }

    public static boolean isLowQualityMapping(MappingStore ms, TokenRange srcTokenRange,
                                              TokenRange dstTokenRange,
                                              TreeTokensMap srcTreeWordMap,
                                              TreeTokensMap dstTreeWordMap,
                                              ConsecutiveTokenRangeCache rangeCache,
                                              boolean considerSameContent){
        TokenMappingCalculator calculator = new TokenMappingCalculator(ms, srcTokenRange, dstTokenRange,
                srcTreeWordMap, dstTreeWordMap);
        return calculator.isLowQualityMapping(rangeCache, considerSameContent);
    }
}
