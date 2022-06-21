package cs.zju.stm.match;


import com.github.gumtreediff.tree.ITree;
import cs.zju.framework.match.StmtMatchMaps;
import cs.zju.framework.match.UniversalTreeNode;
import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;

import java.util.*;

/**
 * This class describes why a statement match is inaccurate
 *
 */
public class StmtMatchErrorDesc {
    public static final String BETTER_STMT_MATCH = "BETTER_STMT_MATCH";
    public static final String BETTER_TOKEN_MATCH = "BETTER_TOKEN_MATCH";
    public static final String UNLIKELY_TOKEN_MATCH = "UNLIKELY_MATCHED_TOKEN";
    public static final String UNLIKELY_MATCHED_STMT = "UNLIKELY_MATCHED_STMT";

    private String errorType = "";
    private boolean containsSrcTokenBadMatched = false;
    private boolean containsDstTokenBadMatched = false;
    private int comparisonTypeForSameStmt = -100;
    private int comparisonTypeForSrcStmt = -100;
    private int comparisonTypeForDstStmt = -100;
    private Map<TokenRange, Integer> srcTokenComparisonTypeMap;
    private Map<TokenRange, Integer> dstTokenComparisonTypeMap;

    private String compareAlgorithm;
    private Map<TokenRange, TokenRange> moreTokenMatchInSameStmtMapSrcToDst;
    private Map<TokenRange, TokenRange> moreTokenMatchInSameStmtMapDstToSrc;
    private UniversalTreeNode betterSrcStmt;
    private UniversalTreeNode betterDstStmt;

    // Impacted statements
    private Set<StmtMatch> impactStmtMatchSet;

    // information about the statement
    private StmtMatch sm;
    private String currentMatchAlgorithm;
    private List<TokenRange> srcTokens;
    private List<TokenRange> dstTokens;
    private Map<TokenRange, TokenRange> tokenMatchSrcToDst;
    private Map<TokenRange, TokenRange> tokenMatchDstToSrc;

    public StmtMatchErrorDesc(StmtMatch sm){
        this.sm = sm;
        this.srcTokens = sm.getSrcTokens();
        this.dstTokens = sm.getDstTokens();
        this.tokenMatchSrcToDst = sm.getTokenMapSrcToDst();
        this.tokenMatchDstToSrc = sm.getTokenMapDstToSrc();
        moreTokenMatchInSameStmtMapSrcToDst = new HashMap<>();
        moreTokenMatchInSameStmtMapDstToSrc = new HashMap<>();
        srcTokenComparisonTypeMap = new HashMap<>();
        dstTokenComparisonTypeMap = new HashMap<>();
        impactStmtMatchSet = new HashSet<>();
        errorType = "";
    }

    public void setCurrentMatchAlgorithm(String algorithm){
        this.currentMatchAlgorithm = algorithm.toUpperCase();
    }

    public void setCompareAlgorithm(String compareAlgorithm) {
        this.compareAlgorithm = compareAlgorithm.toUpperCase();
    }

    public void setErrorType(String type){
        this.errorType = type;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setComparisonTypeForSameStmt(int type) {
        this.comparisonTypeForSameStmt = type;
    }

    public void setComparisonTypeForSrcStmt(int type){
        this.comparisonTypeForSrcStmt = type;
    }

    public void setComparisonTypeForDstStmt(int type){
        this.comparisonTypeForDstStmt = type;
    }

    public void setContainsSrcTokenBadMatched(boolean containsSrcTokenBadMatched) {
        this.containsSrcTokenBadMatched = containsSrcTokenBadMatched;
    }

    public void setContainsDstTokenBadMatched(boolean containsDstTokenBadMatched) {
        this.containsDstTokenBadMatched = containsDstTokenBadMatched;
    }

    public int getBadTokenType(){
        if (srcTokenComparisonTypeMap.size() > 0 && dstTokenComparisonTypeMap.size() > 0)
            return StmtComparisonType.BOTH_TOKEN_BAD_MATCH;
        if (srcTokenComparisonTypeMap.size() > 0)
            return StmtComparisonType.SRC_TOKEN_BAD_MATCH;
        if (dstTokenComparisonTypeMap.size() > 0)
            return StmtComparisonType.DST_TOKEN_BAD_MATCH;
        return -1;
    }

    public boolean srcStmtOrTokenMatchError(){
        int badTokenType = getBadTokenType();
        if (badTokenType != -1 && badTokenType != StmtComparisonType.DST_TOKEN_BAD_MATCH)
            return true;
        if (comparisonTypeForSrcStmt != -100)
            return true;
        return false;
    }

    public boolean dstStmtOrTokenMatchError(){
        int badTokenType = getBadTokenType();
        if (badTokenType != -1 && badTokenType != StmtComparisonType.SRC_TOKEN_BAD_MATCH)
            return true;
        if (comparisonTypeForDstStmt != -100)
            return true;
        return false;
    }

    public int getSrcTokenMatchErrorType(){
        int type = -1;
        for (TokenRange range: srcTokenComparisonTypeMap.keySet()){
            if (type < srcTokenComparisonTypeMap.get(range))
                type = srcTokenComparisonTypeMap.get(range);
        }
        if (type == -1 && errorType.equals(UNLIKELY_TOKEN_MATCH)){
            if (getComparisonTypeForSrcStmt() == StmtComparisonType.SRC_CHANGE_TYPE_AND_VALUE)
                type = TokenComparisonType.CHANGE_TOKEN_TYPE_AND_VALUE;
        }
        return type;
    }

    public int getDstTokenMatchErrorType(){
        int type = -1;
        for (TokenRange range: dstTokenComparisonTypeMap.keySet()){
            if (type < dstTokenComparisonTypeMap.get(range))
                type = dstTokenComparisonTypeMap.get(range);
        }
        if (type == -1 && errorType.equals(UNLIKELY_TOKEN_MATCH)){
            if (getComparisonTypeForDstStmt() == StmtComparisonType.DST_CHANGE_TYPE_AND_VALUE)
                type = TokenComparisonType.CHANGE_TOKEN_TYPE_AND_VALUE;
        }
        return type;
    }

    @Deprecated
    public int getComparisonTypeForSameStmt() {
        return comparisonTypeForSameStmt;
    }

    public int getComparisonTypeForSrcStmt() {
        return comparisonTypeForSrcStmt;
    }

    public int getComparisonTypeForDstStmt() {
        return comparisonTypeForDstStmt;
    }

    /**
     * if we find better matched statement for a src or dst statement,
     * we assign the better dst or src statement.
     * @param otherStmt a better src or dst statement
     * @param isSrc if the statement is from source or dst file
     * @param currentMatchMaps maps of stmt match results
     */
    public void setBetterStmt(UniversalTreeNode otherStmt, boolean isSrc, StmtMatchMaps currentMatchMaps) {
        if (isSrc){
            betterSrcStmt = otherStmt;
            StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(betterSrcStmt, true);
            impactedMatch.setSrcReferenceErrorMatch(sm, comparisonTypeForDstStmt, -1);
            impactedMatch.setDstReferenceErrorMatch(sm, comparisonTypeForDstStmt, -1);
            impactStmtMatchSet.add(impactedMatch);
        } else {
            betterDstStmt = otherStmt;
            StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(betterDstStmt, false);
            impactedMatch.setSrcReferenceErrorMatch(sm, comparisonTypeForSrcStmt, -1);
            impactedMatch.setDstReferenceErrorMatch(sm, comparisonTypeForSrcStmt, -1);
            impactStmtMatchSet.add(impactedMatch);
        }
    }

    /**
     * Add a pair of src and dst tokens that are mapped in the same stmt
     * We also need to calculate statements that are impacted by the wrong matched tokens
     * @param srcToken token from the src stmt
     * @param dstToken token from the dst stmt
     * @param isSrc    if the token from src or dst stmt is wrongly matched
     * @param srcTreeTokenMap  src tree token map
     * @param dstTreeTokenMap  dst tree token map
     * @param currentMatchMaps stmt match maps for current algorithm
     */
    @Deprecated
    public void addMoreTokenToMap(TokenRange srcToken, TokenRange dstToken, boolean isSrc,
                                  TreeTokensMap srcTreeTokenMap, TreeTokensMap dstTreeTokenMap,
                                  StmtMatchMaps currentMatchMaps){
        if (isSrc) {
            moreTokenMatchInSameStmtMapSrcToDst.put(srcToken, dstToken);
            TokenRange wrongDstToken = tokenMatchSrcToDst.get(srcToken);
            if (wrongDstToken != null){
                ITree dstStmt = dstTreeTokenMap.getStmtOfTokenRange(wrongDstToken);
                UniversalTreeNode tmpDstUniversalNode = UniversalTreeNode.getUniversalTreeNode(dstStmt);
                if (tmpDstUniversalNode != sm.getDstUniversalStmt()){
                    StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(tmpDstUniversalNode, false);
                    impactStmtMatchSet.add(impactedMatch);
                    impactedMatch.setDstReferenceErrorMatch(sm, comparisonTypeForSameStmt, -1);
                }
            }
        } else {
            moreTokenMatchInSameStmtMapDstToSrc.put(dstToken, srcToken);
            TokenRange wrongSrcToken = tokenMatchDstToSrc.get(dstToken);
            if (wrongSrcToken != null){
                ITree srcStmt = srcTreeTokenMap.getStmtOfTokenRange(wrongSrcToken);
                UniversalTreeNode tmpSrcUniversalNode = UniversalTreeNode.getUniversalTreeNode(srcStmt);
                if (tmpSrcUniversalNode != sm.getSrcUniversalStmt()){
                    StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(tmpSrcUniversalNode, true);
                    impactStmtMatchSet.add(impactedMatch);
                    impactedMatch.setSrcReferenceErrorMatch(sm, comparisonTypeForSameStmt, -1);
                }
            }
        }
    }

    public void setTokenComparisonTypeMap(Map<TokenRange, Integer> srcTokenComparisonTypeMap,
                                          Map<TokenRange, Integer> dstTokenComparisonTypeMap,
                                          TreeTokensMap srcTreeTokenMap, TreeTokensMap dstTreeTokenMap,
                                          StmtMatchMaps currentMatchMaps){
        this.srcTokenComparisonTypeMap = srcTokenComparisonTypeMap;
        this.dstTokenComparisonTypeMap = dstTokenComparisonTypeMap;

        for (TokenRange srcToken: srcTokenComparisonTypeMap.keySet()){
            TokenRange mappedToken = tokenMatchSrcToDst.get(srcToken);
            if (mappedToken != null) {
                ITree dstStmt = dstTreeTokenMap.getStmtOfTokenRange(mappedToken);
                UniversalTreeNode tmpDstUniversalNode = UniversalTreeNode.getUniversalTreeNode(dstStmt);
                if (tmpDstUniversalNode != sm.getDstUniversalStmt()){
                    StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(tmpDstUniversalNode, false);
                    impactStmtMatchSet.add(impactedMatch);
                    impactedMatch.setDstReferenceErrorMatch(sm, -1, srcTokenComparisonTypeMap.get(srcToken));
                }
            }
        }

        for (TokenRange dstToken: dstTokenComparisonTypeMap.keySet()){
            TokenRange mappedToken = tokenMatchDstToSrc.get(dstToken);
            if (mappedToken != null){
                ITree srcStmt = srcTreeTokenMap.getStmtOfTokenRange(mappedToken);
                UniversalTreeNode tmpSrcUniversalNode = UniversalTreeNode.getUniversalTreeNode(srcStmt);
                if (tmpSrcUniversalNode != sm.getSrcUniversalStmt()){
                    StmtMatch impactedMatch = currentMatchMaps.getMatchingForStmt(tmpSrcUniversalNode, true);
                    impactStmtMatchSet.add(impactedMatch);
                    impactedMatch.setSrcReferenceErrorMatch(sm, -1, dstTokenComparisonTypeMap.get(dstToken));
                }
            }
        }
    }

    private String getBadTokenMapString(TreeTokensMap srcTtMap,
                                        TreeTokensMap dstTtMap,
                                        boolean isSrc){
        String ret = "";
        if (isSrc){
            for (TokenRange range: srcTokens){
                if (srcTokenComparisonTypeMap.containsKey(range)){
                    int type = srcTokenComparisonTypeMap.get(range);
                    ret += '(' + range.toPositionString(srcTtMap) + ')' + range.toString(srcTtMap);
                    ret += ": " + TokenComparisonType.getComparisonTypeString(type) + "\n";
                }
            }
        } else {
            for (TokenRange range: dstTokens){
                if (dstTokenComparisonTypeMap.containsKey(range)){
                    int type = dstTokenComparisonTypeMap.get(range);
                    ret += '(' + range.toPositionString(dstTtMap) + ')' + range.toString(dstTtMap);
                    ret += ": " + TokenComparisonType.getComparisonTypeString(type) + "\n";
                }
            }
        }
        return ret;
    }

    public String toString(TreeTokensMap srcTtMap, TreeTokensMap dstTtMap){
        String ret = "";
        String FIND_BETTER_SRC_STMT = "FIND BETTER SRC STMT";
        String FIND_BETTER_DST_STMT = "FIND BETTER DST STMT";
        String FIND_UNLIKELY_MATCHED_STMT = "UNLIKELY MATCHED STMT";
        String FIND_UNLIKELY_MATCHED_TOKEN = "FIND UNLIKELY MATCHED TOKEN";
        String srcStmtStr = UniversalTreeNode.getStmtLocationStr(sm.getSrcUniversalStmt(), srcTtMap);
        String dstStmtStr = UniversalTreeNode.getStmtLocationStr(sm.getDstUniversalStmt(), dstTtMap);
        String betterSrcStmtStr = UniversalTreeNode.getStmtLocationStr(betterSrcStmt, srcTtMap);
        String betterDstStmtStr = UniversalTreeNode.getStmtLocationStr(betterDstStmt, dstTtMap);

        if (BETTER_STMT_MATCH.equals(errorType)){
            if (betterDstStmtStr != null){
                ret += "REASON TYPE: ";
                ret += FIND_BETTER_DST_STMT + "\n";
                ret += "Reason: " + StmtComparisonType.getComparisonType(comparisonTypeForSrcStmt) + "\n";
                ret += compareAlgorithm + ": " + srcStmtStr + " => " + betterDstStmtStr + "\n";
                ret += "\n";
            }

            if (betterSrcStmtStr != null){
                ret += "REASON TYPE: ";
                ret += FIND_BETTER_SRC_STMT + "\n";
                ret += "Reason: " + StmtComparisonType.getComparisonType(comparisonTypeForDstStmt) + "\n";
                ret += compareAlgorithm + ": " + betterSrcStmtStr + " => " + dstStmtStr + "\n";
                ret += "\n";
            }

        } else if (UNLIKELY_MATCHED_STMT.equals(errorType)) {
            ret += "REASON TYPE: ";
            ret += FIND_UNLIKELY_MATCHED_STMT + "\n";
            ret += "\n";
        } else if (UNLIKELY_TOKEN_MATCH.equals(errorType)){
            ret += "REASON TYPE: ";
            ret += "Reason: " + FIND_UNLIKELY_MATCHED_TOKEN + "\n";
        } else {
            ret += "REASON_TYPE: " + BETTER_TOKEN_MATCH + "\n";
            ret += "Reason: we can find better match\n";
            ret += "From src statement:\n";
            ret += getBadTokenMapString(srcTtMap, dstTtMap, true);
            ret += "From dst statement:\n";
            ret += getBadTokenMapString(srcTtMap, dstTtMap, false);
        }

        if (containsSrcTokenBadMatched){
            ret += "Some tokens of src statement may be badly matched.\n";
        }
        if (containsDstTokenBadMatched){
            ret += "Some tokens of dst statement may be badly matched.\n";
        }

        if (impactStmtMatchSet.size() > 0){
            ret += "\nImpacted Match Set: \n";
            for (StmtMatch tmpSm: impactStmtMatchSet){
                ret += tmpSm + "\n";
            }
        }
        return ret;
    }
}
