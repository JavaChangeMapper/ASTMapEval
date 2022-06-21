package cs.zju.stm.match;

import com.github.gumtreediff.tree.ITree;
import cs.zju.framework.match.UniversalTreeNode;
import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StmtMatchDiffDesc {
    public static final String DIFF_AT_STMT = "DIFF MATCH AT STMT";
    public static final String DIFF_AT_TOKEN = "DIFF MATCH AT TOKEN";

    private static final int columnLength = 80;
    private String currentMatchAlgorithm;
    private String compareAlgorithm;
    private String type;
    private UniversalTreeNode srcUniversalStmt;
    private UniversalTreeNode dstUniversalStmt;

    private List<TokenRange> srcTokens;
    private List<TokenRange> dstTokens;
    private Map<TokenRange, TokenRange> tokenMapSrcToDst;
    private Map<TokenRange, TokenRange> tokenMapDstToSrc;
    private UniversalTreeNode otherSrcStmt;
    private UniversalTreeNode otherDstStmt;
    private Map<TokenRange, TokenRange> srcDiffMatchTokenMap;
    private Map<TokenRange, TokenRange> dstDiffMatchTokenMap;
    private String stmtType;

    private StmtMatch sm;
    private TreeTokensMap srcTtMap;
    private TreeTokensMap dstTtMap;
    private StmtMatchErrorDesc errorDesc = null;

    public StmtMatchDiffDesc(StmtMatch sm){
        this.sm = sm;
        srcDiffMatchTokenMap = new HashMap<>();
        dstDiffMatchTokenMap = new HashMap<>();
        this.type = "";
        this.srcUniversalStmt = sm.getSrcUniversalStmt();
        this.dstUniversalStmt = sm.getDstUniversalStmt();
        this.srcTokens = sm.getSrcTokens();
        this.dstTokens = sm.getDstTokens();
        this.tokenMapSrcToDst = sm.getTokenMapSrcToDst();
        this.tokenMapDstToSrc = sm.getTokenMapDstToSrc();
        this.srcTtMap = sm.getSrcTreeTokenMap();
        this.dstTtMap = sm.getDstTreeTokenMap();
        if (!sm.getSrcUniversalStmt().isNull())
            stmtType = sm.getSrcUniversalStmt().getNodeType();
        else
            stmtType = sm.getDstUniversalStmt().getNodeType();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOtherSrcStmt(UniversalTreeNode otherSrcStmt){
        if (srcUniversalStmt != otherSrcStmt)
            this.otherSrcStmt = otherSrcStmt;
    }

    public void setOtherDstStmt(UniversalTreeNode otherDstStmt){
        if (dstUniversalStmt != otherDstStmt)
            this.otherDstStmt = otherDstStmt;
    }

    private boolean stmtSameMatch(boolean isSrc){
        if (isSrc){
            if (otherDstStmt != null)
                return false;
            for (TokenRange srcToken: srcTokens){
                if (!TokenRange.isEqualTo(tokenMapSrcToDst.get(srcToken), srcDiffMatchTokenMap.get(srcToken)))
                    return false;
            }
        } else {
            if (otherSrcStmt != null)
                return false;
            for (TokenRange dstToken: dstTokens){
                if (!TokenRange.isEqualTo(tokenMapDstToSrc.get(dstToken), dstDiffMatchTokenMap.get(dstToken)))
                    return false;
            }
        }
        return true;
    }

    // map到其他statement, 有多少存在问题
    private boolean isMoveTokenFromSrcStmt(){
        for (TokenRange srcToken: srcTokens){
            TokenRange mappedToken = tokenMapSrcToDst.get(srcToken);
            if (mappedToken != null) {
                ITree dstStmt = dstTtMap.getStmtOfTokenRange(mappedToken);
                UniversalTreeNode tmpUniversalNode = UniversalTreeNode.getUniversalTreeNode(dstStmt);
                if (tmpUniversalNode != dstUniversalStmt){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMoveTokenToDstStmt(){
        for (TokenRange dstToken: dstTokens){
            TokenRange mappedToken = tokenMapDstToSrc.get(dstToken);
            if (mappedToken != null) {
                ITree srcStmt = srcTtMap.getStmtOfTokenRange(mappedToken);
                UniversalTreeNode tmpUniversalNode = UniversalTreeNode.getUniversalTreeNode(srcStmt);
                if (tmpUniversalNode != srcUniversalStmt)
                    return true;
            }
        }
        return false;
    }

    public void addOtherMatchForSrcToken(TokenRange srcToken, TokenRange dstToken){
        if ("".equals(type) && !TokenRange.isEqualTo(dstToken, tokenMapSrcToDst.get(srcToken)))
            type = DIFF_AT_TOKEN;
        srcDiffMatchTokenMap.put(srcToken, dstToken);
    }

    public void addOtherMatchForDstToken(TokenRange dstToken, TokenRange srcToken){
        if ("".equals(type) && !TokenRange.isEqualTo(srcToken, tokenMapDstToSrc.get(dstToken)))
            type = DIFF_AT_TOKEN;
        dstDiffMatchTokenMap.put(dstToken, srcToken);
    }

    public void setCurrentMatchAlgorithm(String currentMatchAlgorithm) {
        this.currentMatchAlgorithm = currentMatchAlgorithm.toUpperCase();
    }

    public void setCompareAlgorithm(String compareAlgorithm) {
        this.compareAlgorithm = compareAlgorithm.toUpperCase();
    }

    private String generateBlanks(String startStr){
        if (startStr.length() < columnLength){
            String ret = "";
            for (int i = 0; i < columnLength - startStr.length(); i++)
                ret += " ";
            return ret;
        }
        return "\t";
    }

    private String tokenMatchToString(boolean isSrc){
        String ret = "";
        List<TokenRange> tokens = srcTokens;
        if (!isSrc)
            tokens = dstTokens;
        for (TokenRange token: tokens){
            String tmpStr = currentMatchAlgorithm + ": " + getTokenMapString(token, isSrc, true);
            ret += tmpStr;
            ret += generateBlanks(tmpStr);
            ret += compareAlgorithm + ": " + getTokenMapString(token, isSrc, false);
            ret += "\n";
        }
        return ret;
    }

    private String getTokenMapString(TokenRange token, boolean isSrc, boolean isCurrentAlgorithm){
        if (isSrc){
            TokenRange dstToken;
            if (isCurrentAlgorithm)
                dstToken = tokenMapSrcToDst.get(token);
            else
                dstToken = srcDiffMatchTokenMap.get(token);
            if (dstToken == null) {
                if (isCurrentAlgorithm ||
                        !TokenRange.isEqualTo(tokenMapSrcToDst.get(token), srcDiffMatchTokenMap.get(token))) {
                    return "(" + token.toPositionString(srcTtMap) +
                    " => null) " + token.toString(srcTtMap) + " => ";
                } else {
                    return "SAME MATCHING";
                }
            }

            if (!isCurrentAlgorithm){
                if (TokenRange.isEqualTo(tokenMapSrcToDst.get(token), srcDiffMatchTokenMap.get(token)))
                    return "SAME MATCHING";
            }

            return "(" + token.toPositionString(srcTtMap) + ") =>" +
                    " (" + dstToken.toPositionString(dstTtMap) + ") "
                    + token.toString(srcTtMap) + " => " + dstToken.toString(dstTtMap);
        } else {
            TokenRange srcToken;
            if (isCurrentAlgorithm)
                srcToken = tokenMapDstToSrc.get(token);
            else
                srcToken = dstDiffMatchTokenMap.get(token);
            if (srcToken == null) {
                if (isCurrentAlgorithm ||
                        !TokenRange.isEqualTo(tokenMapDstToSrc.get(token), dstDiffMatchTokenMap.get(token))) {
                    return  "(null => " + token.toPositionString(dstTtMap) + ")" +
                            " => " + token.toString(dstTtMap);
                } else {
                    return "SAME MATCHING";
                }
            }
            if (!isCurrentAlgorithm){
                if (TokenRange.isEqualTo(tokenMapDstToSrc.get(token), dstDiffMatchTokenMap.get(token)))
                    return "SAME MATCHING";
            }
            return " (" + srcToken.toPositionString(srcTtMap) + ") =>" +
                    " (" + token.toPositionString(dstTtMap) + ") " +
                    srcToken.toString(srcTtMap) + " => " + token.toString(dstTtMap);
        }
    }

    public String toString(){
        String ret = "";

        String srcStmtStr = UniversalTreeNode.getStmtLocationStr(srcUniversalStmt, srcTtMap);
        String dstStmtStr = UniversalTreeNode.getStmtLocationStr(dstUniversalStmt, dstTtMap);
        String otherDstStmtStr = UniversalTreeNode.getStmtLocationStr(otherDstStmt, dstTtMap);
        String otherSrcStmtStr = UniversalTreeNode.getStmtLocationStr(otherSrcStmt, srcTtMap);

        if (DIFF_AT_STMT.equals(type)){
            ret += "=====================================================================\n";
            ret += "Current: " + currentMatchAlgorithm + "\tCompare: " + compareAlgorithm + "\n";
            ret += sm + "\n";
            ret += DIFF_AT_STMT + "\n";
            if (srcStmtStr != null || otherDstStmtStr != null)
                ret += compareAlgorithm + ": " + srcStmtStr + " => " + otherDstStmtStr + "\n";
            if (otherSrcStmtStr != null || dstStmtStr != null)
                ret += compareAlgorithm + ": " + otherSrcStmtStr + " => " + dstStmtStr + "\n\n";
        } else {
            ret += "=====================================================================\n";
            ret += "Current: " + currentMatchAlgorithm + "\tCompare: " + compareAlgorithm + "\n";
            ret += sm + "\n";
            ret += DIFF_AT_TOKEN + "\n\n";
        }

        if (srcUniversalStmt != null)
            ret += "From src statement: " + srcStmtStr + "\n";
        ret += tokenMatchToString(true);

        if (dstUniversalStmt != null) {
            ret += "\n";
            ret += "From dst statement: " + dstStmtStr + "\n";
        }

        ret += tokenMatchToString(false);

        if (errorDesc != null) {
            ret += "\n";
            ret += errorDesc.toString(srcTtMap, dstTtMap) + "\n";
        } else {
            if (sm.getReferenceErrorType() != -1){
                ret += "\n";
                if (sm.getSrcReferenceErrorMatch() != null) {
                    ret += "SRC STMT REFERENCE ERROR:\n";
                    ret += sm.getSrcReferenceErrorMatch();
                }
                if (sm.getDstReferenceErrorMatch() != null){
                    ret += "DST STMT REFERENCE ERROR:\n";
                    ret += sm.getDstReferenceErrorMatch();
                }
            }
        }
        return ret;
    }

    public void setErrorDesc(StmtMatchErrorDesc errorDesc) {
        this.errorDesc = errorDesc;
    }

    public StmtMatchErrorDesc getErrorDesc() {
        return errorDesc;
    }

    public UniversalTreeNode getSrcUniversalStmt() {
        return srcUniversalStmt;
    }

    public UniversalTreeNode getDstUniversalStmt() {
        return dstUniversalStmt;
    }

    public StmtMatch getStmtMatch() {
        return sm;
    }

    public boolean isSrcStmtOrTokenMapError(){
        if (errorDesc != null && errorDesc.srcStmtOrTokenMatchError())
            return true;
        if (sm.getSrcReferenceErrorMatch() != null)
            return true;
        return false;
    }

    public boolean isDstStmtOrTokenMapError(){
        if (errorDesc != null && errorDesc.dstStmtOrTokenMatchError())
            return true;
        if (sm.getDstReferenceErrorMatch() != null)
            return true;
        return false;
    }

    public String[] toCsvRecord(String commitId, String filePath){
        int srcStmtLine = -1;
        int dstStmtLine = -1;
        int otherDstStmtLine = -1;
        int otherSrcStmtLine = -1;
        String betterAlgorithm = "";
        int errorType = -1;
        if (srcUniversalStmt != null && !srcUniversalStmt.isNull())
            srcStmtLine = srcTtMap.getStartLineOfStmt(srcUniversalStmt.getPos());
        if (dstUniversalStmt != null && !dstUniversalStmt.isNull())
            dstStmtLine = dstTtMap.getStartLineOfStmt(dstUniversalStmt.getPos());
        if (otherDstStmt != null && !otherDstStmt.isNull())
            otherDstStmtLine = dstTtMap.getStartLineOfStmt(otherDstStmt.getPos());
        if (otherSrcStmt != null && !otherSrcStmt.isNull())
            otherSrcStmtLine = srcTtMap.getStartLineOfStmt(otherSrcStmt.getPos());
        if (errorDesc != null || sm.getReferenceErrorType() != -1) {
            betterAlgorithm = "compare";
            if (errorDesc != null){
                String tmpType = errorDesc.getErrorType();
                if (tmpType.equals(StmtMatchErrorDesc.BETTER_STMT_MATCH)){
                    if (errorDesc.getComparisonTypeForSrcStmt() != -100 ){
                        errorType = StmtComparisonType.getSimpleErrorType(errorDesc.getComparisonTypeForSrcStmt());
                    } else {
                        errorType = StmtComparisonType.getSimpleErrorType(errorDesc.getComparisonTypeForDstStmt());
                    }
                } else if (tmpType.equals(StmtMatchErrorDesc.BETTER_TOKEN_MATCH)){
                    errorType = StmtComparisonType.getSimpleErrorType(errorDesc.getBadTokenType());
                } else if (tmpType.equals(StmtMatchErrorDesc.UNLIKELY_MATCHED_STMT)){
                    betterAlgorithm = "";
                    errorType = StmtComparisonType.getSimpleErrorType(errorDesc.getComparisonTypeForSrcStmt());
                } else if (tmpType.equals(StmtMatchErrorDesc.UNLIKELY_TOKEN_MATCH)){
                    betterAlgorithm = "";
                    if (errorDesc.getComparisonTypeForSrcStmt() == StmtComparisonType.SRC_CHANGE_TYPE_AND_VALUE &&
                        errorDesc.getComparisonTypeForDstStmt() == StmtComparisonType.DST_CHANGE_TYPE_AND_VALUE){
                        errorType = StmtComparisonType.BOTH_CHANGE_TYPE_AND_VALUE;
                    } else if (errorDesc.getComparisonTypeForSrcStmt() == StmtComparisonType.SRC_CHANGE_TYPE_AND_VALUE){
                        errorType = StmtComparisonType.SRC_CHANGE_TYPE_AND_VALUE;
                    } else if (errorDesc.getComparisonTypeForDstStmt() == StmtComparisonType.DST_CHANGE_TYPE_AND_VALUE){
                        errorType = StmtComparisonType.DST_CHANGE_TYPE_AND_VALUE;
                    }
                }
            } else {
                betterAlgorithm = "";
                errorType = sm.getReferenceErrorType();
            }
        }

        List<String> record = new ArrayList<>();
        record.add(commitId);
        record.add(filePath);
        record.add(currentMatchAlgorithm);
        record.add(stmtType);
        record.add(Integer.toString(srcUniversalStmt.getPos()));
        record.add(Integer.toString(dstUniversalStmt.getPos()));
        record.add(Integer.toString(srcStmtLine));
        record.add(Integer.toString(dstStmtLine));

        record.add(compareAlgorithm);
        record.add(type);
        if (type.equals(DIFF_AT_STMT)) {
            record.add(Integer.toString(srcStmtLine));
            record.add(Integer.toString(otherDstStmtLine));
            record.add(Integer.toString(otherSrcStmtLine));
            record.add(Integer.toString(dstStmtLine));
        } else {
            record.add(Integer. toString(-1));
            record.add(Integer. toString(-1));
            record.add(Integer. toString(-1));
            record.add(Integer. toString(-1));
        }
        record.add(Double.toString(sm.getRatioOfIdenticalTokens()));
        record.add(betterAlgorithm);
        if (errorType == -1)
            record.add("0");
        else
            record.add(Integer.toString(errorType));
        record.add(Integer.toString(srcTokens.size()));
        record.add(Integer.toString(dstTokens.size()));

        if (stmtSameMatch(true))
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));
        if (stmtSameMatch(false))
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));

        if (isSrcStmtOrTokenMapError())
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));

        if (isDstStmtOrTokenMapError())
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));

        if (isMoveTokenFromSrcStmt())
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));

        if (isMoveTokenToDstStmt())
            record.add(Integer.toString(1));
        else
            record.add(Integer.toString(0));

        record.add(Integer.toString(getSrcTokenMatchErrorType(errorDesc)));
        record.add(Integer.toString(getDstTokenMatchErrorType(errorDesc)));
        return record.toArray(new String[record.size()]);
    }

    private int getSrcTokenMatchErrorType(StmtMatchErrorDesc desc){
        int type = -1;
        if (desc != null)
            type = desc.getSrcTokenMatchErrorType();
        if (type == -1 && sm.getSrcTokenRefErrorType() != -1)
            type = sm.getSrcTokenRefErrorType();
        return type;
    }

    private int getDstTokenMatchErrorType(StmtMatchErrorDesc desc){
        int type = -1;
        if (desc != null)
            type = desc.getDstTokenMatchErrorType();
        if (type == -1 && sm.getDstTokenRefErrorType() != -1){
            type = sm.getDstTokenRefErrorType();
        }
        return type;
    }

    public static String[] getHeaders(){
        String[] headers = {
                "commitId", "filepath", "algorithm", "stmtType", "srcCharPos", "dstCharPos", "srcStmtLine", "dstStmtLine",
                "compareAlgorithm", "diffType", "srcStmtLine2", "dstStmtLine2", "srcStmtLine3", "dstStmtLine3", "ratioOfIdenticalToken",
                "betterAlgorithm", "errorType","srcTokenNum", "dstTokeNum", "srcSameMatch", "dstSameMatch",
                "srcStmtTokenError", "dstStmtTokenError", "srcCrossStmtMatch", "dstCrossStmtMatch",
                "srcStmtTokenErrorType", "dstStmtTokenErrorType"
        };
        return headers;
    }
}
