package cs.zju.eva.matchfw.result;

import cs.zju.eva.utils.FileRevision;

public class ComparisonRecord {
    private String commitId;
    private String filePath;
    private String algorithm;
    private String stmtType;
    private int srcCharPos;
    private int dstCharPos;
    private int srcStmtLine;
    private int dstStmtLine;
    private String comparedAlgorithm;
    private String diffType;
    private int otherSrcStmtLine;
    private int otherDstStmtLine;
    private double ratioOfIdenticalToken;
    private String betterAlgorithm;
    private int errorType;

    private int stmtBadMatch;
    private int srcTokenBadMatch;
    private int dstTokenBadMatch;

    private int srcTokens;
    private int dstTokens;
    private int srcSameMatch;
    private int dstSameMatch;
    private int srcStmtOrTokenError;
    private int dstStmtOrTokenError;
    private int srcCrossStmtMatchToken;
    private int dstCrossStmtMatchToken;
    private int stmtErrorType;
    private int srcTokenErrorType;
    private int dstTokenErrorType;

    public ComparisonRecord(String[] csvRecord, boolean isResult, boolean isNewResult){
        commitId = csvRecord[0];
        filePath = csvRecord[1];
        algorithm = csvRecord[2];
        stmtType = csvRecord[3];
        srcCharPos = Integer.parseInt(csvRecord[4]);
        dstCharPos = Integer.parseInt(csvRecord[5]);
        srcStmtLine = Integer.parseInt(csvRecord[6]);
        dstStmtLine = Integer.parseInt(csvRecord[7]);
        comparedAlgorithm = csvRecord[8];
        diffType = csvRecord[9];
        otherDstStmtLine = Integer.parseInt(csvRecord[11]);
        otherSrcStmtLine = Integer.parseInt(csvRecord[12]);
        ratioOfIdenticalToken = Double.parseDouble(csvRecord[14]);
        betterAlgorithm = csvRecord[15];
        if (csvRecord[16] != null && !csvRecord[16].equals(""))
            errorType = Integer.parseInt(csvRecord[16]);
        else
            errorType = -1;

        if (isResult) {
            stmtBadMatch = Integer.parseInt(csvRecord[17]);
            srcTokenBadMatch = Integer.parseInt(csvRecord[20]);
            dstTokenBadMatch = Integer.parseInt(csvRecord[21]);
        }

        if (isNewResult){
            srcTokens = Integer.parseInt(csvRecord[17]);
            dstTokens = Integer.parseInt(csvRecord[18]);
            srcSameMatch = Integer.parseInt(csvRecord[19]);
            dstSameMatch = Integer.parseInt(csvRecord[20]);
            srcStmtOrTokenError = Integer.parseInt(csvRecord[21]);
            dstStmtOrTokenError = Integer.parseInt(csvRecord[22]);
            srcCrossStmtMatchToken = Integer.parseInt(csvRecord[23]);
            dstCrossStmtMatchToken = Integer.parseInt(csvRecord[24]);
            srcTokenErrorType = Integer.parseInt(csvRecord[25]);
            dstTokenErrorType = Integer.parseInt(csvRecord[26]);
        }
    }

    public String getCommitId() {
        return commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getStmtType() {
        return stmtType;
    }

    public int getSrcCharPos() {
        return srcCharPos;
    }

    public int getDstCharPos() {
        return dstCharPos;
    }

    public int getSrcStmtLine() {
        return srcStmtLine;
    }

    public int getDstStmtLine() {
        return dstStmtLine;
    }

    public String getComparedAlgorithm() {
        return comparedAlgorithm;
    }

    public String getDiffType() {
        return diffType;
    }

    public int getOtherDstStmtLine() {
        return otherDstStmtLine;
    }

    public int getOtherSrcStmtLine() {
        return otherSrcStmtLine;
    }

    public String getBetterAlgorithm() {
        return betterAlgorithm;
    }

    public double getRatioOfIdenticalToken() {
        return ratioOfIdenticalToken;
    }

    public int getErrorType() {
        return errorType;
    }

    public FileRevision getFileRevision(){
        return new FileRevision(commitId, filePath);
    }

    public boolean isError(){
        return getErrorType() != -1;
    }

    public int getStmtBadMatch() {
        return stmtBadMatch;
    }

    public int getSrcTokenBadMatch() {
        return srcTokenBadMatch;
    }

    public int getDstTokenBadMatch() {
        return dstTokenBadMatch;
    }

    public int getSrcSameMatch() {
        return srcSameMatch;
    }

    public int getDstSameMatch() {
        return dstSameMatch;
    }

    public int getSrcTokens() {
        return srcTokens;
    }

    public int getDstTokens() {
        return dstTokens;
    }

    public void setSrcTokens(int srcTokens) {
        this.srcTokens = srcTokens;
    }

    public void setDstTokens(int dstTokens) {
        this.dstTokens = dstTokens;
    }

    public void setSrcSameMatch(int srcSameMatch) {
        this.srcSameMatch = srcSameMatch;
    }

    public void setDstSameMatch(int dstSameMatch) {
        this.dstSameMatch = dstSameMatch;
    }

    public void setSrcStmtOrTokenError(int srcStmtOrTokenError) {
        this.srcStmtOrTokenError = srcStmtOrTokenError;
    }

    public void setDstStmtOrTokenError(int dstStmtOrTokenError) {
        this.dstStmtOrTokenError = dstStmtOrTokenError;
    }

    public void setSrcCrossStmtMatchToken(int srcCrossStmtMatchToken) {
        this.srcCrossStmtMatchToken = srcCrossStmtMatchToken;
    }

    public void setDstCrossStmtMatchToken(int dstCrossStmtMatchToken) {
        this.dstCrossStmtMatchToken = dstCrossStmtMatchToken;
    }

    public void setStmtErrorType(int stmtErrorType) {
        this.stmtErrorType = stmtErrorType;
    }

    public void setSrcTokenErrorType(int srcTokenErrorType) {
        this.srcTokenErrorType = srcTokenErrorType;
    }

    public void setDstTokenErrorType(int dstTokenErrorType) {
        this.dstTokenErrorType = dstTokenErrorType;
    }

    public int getSrcStmtOrTokenError() {
        return srcStmtOrTokenError;
    }

    public int getDstStmtOrTokenError() {
        return dstStmtOrTokenError;
    }

    public int getSrcCrossStmtMatchToken() {
        return srcCrossStmtMatchToken;
    }

    public int getDstCrossStmtMatchToken() {
        return dstCrossStmtMatchToken;
    }

    public boolean isStmtOrTokenError(boolean isSrc){
        if (isSrc)
            return getSrcStmtOrTokenError() == 1;
        else
            return getDstStmtOrTokenError() == 1;
    }

    public int getStmtErrorType() {
        return stmtErrorType;
    }

    public int getSrcTokenErrorType() {
        return srcTokenErrorType;
    }

    public int getDstTokenErrorType() {
        return dstTokenErrorType;
    }

    public String getStmtTokenErrorType(boolean isSrc){
        if (isSrc && getSrcStmtOrTokenError() == 1){
            if (errorType == 4 || errorType == 7)
                return "NIT";
            if (errorType == 3)
                return "NCT";
            if (errorType == 2 || errorType == 5)
                return "PM";
            if (srcTokenErrorType == 4)
                return "TYPE";
            if (srcTokenErrorType == 3)
                return "STMT";
            if (srcTokenErrorType == 2)
                return "VAL";
            if (srcTokenErrorType == 1)
                return "LLCS";
        }
        if (!isSrc && getDstStmtOrTokenError() == 1){
            if (errorType == 4 || errorType == 7)
                return "NIT";
            if (errorType == 3)
                return "NCT";
            if (errorType == 2 || errorType == 5)
                return "PM";
            if (dstTokenErrorType == 4)
                return "TYPE";
            if (dstTokenErrorType == 3)
                return "STMT";
            if (dstTokenErrorType == 2)
                return "VAL";
            if (dstTokenErrorType == 1)
                return "LLCS";
        }
        return "";
    }
}
