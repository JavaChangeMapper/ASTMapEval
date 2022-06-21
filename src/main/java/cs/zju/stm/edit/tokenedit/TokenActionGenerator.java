package cs.zju.stm.edit.tokenedit;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.edit.tokenedit.actions.*;
import cs.zju.stm.edit.tokenedit.reverseorder.*;
import cs.zju.utils.Pair;

import java.util.*;

public class TokenActionGenerator {
    private TreeTokensMap srcTreeWordMap;
    private TreeTokensMap dstTreeWordMap;
    private ITree srcStmt;
    private ITree dstStmt;
    private List<TokenRange> srcTokenRanges;
    private List<TokenRange> dstTokenRanges;

    private MappingStore stmtMs;

    private List<Pair<TokenRange, TokenRange>> sameWordRangePairs;
    private List<Pair<TokenRange, TokenRange>> matchedWordRanges = null;

    private double edits;
    private List<TokenEditAction> actions;

    // Use as cache
    private Map<TokenRange, Integer> srcRangeIdxMap = new HashMap<>();
    private Map<TokenRange, Integer> dstRangeIdxMap = new HashMap<>();


    public TokenActionGenerator(MappingStore stmtMs, TreeTokensMap srcTreeWordMap,
                                TreeTokensMap dstTreeWordMap, ITree srcStmt, ITree dstStmt){
        this.srcTreeWordMap = srcTreeWordMap;
        this.dstTreeWordMap = dstTreeWordMap;
        this.srcStmt = srcStmt;
        this.dstStmt = dstStmt;
        if (srcTreeWordMap != null && srcStmt != null)
            this.srcTokenRanges = srcTreeWordMap.getTokenRangesOfNode(srcStmt);
        if (dstTreeWordMap != null && dstStmt != null)
            this.dstTokenRanges = dstTreeWordMap.getTokenRangesOfNode(dstStmt);
        this.stmtMs = stmtMs;
        this.actions = new ArrayList<>();
        if (srcTokenRanges != null) {
            for (int i = 0; i < srcTokenRanges.size(); i++)
                srcRangeIdxMap.put(srcTokenRanges.get(i), i);
        }
        if (dstTokenRanges != null) {
            for (int i = 0; i < dstTokenRanges.size(); i++)
                dstRangeIdxMap.put(dstTokenRanges.get(i), i);
        }
    }

    public double getEdits() {
        return edits;
    }

    public List<TokenEditAction> getActions(){
        return actions;
    }

    public List<TokenEditAction> getOrderedActions(){
        TokenEditOrderCalculator calculator = new TokenEditOrderCalculator(srcTokenRanges, dstTokenRanges,
                stmtMs, actions, matchedWordRanges);
        return calculator.getOrderedActions();
    }

    public List<Pair<String, String>> getSameWordPairs(){
        List<Pair<String, String>> ret = new ArrayList<>();
        for (Pair<TokenRange, TokenRange> rangePair: sameWordRangePairs){
            ret.add(new Pair<>(srcTreeWordMap.getTokenByRange(rangePair.first),
                    dstTreeWordMap.getTokenByRange(rangePair.second)));
        }
        return ret;
    }

    public List<Pair<TokenRange, TokenRange>> getMatchedWordRanges(){
        return matchedWordRanges;
    }

    public List<Pair<String, String>> getMatchedWords(){
        List<Pair<String, String>> ret = new ArrayList<>();
        for (Pair<TokenRange, TokenRange> pair: matchedWordRanges){
            TokenRange left = pair.first;
            TokenRange right = pair.second;
            String leftWord = srcTreeWordMap.getTokenByRange(left);
            String rightWord = dstTreeWordMap.getTokenByRange(right);
            ret.add(new Pair<>(leftWord, rightWord));
        }
        return ret;
    }

    // Calculate Word Edit Actions
    public void calWordEditActions() {
        sameWordRangePairs = new ArrayList<>();
        matchedWordRanges = new ArrayList<>();
        edits = 0;
        List<Integer> srcWordRangeIndexesFromDst = new ArrayList<>();
        List<Integer> dstWordRangeIndexesFromSrc = new ArrayList<>();

        List<List<Integer>> insertedWordsIndexes = null;
        List<List<Integer>> removedWordsIndexes = null;

        if (srcStmt != null && dstStmt != null) {
            for (TokenRange srcRange : srcTokenRanges) {
                TokenRange dstRange = findMappedRange(stmtMs, srcRange, true);
                if (dstRange == null)
                    continue;
                if (dstTreeWordMap.isBaseElementTokenInStmt(dstRange, dstStmt)) {
                    srcWordRangeIndexesFromDst.add(dstRangeIdxMap.get(dstRange));
                    String srcWord = srcTreeWordMap.getTokenByRange(srcRange);
                    String dstWord = dstTreeWordMap.getTokenByRange(dstRange);
                    matchedWordRanges.add(new Pair<>(srcRange, dstRange));
                    if (!srcWord.equals(dstWord)) {
                        TokenEditAction action = new TokenUpdate(srcStmt, dstStmt);
                        action.setWordRange(srcRange, dstRange, srcTreeWordMap, dstTreeWordMap);
                        actions.add(action);
                        edits += 1;
                    } else {
                        sameWordRangePairs.add(new Pair<>(srcRange, dstRange));
                    }
                }
            }

            for (TokenRange dstRange : dstTokenRanges) {
                TokenRange srcRange = findMappedRange(stmtMs, dstRange, false);
                if (srcRange == null)
                    continue;

                if (srcTreeWordMap.isBaseElementTokenInStmt(srcRange, srcStmt)) {
                    dstWordRangeIndexesFromSrc.add(srcRangeIdxMap.get(srcRange));
                }
            }

            LcsReverseOrder rm1 = new LcsReverseOrder(srcWordRangeIndexesFromDst, dstTokenRanges.size());
            LcsReverseOrder rm2 = new LcsReverseOrder(dstWordRangeIndexesFromSrc, srcTokenRanges.size());
            double inStmtEdits = rm1.getMoveEdits();
            List<LcsSiblingMove> reverseOrderMoves = rm1.getReverseOrderMoveList();
//            addAllInterStmtMoveActions(reverseOrderMoves);\
            addAllLcsInterStmtMoveActions(reverseOrderMoves);
            edits += inStmtEdits;
            Set<Integer> mappedSrcWordIdxes = new HashSet<>();
            Set<Integer> mappedDstWordIdxes = new HashSet<>();
            insertedWordsIndexes = rm1.getInsertedOrRemovedNumberList(mappedDstWordIdxes);
            removedWordsIndexes = rm2.getInsertedOrRemovedNumberList(mappedSrcWordIdxes);
        } else {
            if (srcStmt != null){
                removedWordsIndexes = new ArrayList<>();
                List<Integer> tmp = new ArrayList<>();
                for (int i = 0; i < srcTokenRanges.size(); i++)
                    tmp.add(i);
                removedWordsIndexes.add(tmp);
            }
            if (dstStmt != null){
                insertedWordsIndexes = new ArrayList<>();
                List<Integer> tmp = new ArrayList<>();
                for (int i = 0; i < dstTokenRanges.size(); i++)
                    tmp.add(i);
                insertedWordsIndexes.add(tmp);
            }
        }

        if (insertedWordsIndexes != null) {
            for (List<Integer> consecutiveNumbers : insertedWordsIndexes) {
                edits += getEditsFromConsecutiveWords(stmtMs, consecutiveNumbers, dstTokenRanges, false);
            }
        }

        if (removedWordsIndexes != null) {
            for (List<Integer> consecutiveNumbers : removedWordsIndexes) {
                edits += getEditsFromConsecutiveWords(stmtMs, consecutiveNumbers, srcTokenRanges, true);
            }
        }
    }

    private double getEditsFromConsecutiveWords(MappingStore ms, List<Integer> indexes,
                                                List<TokenRange> ranges, boolean isSrc){
        List<TokenRange> consecutiveWords = new ArrayList<>();
        for (int index: indexes){
            consecutiveWords.add(ranges.get(index));
        }
        if (consecutiveWords.size() == 1){
            double edits = 1.0;
            edits += getWordEdits(ms, consecutiveWords.get(0), isSrc);
            addActionForConsecutiveWords(consecutiveWords, isSrc);
            return edits;
        }
        List<TokenRange> tmpRanges = new ArrayList<>();
        double edits = 0;
        for (int i = 0; i < consecutiveWords.size(); i++){
            TokenRange range = consecutiveWords.get(i);
            if (tmpRanges.size() == 0){
                edits += 1.0;
                tmpRanges.add(range);
                edits += getWordEdits(ms, range, isSrc);
                continue;
            }

            if (isNeighborWordsMoved(ms, tmpRanges.get(tmpRanges.size() - 1), range, isSrc)) {
                tmpRanges.add(range);
                edits += getWordEdits(ms, range, isSrc);
            } else {
                if (tmpRanges.size() > 0){
                    addActionForConsecutiveWords(tmpRanges, isSrc);
                }
                tmpRanges = new ArrayList<>();
                edits += 1.0;
                tmpRanges.add(range);
                edits += getWordEdits(ms, range, isSrc);
            }
        }
        if (tmpRanges.size() > 0) {
            addActionForConsecutiveWords(tmpRanges, isSrc);
        }
        return edits;
    }

    private void addActionForConsecutiveWords(List<TokenRange> ranges, boolean isSrc){
        boolean isMoved = isWordMovedAcrossStmt(stmtMs, ranges.get(0), isSrc);
        if (!isMoved){
            if (ranges.size() > 1) {
                throw new RuntimeException("Cannot handle insert or remove more than 1 word");
            }
            if (isSrc){
                TokenEditAction action = new TokenDelete(srcStmt, dstStmt);
                action.setWordRanges(ranges, null, srcTreeWordMap, dstTreeWordMap);
                actions.add(action);
            } else {
                TokenEditAction action = new TokenInsert(srcStmt, dstStmt);
                action.setWordRanges(null, ranges, srcTreeWordMap, dstTreeWordMap);
                actions.add(action);
            }
        } else {
            List<TokenRange> mappedRanges = getMappedWordRanges(ranges, isSrc);
            if (isSrc){
                TokenEditAction action = new TokenMoveFromStmt(srcStmt, dstStmt);
                action.setWordRanges(ranges, mappedRanges, srcTreeWordMap, dstTreeWordMap);
                actions.add(action);
            } else {
                TokenEditAction action = new TokenMoveToStmt(srcStmt, dstStmt);
                action.setWordRanges(mappedRanges, ranges, srcTreeWordMap, dstTreeWordMap);
                actions.add(action);
            }
        }
    }

    private List<TokenRange> getMappedWordRanges(List<TokenRange> ranges, boolean isSrc){
        List<TokenRange> ret = new ArrayList<>();
        for (TokenRange range: ranges){
            TokenRange mappedRange = findMappedRange(stmtMs, range, isSrc);
            ret.add(mappedRange);
        }
        return ret;
    }

    private boolean isWordMovedAcrossStmt(MappingStore ms, TokenRange range, boolean isSrc){
        TokenRange mappedTokenRange = findMappedRange(ms, range, isSrc);
        if (mappedTokenRange == null)
            return false;
        ITree srcStmt;
        ITree dstStmt;
        if (isSrc){
            srcStmt = srcTreeWordMap.getStmtOfTokenRange(range);
            dstStmt = dstTreeWordMap.getStmtOfTokenRange(mappedTokenRange);
        } else {
            srcStmt = srcTreeWordMap.getStmtOfTokenRange(mappedTokenRange);
            dstStmt = dstTreeWordMap.getStmtOfTokenRange(range);
        }
        return ms.getDstForSrc(srcStmt) != dstStmt;
    }

    private boolean isNeighborWordsMoved(MappingStore ms, TokenRange range1, TokenRange range2, boolean isSrc){
        return TreeTokensMap.isNeighborTokensMoved(ms, range1, range2, isSrc, srcTreeWordMap, dstTreeWordMap);
    }

    private double getWordEdits(MappingStore ms, TokenRange range, boolean isSrc){
        if (isSrc)
            return 0;
        TokenRange srcRange = findMappedRange(ms, range, false);
        if (srcRange != null){
            String srcWord = srcTreeWordMap.getTokenByRange(srcRange);
            String dstWord = dstTreeWordMap.getTokenByRange(range);
            if (!srcWord.equals(dstWord)) {
                TokenEditAction action = new TokenUpdate(srcStmt, dstStmt);
                action.setWordRange(srcRange, range, srcTreeWordMap, dstTreeWordMap);
                actions.add(action);
                return 1;
            }
        }
        return 0;
    }

    private TokenRange findMappedRange(MappingStore ms, TokenRange tokenRange,
                                       boolean isSrc){
        return TreeTokensMap.findMappedRange(ms, tokenRange, isSrc, srcTreeWordMap, dstTreeWordMap);
    }

    private void addAllLcsInterStmtMoveActions(List<LcsSiblingMove> moveList){
        for (LcsSiblingMove move: moveList){
            InStmtMoveRange r = calInStmtMovedRanges(move);
            List<TokenRange> srcRanges = r.movedRanges;
            String moveDirection = move.moveType;
            List<TokenRange> dstRanges = new ArrayList<>();
            for (TokenRange range: srcRanges){
                TokenRange dstRange = findMappedRange(stmtMs, range, true);
                dstRanges.add(dstRange);
            }
            TokenEditAction action = new TokenInStmtMove(srcStmt, dstStmt);
            action.setWordRanges(srcRanges, dstRanges, srcTreeWordMap, dstTreeWordMap);
            action.setMoveDirectionInStmtMove(moveDirection);
            actions.add(action);
        }
    }

    @Deprecated
    private void addAllInterStmtMoveActions(List<InterSiblingMove> moveListByDstIdxes){
        for(InterSiblingMove move: moveListByDstIdxes){
            InStmtMovedRangeGroup g = calInStmtMovedRanges(move);
            List<TokenRange> srcRanges = g.movedRanges;

            List<TokenRange> dstRanges = new ArrayList<>();
            for (TokenRange range: srcRanges){
                TokenRange dstRange = findMappedRange(stmtMs, range, true);
                dstRanges.add(dstRange);
            }
            TokenEditAction action = new TokenInStmtMove(srcStmt, dstStmt);
            List<TokenRange> exchangedDstRanges = new ArrayList<>();
            for (TokenRange range: g.rangesNotMoved){
                exchangedDstRanges.add(findMappedRange(stmtMs, range, true));
            }
            action.setWordRangesForInStmtMove(srcRanges, dstRanges, srcTreeWordMap, dstTreeWordMap,
                    g.rangesNotMoved, exchangedDstRanges);
            actions.add(action);
        }
    }

    private InStmtMoveRange calInStmtMovedRanges(LcsSiblingMove move){
        List<TokenRange> movedRanges = new ArrayList<>();
        for (int i: move.movedTargetIdxes){
            TokenRange dstRange = dstTokenRanges.get(i);
            TokenRange srcRange = findMappedRange(stmtMs, dstRange, false);
            movedRanges.add(srcRange);
        }
        return new InStmtMoveRange(movedRanges, move.moveType);
    }

    @Deprecated
    private InStmtMovedRangeGroup calInStmtMovedRanges(InterSiblingMove move){
        List<Integer> leftList = move.leftList;
        List<Integer> rightList = move.rightList;
        List<TokenRange> leftSrcRanges = new ArrayList<>();
        List<TokenRange> rightSrcRanges = new ArrayList<>();
        for (int i: leftList){
            TokenRange dstRange = dstTokenRanges.get(i);
            TokenRange srcRange = findMappedRange(stmtMs, dstRange, false);
            leftSrcRanges.add(srcRange);
        }
        for (int i: rightList){
            TokenRange dstRange = dstTokenRanges.get(i);
            TokenRange srcRange = findMappedRange(stmtMs, dstRange, false);
            rightSrcRanges.add(srcRange);
        }
        return new InStmtMovedRangeGroup(rightSrcRanges, leftSrcRanges);
    }

//    private InStmtMovedRangeGroup calInStmtMovedRanges(InterSiblingMove move){
//        List<Integer> leftList = move.leftList;
//        List<Integer> rightList = move.rightList;
//        List<WordRange> movedRanges;
//        List<WordRange> rangesNotMoved;
//        if (leftList.size() <= rightList.size()){
//            movedRanges = getSrcRangeListByDstIdx(leftList);
//            rangesNotMoved = getSrcRangeListByDstIdx(rightList);
//        } else {
//            movedRanges = getSrcRangeListByDstIdx(rightList);
//            rangesNotMoved = getSrcRangeListByDstIdx(leftList);
//        }
//        return new InStmtMovedRangeGroup(movedRanges, rangesNotMoved);
//    }

    private List<TokenRange> getSrcRangeListByDstIdx(List<Integer> idxes){
        List<TokenRange> srcRanges = new ArrayList<>();
        for (int i: idxes){
            TokenRange dstRange = dstTokenRanges.get(i);
            TokenRange srcRange = findMappedRange(stmtMs, dstRange, false);
            srcRanges.add(srcRange);
        }
        return srcRanges;
    }

    public Set<TokenRange> getLowQualityMappedWordRanges(boolean considerSameContent){
        Set<TokenRange> ret = new HashSet<>();
        if (getEdits() == 0)
            return ret;
        if (srcTokenRanges != null && dstTokenRanges != null) {
            if (srcTokenRanges.size() == 0 && dstTokenRanges.size() == 0)
                return ret;
        }

        if (srcTokenRanges != null) {
            ConsecutiveTokenRangeCache rangeCache = new ConsecutiveTokenRangeCache(srcTreeWordMap, dstTreeWordMap);
            for (TokenRange srcRange : srcTokenRanges) {
                TokenRange dstRange = findMappedRange(stmtMs, srcRange, true);
                boolean isLowQuality = TokenMappingCalculator.isLowQualityMapping(stmtMs, srcRange,
                        dstRange, srcTreeWordMap, dstTreeWordMap, rangeCache, considerSameContent);
                if (isLowQuality) {
                    ret.add(srcRange);
                }
            }
        }
        return ret;
    }


    public static class InStmtMoveRange{
        public List<TokenRange> movedRanges;
        public String moveDirection;

        public InStmtMoveRange (List<TokenRange> movedRanges, String moveDirection){
            this.movedRanges = movedRanges;
            this.moveDirection = moveDirection;
        }
    }

    @Deprecated
    public static class InStmtMovedRangeGroup {
        public List<TokenRange> movedRanges;
        public List<TokenRange> rangesNotMoved;

        public InStmtMovedRangeGroup(List<TokenRange> movedRanges, List<TokenRange> rangesNotMoved) {
            this.movedRanges = movedRanges;
            this.rangesNotMoved = rangesNotMoved;
        }
    }
}
