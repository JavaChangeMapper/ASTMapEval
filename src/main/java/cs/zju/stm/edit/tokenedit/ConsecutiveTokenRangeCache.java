package cs.zju.stm.edit.tokenedit;

import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;

import java.util.*;

public class ConsecutiveTokenRangeCache {
    private TreeTokensMap srcTreeWordMap;
    private TreeTokensMap dstTreeWordMap;

    private List<String> togetherMovedSrcWords;
    private List<String> togetherMovedDstWords;
    private List<TokenRange> togetherSrcRangesInSameStmt;
    private List<TokenRange> togetherMovedSrcRanges;
    private List<TokenRange> togetherMovedDstRanges;

    private Map<TokenRange, Integer> togetherSrcRangesInSameStmtMap;
    private Map<TokenRange, Integer> togetherMovedSrcRangesMap;

    private int sameWordCount;
    private double sameWordRatio;

    public ConsecutiveTokenRangeCache(TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap){
        togetherMovedSrcWords = new ArrayList<>();
        togetherMovedDstWords = new ArrayList<>();
        togetherSrcRangesInSameStmt = new ArrayList<>();
        togetherMovedSrcRanges = new ArrayList<>();
        togetherMovedDstRanges = new ArrayList<>();
        togetherSrcRangesInSameStmtMap = new HashMap<>();
        togetherMovedSrcRangesMap = new HashMap<>();
        this.srcTreeWordMap = srcTreeWordMap;
        this.dstTreeWordMap = dstTreeWordMap;
    }

    public void addNewWordRanges(List<TokenRange> togetherSrcRangesInSameStmt,
                                 List<TokenRange> togetherMovedSrcRanges,
                                 List<TokenRange> togetherMovedDstRanges){
        this.togetherSrcRangesInSameStmt = togetherSrcRangesInSameStmt;
        this.togetherMovedSrcRanges = togetherMovedSrcRanges;
        this.togetherMovedDstRanges = togetherMovedDstRanges;
        this.togetherSrcRangesInSameStmtMap = new HashMap<>();
        this.togetherMovedSrcRangesMap = new HashMap<>();
        togetherMovedSrcWords = new ArrayList<>();
        togetherMovedDstWords = new ArrayList<>();
        for (int i = 0; i < togetherSrcRangesInSameStmt.size(); i++)
            togetherSrcRangesInSameStmtMap.put(togetherSrcRangesInSameStmt.get(i), i);

        for (int i = 0; i < togetherMovedSrcRanges.size(); i++) {
            togetherMovedSrcRangesMap.put(togetherMovedSrcRanges.get(i), i);
            String word = srcTreeWordMap.getTokenByRange(togetherMovedSrcRanges.get(i));
            togetherMovedSrcWords.add(word);
        }

        for (TokenRange togetherMovedDstRange : togetherMovedDstRanges) {
            String word = dstTreeWordMap.getTokenByRange(togetherMovedDstRange);
            togetherMovedDstWords.add(word);
        }
        sameWordCount = 0;
        sameWordRatio = 0;
        calSameWordRatio();
    }

    private void calSameWordRatio(){
        if (togetherMovedSrcWords.size() > 0){
            sameWordCount = 0;
            for (int i = 0; i < togetherMovedSrcWords.size(); i++){
                String srcWord = togetherMovedSrcWords.get(i);
                String dstWord = togetherMovedDstWords.get(i);
                if (srcWord.equals(dstWord))
                    sameWordCount += 1.0;
            }
            sameWordRatio = (double) sameWordCount / togetherMovedSrcWords.size();
        }
    }

    public boolean canBeConsideredMoved(){
        int lowBoundForSameWord = togetherMovedSrcWords.size() / 2;
        return sameWordCount >= lowBoundForSameWord;
    }

    public boolean noSameWord(){
        return sameWordCount == 0;
    }

    public boolean containSrcRangeInSameStmt(TokenRange range){
        return togetherSrcRangesInSameStmtMap.containsKey(range);
    }

    public boolean containMovedSrcRange(TokenRange srcRange){
        return togetherMovedSrcRangesMap.containsKey(srcRange);
    }

    public int getSizeOfTogetherSrcRangesInSameStmt(){
        return togetherSrcRangesInSameStmt.size();
    }

    public int getSizeOfTogetherMovedRanges(){
        return togetherMovedSrcRanges.size();
    }

    public boolean hasLeftNeighborMovedRange(TokenRange srcRange, boolean checkContent){
        int index = togetherMovedSrcRangesMap.get(srcRange);
        if (index <= 0)
            return false;
        else {
            if (checkContent){
                String leftSrcWord = togetherMovedSrcWords.get(index - 1);
                String leftDstWord = togetherMovedDstWords.get(index - 1);
                return leftSrcWord.equals(leftDstWord);
            }
            return true;
        }
    }

    public boolean hasRightNeighborMovedRange(TokenRange srcRange, boolean checkContent){
        int index = togetherMovedSrcRangesMap.get(srcRange);
        if (index == -1 || index == togetherMovedSrcRanges.size() - 1)
            return false;
        else {
            if (checkContent){
                String rightSrcWord = togetherMovedSrcWords.get(index + 1);
                String rightDstWord = togetherMovedDstWords.get(index + 1);
                return rightSrcWord.equals(rightDstWord);
            }
            return true;
        }
    }
}
