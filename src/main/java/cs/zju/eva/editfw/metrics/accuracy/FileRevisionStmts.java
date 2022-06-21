package cs.zju.eva.editfw.metrics.accuracy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRevisionStmts {
    private List<StmtRecord> stmts;
    private Map<Integer, List<StmtRecord>> srcStartLineStmtMap = null;
    private Map<Integer, List<StmtRecord>> dstStartLineStmtMap = null;

    public FileRevisionStmts(){
        stmts = new ArrayList<>();
    }

    public void addStmt(StmtRecord stmt){
        stmts.add(stmt);
    }

    public List<Double> getSameTokenRatioList() {
        List<Double> ret = new ArrayList<>();
        for (StmtRecord stmt : stmts) {
            double ratio = stmt.matchRatio();
            if (ratio < 1)
                ret.add(ratio);
        }
        return ret;
    }

    private void initMap(){
        if (srcStartLineStmtMap != null && dstStartLineStmtMap != null)
            return;
        srcStartLineStmtMap = new HashMap<>();
        dstStartLineStmtMap = new HashMap<>();
        for (StmtRecord stmt: stmts){
            int srcStartPos = stmt.getSrcStartPos();
            int dstStartPos = stmt.getDstStartPos();
            if (srcStartPos != -1){
                if (!srcStartLineStmtMap.containsKey(srcStartPos))
                    srcStartLineStmtMap.put(srcStartPos, new ArrayList<>());
                srcStartLineStmtMap.get(srcStartPos).add(stmt);
            }
            if (dstStartPos != -1){
                if (!dstStartLineStmtMap.containsKey(dstStartPos))
                    dstStartLineStmtMap.put(dstStartPos, new ArrayList<>());
                dstStartLineStmtMap.get(dstStartPos).add(stmt);
            }
        }
    }

    private StmtRecord getStmt(int line, boolean src, String stmtType){
        initMap();
        List<StmtRecord> tmpList;
        if (src)
            tmpList = srcStartLineStmtMap.get(line);
        else
            tmpList = dstStartLineStmtMap.get(line);
        if (tmpList == null)
            return null;
        for (StmtRecord stmt: tmpList){
            if (stmt.getStmtType().equals(stmtType))
                return stmt;
        }
        return null;
    }

    public void doCompare(FileRevisionStmts comparisonStmts){
        if (comparisonStmts == null)
            return;
        for (StmtRecord stmt: stmts){
            int srcStartPos = stmt.getSrcStartPos();
            int dstStartPos = stmt.getDstStartPos();
            String stmtType = stmt.getStmtType();
            if (stmtType.equals("Block"))
                continue;
            if (stmt.getChangeType().equals(""))
                continue;
            if (srcStartPos != -1) {
                StmtRecord stmt1 = comparisonStmts.getStmt(srcStartPos, true, stmtType);
                if (stmt1 == null || stmt1.getChangeType().equals("")) {
                    stmt.setMoreEditsThanOther();
                } else if ((stmt1.getDstStartPos() == dstStartPos) && stmt1.getEdits() < stmt.getEdits()) {
                    stmt.setMoreEditsThanOther();
                }
            } else {
                StmtRecord stmt2 = comparisonStmts.getStmt(dstStartPos, false, stmtType);
                if (stmt2 == null || stmt2.getChangeType().equals(""))
                    stmt.setMoreEditsThanOther();
                else if (stmt2.getSrcStartLine() == -1 && stmt2.getEdits() < stmt.getEdits())
                    stmt.setMoreEditsThanOther();
            }
        }
    }

    public List<StmtRecord> getAllContextsTestedByCEC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (StmtRecord stmt: stmts){
            if (stmt.isTestedByCEC()){
                ret.add(stmt);
            }
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsByCEC(double lmad){
        List<StmtRecord> ret = new ArrayList<>();
        for (StmtRecord stmt: stmts){
            if (stmt.isSuspiciousByCEC(lmad))
                ret.add(stmt);
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsBySDC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (StmtRecord stmt: stmts){
            if (stmt.isSuspiciousBySDC())
                ret.add(stmt);
        }
        return ret;
    }

    public List<StmtRecord> getSuspiciousStmtsByMMTC(){
        List<StmtRecord> ret = new ArrayList<>();
        for (StmtRecord stmt: stmts){
            if (stmt.isSuspiciousByMMTC())
                ret.add(stmt);
        }
        return ret;
    }

    public List<StmtRecord> getAllSuspiciousStmts(double lmad){
        List<StmtRecord> ret = new ArrayList<>();
        for (StmtRecord stmt: stmts){
            if (stmt.isSuspicious(lmad))
                ret.add(stmt);
        }
        return ret;
    }
}
