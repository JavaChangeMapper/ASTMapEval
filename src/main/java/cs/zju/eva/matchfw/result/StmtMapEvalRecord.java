package cs.zju.eva.matchfw.result;

import cs.zju.eva.utils.FileRevision;

import java.util.*;

public class StmtMapEvalRecord {

    private static HashMap<StmtObj, StmtMapEvalRecord> stmtRecordMap = new HashMap<>();
    private static List<StmtObj> stmtList = new ArrayList<>();

    private FileRevision fileRevision;
    private StmtObj stmtObj;
    private int startLine;
    private int gtInaccurate = 0;
    private int mtdInaccurate = 0;
    private int ijmInaccurate = 0;
    private int gtInaccurateByMtd = 0;
    private int gtInaccurateByIjm = 0;
    private int mtdInaccurateByGt = 0;
    private int mtdInaccurateByIjm = 0;
    private int ijmInaccurateByGt = 0;
    private int ijmInaccurateByMtd = 0;
    private int gtMtdInconsistent = 0;
    private int gtIjmInconsistent = 0;
    private int mtdIjmInconsistent = 0;

    private String focusAlgorithm = "";
    private int fileId;

    public StmtMapEvalRecord(){
    }

    private boolean isNull(){
        return stmtObj.isNull();
    }

    public StmtObj getStmtObj() {
        return stmtObj;
    }

    public FileRevision getFileRevision() {
        return fileRevision;
    }

    public int getGtInaccurate() {
        return gtInaccurate;
    }

    public int getMtdInaccurate() {
        return mtdInaccurate;
    }

    public int getIjmInaccurate() {
        return ijmInaccurate;
    }

    public int getGtInaccurateByIjm() {
        return gtInaccurateByIjm;
    }

    public int getGtInaccurateByMtd() {
        return gtInaccurateByMtd;
    }

    public int getMtdInaccurateByGt() {
        return mtdInaccurateByGt;
    }

    public int getMtdInaccurateByIjm() {
        return mtdInaccurateByIjm;
    }

    public int getIjmInaccurateByGt() {
        return ijmInaccurateByGt;
    }

    public int getIjmInaccurateByMtd() {
        return ijmInaccurateByMtd;
    }

    public int getGtMtdInconsistent() {
        return gtMtdInconsistent;
    }

    public int getGtIjmInconsistent() {
        return gtIjmInconsistent;
    }

    public int getMtdIjmInconsistent() {
        return mtdIjmInconsistent;
    }

    public static StmtMapEvalRecord getRecordForStmtObj(StmtObj stmtObj){
        return stmtRecordMap.get(stmtObj);
    }

    public void setFocusAlgorithm(String focusAlgorithm) {
        this.focusAlgorithm = focusAlgorithm;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    private static void setInfo(StmtMapEvalRecord sr, ComparisonRecord record, boolean isSrc, String project){
        sr.fileRevision = new FileRevision(record.getCommitId(), record.getFilePath());
        sr.stmtObj = new StmtObj(record, isSrc, project);
        if (isSrc)
            sr.startLine = record.getSrcStmtLine();
        else
            sr.startLine = record.getDstStmtLine();
        if (isGtInaccurate(record, isSrc)) {
            sr.gtInaccurate = 1;
            if (record.getComparedAlgorithm().equals("MTDIFF"))
                sr.gtInaccurateByMtd = 1;
            if (record.getComparedAlgorithm().equals("IJM"))
                sr.gtInaccurateByIjm = 1;
        }
        if (isMtdInaccurate(record, isSrc)) {
            sr.mtdInaccurate = 1;
            if (record.getComparedAlgorithm().equals("GT"))
                sr.mtdInaccurateByGt = 1;
            if (record.getComparedAlgorithm().equals("IJM"))
                sr.mtdInaccurateByIjm = 1;
        }
        if (isIjmInaccurate(record, isSrc)) {
            sr.ijmInaccurate = 1;
            if (record.getComparedAlgorithm().equals("GT"))
                sr.ijmInaccurateByGt = 1;
            if (record.getComparedAlgorithm().equals("MTDIFF"))
                sr.ijmInaccurateByMtd = 1;
        }

        if (isGtMtdInconsistent(record, isSrc))
            sr.gtMtdInconsistent = 1;
        if (isGtIjmInconsistent(record, isSrc))
            sr.gtIjmInconsistent = 1;
        if (isMtdIjmInconsistent(record, isSrc))
            sr.mtdIjmInconsistent = 1;
    }

    private static boolean isGtMtdInconsistent(ComparisonRecord record, boolean isSrc){
        String algorithm1 = record.getAlgorithm();
        String algorithm2 = record.getComparedAlgorithm();
        Set<String> algorithms = new HashSet<>();
        algorithms.add("GT");
        algorithms.add("MTDIFF");

        if (algorithms.contains(algorithm1) && algorithms.contains(algorithm2)) {
            if (isSrc)
                return record.getSrcSameMatch() == 0;
            else
                return record.getDstSameMatch() == 0;
        }
        return false;
    }

    private static boolean isGtIjmInconsistent(ComparisonRecord record, boolean isSrc){
        String algorithm1 = record.getAlgorithm();
        String algorithm2 = record.getComparedAlgorithm();
        Set<String> algorithms = new HashSet<>();
        algorithms.add("GT");
        algorithms.add("IJM");
        if (algorithms.contains(algorithm1) && algorithms.contains(algorithm2)){
            if (isSrc)
                return record.getSrcSameMatch() == 0;
            else
                return record.getDstSameMatch() == 0;
        }
        return false;
    }

    private static boolean isMtdIjmInconsistent(ComparisonRecord record, boolean isSrc){
        String algorithm1 = record.getAlgorithm();
        String algorithm2 = record.getComparedAlgorithm();
        Set<String> algorithms = new HashSet<>();
        algorithms.add("MTDIFF");
        algorithms.add("IJM");
        if (algorithms.contains(algorithm1) && algorithms.contains(algorithm2)){
            if (isSrc)
                return record.getSrcSameMatch() == 0;
            else
                return record.getDstSameMatch() == 0;
        }
        return false;
    }

    private static boolean isGtInaccurate(ComparisonRecord record, boolean isSrc){
        if (record.getAlgorithm().equals("GT")){
            if (isSrc)
                return record.getSrcStmtOrTokenError() == 1;
            else
                return record.getDstStmtOrTokenError() == 1;
        }
        return false;
    }

    private static boolean isMtdInaccurate(ComparisonRecord record, boolean isSrc){
        if (record.getAlgorithm().equals("MTDIFF")){
            if (isSrc)
                return record.getSrcStmtOrTokenError() == 1;
            else
                return record.getDstStmtOrTokenError() == 1;
        }
        return false;
    }

    private static boolean isIjmInaccurate(ComparisonRecord record, boolean isSrc){
        if (record.getAlgorithm().equals("IJM")){
            if (isSrc)
                return record.getSrcStmtOrTokenError() == 1;
            else
                return record.getDstStmtOrTokenError() == 1;
        }
        return false;
    }

    public static StmtMapEvalRecord getRecordForStmt(String project, ComparisonRecord record, boolean isSrc){
        StmtObj obj = new StmtObj(record, isSrc, project);
        if (stmtRecordMap.containsKey(obj)) {
            StmtMapEvalRecord sr = stmtRecordMap.get(obj);
            setInfo(sr, record, isSrc, project);
            return sr;
        }
        stmtList.add(obj);
        StmtMapEvalRecord sr = new StmtMapEvalRecord();
        setInfo(sr, record, isSrc, project);
        stmtRecordMap.put(obj, sr);
        return sr;
    }

    public String[] toCsvRecords(){
        String[] record = {
                Integer.toString(startLine),
                Integer.toString(gtInaccurate),
                Integer.toString(mtdInaccurate),
                Integer.toString(ijmInaccurate),
                Integer.toString(gtInaccurateByMtd),
                Integer.toString(gtInaccurateByIjm),
                Integer.toString(mtdInaccurateByGt),
                Integer.toString(mtdInaccurateByIjm),
                Integer.toString(ijmInaccurateByGt),
                Integer.toString(ijmInaccurateByMtd),
                Integer.toString(gtMtdInconsistent),
                Integer.toString(gtIjmInconsistent),
                Integer.toString(mtdIjmInconsistent),
                focusAlgorithm,
                Integer.toString(fileId)
        };
        String[] objRecord = stmtObj.toCsvRecord();
        String[] ret = new String[objRecord.length + record.length];
        System.arraycopy(objRecord, 0, ret, 0, objRecord.length);
        System.arraycopy(record, 0, ret, objRecord.length, record.length);
        return ret;
    }

    public static StmtMapEvalRecord getStmtEvalRecordFromCsv(String[] record){
        String project = record[0];
        String commitId = record[1];
        String filePath = record[2];
        boolean isSrc = record[3].equals("1");
        int startPos = Integer.parseInt(record[4]);
        StmtObj obj = new StmtObj(project, commitId, filePath, startPos, isSrc);
        StmtMapEvalRecord sr = new StmtMapEvalRecord();
        sr.stmtObj = obj;
        sr.fileRevision = new FileRevision(commitId, filePath);
        sr.startLine = Integer.parseInt(record[5]);
        sr.gtInaccurate = Integer.parseInt(record[6]);
        sr.mtdInaccurate = Integer.parseInt(record[7]);
        sr.ijmInaccurate = Integer.parseInt(record[8]);
        sr.gtInaccurateByMtd = Integer.parseInt(record[9]);
        sr.gtInaccurateByIjm = Integer.parseInt(record[10]);
        sr.mtdInaccurateByGt = Integer.parseInt(record[11]);
        sr.mtdInaccurateByIjm = Integer.parseInt(record[12]);
        sr.ijmInaccurateByGt = Integer.parseInt(record[13]);
        sr.ijmInaccurateByMtd = Integer.parseInt(record[14]);
        sr.gtMtdInconsistent = Integer.parseInt(record[15]);
        sr.gtIjmInconsistent = Integer.parseInt(record[16]);
        sr.mtdIjmInconsistent = Integer.parseInt(record[17]);
        sr.focusAlgorithm = record[18];
        sr.fileId = Integer.parseInt(record[19]);
        return sr;
    }

    public static String[] getHeaders(){
        String[] headers ={
                "startLine", "gtInaccurate", "mtdInaccurate", "ijmInaccurate",
                "gtInaccurateByMtd", "gtInaccurateByIjm",
                "mtdInaccurateByGt", "mtdInaccurateByIjm",
                "ijmInaccurateByGt", "ijmInaccurateByMtd",
                "gtMtdInconsistent", "gtIjmInconsistent", "mtdIjmInconsistent",
                "focusAlgorithm", "fileId"
        };

        String[] objHeaders = StmtObj.getHeaders();
        String[] ret = new String[objHeaders.length + headers.length];
        System.arraycopy(objHeaders, 0, ret, 0, objHeaders.length);
        System.arraycopy(headers, 0, ret, objHeaders.length, headers.length);
        return ret;
    }
}
