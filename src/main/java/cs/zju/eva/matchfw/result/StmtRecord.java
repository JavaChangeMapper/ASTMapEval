package cs.zju.eva.matchfw.result;

import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.Pair;

import java.util.*;

public class StmtRecord {
    private boolean isSrc;
    private String stmtType;
    private int stmtCharStartPos = -1;
    private FileRevision fr;
    private String algorithm;
    private String compareAlgorithm;
    private double ratioOfIdenticalToken;
    private String project;
    private int errorType;
    private int stmtErrorType;
    private int tokenErrorType;
    private boolean stmtBadMatch = false;
    private boolean tokenBadMatch = false;
    private boolean methodBadMatch = false;

    public StmtRecord(ComparisonRecord record, boolean isSrc, String project){
        this.isSrc = isSrc;
        this.stmtType = record.getStmtType();
        this.project = project;
        this.algorithm = record.getAlgorithm();
        this.compareAlgorithm = record.getComparedAlgorithm();
        this.ratioOfIdenticalToken = record.getRatioOfIdenticalToken();
        this.errorType = record.getErrorType();
        this.stmtErrorType = record.getStmtErrorType();
        this.stmtBadMatch = record.getStmtBadMatch() == 1;
        if (isSrc && record.getSrcSameMatch() == 0) {
            this.stmtCharStartPos = record.getSrcCharPos();
            this.tokenBadMatch = record.getDstTokenBadMatch() == 1;
            this.tokenErrorType = record.getSrcTokenErrorType();
        }
        if (!isSrc && record.getDstSameMatch() == 0) {
            this.stmtCharStartPos = record.getDstCharPos();
            this.tokenBadMatch = record.getSrcTokenBadMatch() == 1;
            this.tokenErrorType = record.getDstTokenErrorType();
        }

        fr = new FileRevision(record.getCommitId(), record.getFilePath());
        stmtBadMatch = record.getStmtBadMatch() == 1;

        if (isSrc){
            this.methodBadMatch = record.getSrcStmtOrTokenError() == 1;
        } else {
            this.methodBadMatch = record.getDstStmtOrTokenError() == 1;
        }
    }

    public int getStmtErrorType() {
        if (methodBadMatch)
            return stmtErrorType;
        return 0;
    }

    public int getTokenErrorType() {
        return tokenErrorType;
    }

    public boolean isBlock(){
        return stmtType.equals("Block");
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public double getRatioOfIdenticalToken() {
        return ratioOfIdenticalToken;
    }

    public String getStmtType() {
        return stmtType;
    }

    public boolean isStmtBadMatch() {
        return stmtBadMatch;
    }

    public boolean isTokenBadMatch() {
        return tokenBadMatch;
    }

    public boolean findInaccuracy(){
        return (stmtBadMatch || tokenBadMatch) && methodBadMatch;
    }

    public boolean findAccurateMappingInaccurate(){
        return !stmtBadMatch && !tokenBadMatch && methodBadMatch;
    }

    public boolean isMethodBadMatch(){
        return methodBadMatch;
    }

    public int getErrorType() {
        return errorType;
    }

    public String getProject() {
        return project;
    }

    public String getCompareAlgorithm() {
        return compareAlgorithm;
    }

    public FileRevision getFr() {
        return fr;
    }

    public int getStmtCharStartPos() {
        return stmtCharStartPos;
    }

    @Override
    public String toString() {
        return "StmtRecord{" +
                "isSrc=" + isSrc +
                ", stmtType='" + stmtType + '\'' +
                ", stmtCharStartPos=" + stmtCharStartPos +
                ", fr=" + fr +
                ", algorithm='" + algorithm + '\'' +
                ", compareAlgorithm='" + compareAlgorithm + '\'' +
                ", ratioOfIdenticalToken=" + ratioOfIdenticalToken +
                ", project='" + project + '\'' +
                ", stmtBadMatch=" + stmtBadMatch +
                ", tokenBadMatch=" + tokenBadMatch +
                '}';
    }

    private static void addStmtRecordToMap(Map<Set<String>, List<StmtRecord>> map, StmtRecord record){
        Set<String> algorithmSet = new HashSet<>();
        String algorithm1 = record.algorithm;
        String algorithm2 = record.compareAlgorithm;
        algorithmSet.add(algorithm1);
        algorithmSet.add(algorithm2);
        if (!map.containsKey(algorithmSet))
            map.put(algorithmSet, new ArrayList<>());
        map.get(algorithmSet).add(record);
    }

    public static Map<Set<String>, List<StmtRecord>> getStmtRecords(List<ComparisonRecord> records,
                                                                    String project){
        Map<Set<String>, List<StmtRecord>> ret = new HashMap<>();
        for (ComparisonRecord cr: records){
            StmtRecord rcsrc = new StmtRecord(cr, true, project);
            StmtRecord rcdst = new StmtRecord(cr, false, project);
            if (rcsrc.stmtCharStartPos != -1){
                addStmtRecordToMap(ret, rcsrc);
            }

            if (rcdst.stmtCharStartPos != -1){
                addStmtRecordToMap(ret, rcdst);
            }
        }
        return ret;
    }

    public static Map<Integer, String> getErrorTypeStringMap(){
        Map<Integer, String> errorStringMap = new HashMap<>();
        errorStringMap.put(1, "Statement Exchange");
        errorStringMap.put(2, "Statement Move");
        errorStringMap.put(3, "Token Exchange");
        errorStringMap.put(4, "More Unmatched Token");
        errorStringMap.put(5, "Same Stmt Token non-mapped");
        errorStringMap.put(6, "Identical Token Non-mapped");
        errorStringMap.put(7, "Block badly mapped");
        errorStringMap.put(8, "Affected");
        errorStringMap.put(9, "Enough Token Non-mapped");
        errorStringMap.put(10, "Stmt Badly mapped");
        errorStringMap.put(11, "Change Type and Value");
        errorStringMap.put(12, "Change Type and Value");
        errorStringMap.put(13, "Change Type and Value");
        errorStringMap.put(14, "Move Token to Other stmt");
        errorStringMap.put(15, "Token moved from other stmt");
        errorStringMap.put(16, "Both with moved tokens");
        errorStringMap.put(17, "Should map but not mapped");
        return errorStringMap;
    }
}
