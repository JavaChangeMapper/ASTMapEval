package cs.zju.stm.edit.tokenedit.actions;


import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.TokenRange;

import java.util.ArrayList;
import java.util.List;


public class TokenEditAction {
    protected ITree srcStmt;
    protected ITree dstStmt;
    protected int srcStmtLine;
    protected int dstStmtLine;

    protected ITree srcStmtOfWords;
    protected ITree dstStmtOfWords;
    protected int srcWordsLine;
    protected int dstWordsLine;

    protected List<String> srcTokens;
    protected List<String> dstTokens;
    protected List<TokenRange> srcRanges;
    protected List<TokenRange> dstRanges;

    protected List<TokenRange> exchangeRanges;
    protected List<TokenRange> exchangeDstRanges;
    protected List<String> exchangeTokens;

    protected String type = "";
    protected String moveDirection = "";


    public TokenEditAction(ITree srcStmt, ITree dstStmt) {
        this.srcStmt = srcStmt;
        this.dstStmt = dstStmt;
    }

    public void setWordRange(TokenRange srcRange, TokenRange dstRange,
                             TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap){
        List<TokenRange> srcRanges = null;
        List<TokenRange> dstRanges = null;
        if (srcRange != null){
            srcRanges = new ArrayList<>();
            srcRanges.add(srcRange);
        }
        if (dstRange != null){
            dstRanges = new ArrayList<>();
            dstRanges.add(dstRange);
        }
        setWordRanges(srcRanges, dstRanges,srcTreeWordMap, dstTreeWordMap);
    }

    public void setWordRangesForInStmtMove(List<TokenRange> srcRanges, List<TokenRange> dstRanges,
                                           TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap,
                                           List<TokenRange> rangesExchange, List<TokenRange> dstRangesExchange){
        setWordRanges(srcRanges, dstRanges, srcTreeWordMap, dstTreeWordMap);
        this.exchangeRanges = rangesExchange;
        this.exchangeDstRanges = dstRangesExchange;
        exchangeTokens = new ArrayList<>();
        for (TokenRange range: exchangeRanges){
            String word = srcTreeWordMap.getTokenByRange(range);
            exchangeTokens.add(word);
        }
    }

    public void setWordRanges(List<TokenRange> srcRanges, List<TokenRange> dstRanges,
                              TreeTokensMap srcTreeWordMap, TreeTokensMap dstTreeWordMap){
        this.srcRanges = srcRanges;
        this.dstRanges = dstRanges;
        this.srcTokens = null;
        this.dstTokens = null;
        if (srcRanges != null) {
            TokenRange srcRange = srcRanges.get(0);
            srcStmtOfWords = srcTreeWordMap.getStmtOfTokenRange(srcRange);
            this.srcTokens = srcTreeWordMap.getTokensByRanges(srcRanges);
            this.srcWordsLine = srcTreeWordMap.getLineRangeOfStmt(srcStmtOfWords).first;
        }
        if (dstRanges != null) {
            TokenRange dstRange = dstRanges.get(0);
            dstStmtOfWords = dstTreeWordMap.getStmtOfTokenRange(dstRange);
            this.dstTokens = dstTreeWordMap.getTokensByRanges(dstRanges);
            this.dstWordsLine = dstTreeWordMap.getLineRangeOfStmt(dstStmtOfWords).first;
        }
        if (srcStmt != null)
            this.srcStmtLine = srcTreeWordMap.getLineRangeOfStmt(srcStmt).first;
        if (dstStmt != null)
            this.dstStmtLine = dstTreeWordMap.getLineRangeOfStmt(dstStmt).first;
    }

    public String getName() {
        return "";
    }

    public List<TokenRange> getSrcRanges() {
        return srcRanges;
    }

    public List<TokenRange> getDstRanges() {
        return dstRanges;
    }

    public List<TokenRange> getExchangeRanges() {
        return exchangeRanges;
    }

    public List<TokenRange> getExchangeDstRanges() {
        return exchangeDstRanges;
    }

    public boolean isMove() {
        return (this instanceof TokenMoveFromStmt)
                || (this instanceof TokenMoveToStmt);
    }

    public boolean isExchange() {
        return this instanceof TokenInStmtMove;
    }

    public ITree getSrcStmt() {
        return srcStmt;
    }

    public ITree getDstStmt() {
        return dstStmt;
    }

    public String getType() {
        return type;
    }

    public boolean isIdenticalTo(TokenEditAction action){
        if (srcStmt == action.srcStmt && dstStmt == action.dstStmt){
            boolean condition1 = srcRanges == null && action.srcRanges == null;
            boolean condition2 = dstRanges == null && action.dstRanges == null;
            if (condition1 || (srcRanges != null && srcRanges.equals(action.srcRanges))) {
                if (condition2 || (dstRanges != null && dstRanges.equals(action.dstRanges))) {
                    return type.equals(action.type);
                }
            }
        }
        return false;
    }

    public void setMoveDirectionInStmtMove(String moveDirection){
        this.moveDirection = moveDirection;
    }
}
