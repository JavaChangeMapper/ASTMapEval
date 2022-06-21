package cs.zju.eva.editfw.metrics.accuracy;

import cs.zju.eva.utils.FileRevision;

import java.util.ArrayList;
import java.util.List;

public class StmtRecord {
    private FileRevision revision;
    private int srcStartLine;
    private int dstStartLine;
    private int srcStartPos;
    private int dstStartPos;
    private String changeType;
    private String stmtType;
    private int edits;
    private int moveEdits;
    private boolean moreEditsThanOther;
    private int shortStmtTokenCount;
    private int sameTokenCount;
    private boolean hasMultipleCommunications;
    private boolean hasLowQualityMove;
    private boolean hasEditsInconsistentWithDiff;

    public StmtRecord(FileRevision revision, int srcStartLine, int dstStartLine,
                      int srcStartPos, int dstStartPos,
                      String changeType,
                      String stmtType, int edits, int moveEdits,
                      boolean hasMultipleCommunications,
                      boolean hasLowQualityMove, boolean hasEditsInconsistentWithDiff,
                      int shortStmtTokenCount, int sameTokenCount){
        this.revision = revision;
        this.srcStartLine = srcStartLine;
        this.dstStartLine = dstStartLine;
        this.srcStartPos = srcStartPos;
        this.dstStartPos = dstStartPos;
        this.changeType = changeType;
        this.stmtType = stmtType;
        this.edits = edits;
        this.moveEdits = moveEdits;
        this.hasMultipleCommunications = hasMultipleCommunications;
        this.hasLowQualityMove = hasLowQualityMove;
        this.hasEditsInconsistentWithDiff = hasEditsInconsistentWithDiff;
        this.shortStmtTokenCount = shortStmtTokenCount;
        this.sameTokenCount = sameTokenCount;
    }

    public FileRevision getRevision() {
        return revision;
    }

    public int getSrcStartLine() {
        return srcStartLine;
    }

    public int getDstStartLine() {
        return dstStartLine;
    }

    public int getSrcStartPos() {
        return srcStartPos;
    }

    public int getDstStartPos() {
        return dstStartPos;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getStmtType() {
        return stmtType;
    }

    public int getEdits() {
        return edits;
    }

    public int getMoveEdits() {
        return moveEdits;
    }

    public boolean isHasMultipleCommunications() {
        return hasMultipleCommunications;
    }

    public boolean isHasLowQualityMove() {
        return hasLowQualityMove;
    }

    public boolean isHasEditsInconsistentWithDiff() {
        return hasEditsInconsistentWithDiff;
    }

    public int getShortStmtTokenCount() {
        return shortStmtTokenCount;
    }

    public double matchRatio(){
        if (shortStmtTokenCount == 0)
            return 10;
        double ratio = (double) sameTokenCount / shortStmtTokenCount;
        return ratio;
    }

    public boolean isTestedByCEC(){
        return shortStmtTokenCount > 3 && edits > 0;
    }

    public boolean isSuspiciousByCEC(double lowerMAD){
        return shortStmtTokenCount > 3 && matchRatio() == 0;
    }

    public boolean isSuspiciousBySDC(){
        return moreEditsThanOther;
    }

    public boolean isSuspiciousByMMTC(){
        return hasLowQualityMove;
    }

    public boolean isSuspicious(double lowerMAD){
        return isSuspiciousByCEC(lowerMAD) || isSuspiciousBySDC() || isSuspiciousByMMTC();
    }

    public void setMoreEditsThanOther() {
        this.moreEditsThanOther = true;
    }

    public String[] getRecord(double lowerMADRatio){
        List<String> record = new ArrayList<>();
        record.add(revision.first);
        record.add(revision.second);
        record.add(stmtType);
        record.add(changeType);
        record.add(Integer.toString(srcStartLine));
        record.add(Integer.toString(dstStartLine));
        record.add(Integer.toString(srcStartPos));
        record.add(Integer.toString(dstStartPos));
        record.add(Integer.toString(shortStmtTokenCount));
        record.add(Integer.toString(edits));
        record.add(Integer.toString(isHasLowQualityMove()? 1 : 0));
        record.add(Integer.toString(isSuspiciousBySDC() ? 1:0));
        record.add(Double.toString(matchRatio()));
        return record.toArray(new String[record.size()]);
    }

    @Override
    public String toString() {
        return "StmtRecord{" +
                "revision=" + revision +
                ", srcStartLine=" + srcStartLine +
                ", dstStartLine=" + dstStartLine +
                ", changeType='" + changeType + '\'' +
                ", stmtType='" + stmtType + '\'' +
                ", edits=" + edits +
                ", moveEdits=" + moveEdits +
                ", moreEditsThanOther=" + moreEditsThanOther +
                ", shortStmtTokenCount=" + shortStmtTokenCount +
                ", sameTokenCount=" + sameTokenCount +
                ", hasMultipleCommunications=" + hasMultipleCommunications +
                ", hasLowQualityMove=" + hasLowQualityMove +
                ", hasEditsInconsistentWithDiff=" + hasEditsInconsistentWithDiff +
                '}';
    }

    public static String[] getHeaders(){
        String[] headers = {
                "commitId", "srcFilePath", "stmtType", "stmtChangeType", "srcStartLine", "dstStartLine",
                "srcStartPos", "dstStartPos",
                "shortStmtTokenCount",
                "edits", "MMTC", "SDC", "CEC"
        };
        return headers;
    }
}
