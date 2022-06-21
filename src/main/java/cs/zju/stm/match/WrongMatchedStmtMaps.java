package cs.zju.stm.match;

import cs.zju.framework.match.UniversalTreeNode;

import java.util.*;

public class WrongMatchedStmtMaps {
    private static Map<String, Set<UniversalTreeNode>> algorithmWrongMatchedSrcStmts;
    private static Map<String, Set<UniversalTreeNode>> algorithmWrongMatchedDstStmts;

    public void initWrongMatchedStmts(List<String> algorithms){
        algorithmWrongMatchedSrcStmts = new HashMap<>();
        algorithmWrongMatchedDstStmts = new HashMap<>();
        for (String alg: algorithms){
            algorithmWrongMatchedSrcStmts.put(alg, new HashSet<>());
            algorithmWrongMatchedDstStmts.put(alg, new HashSet<>());
        }
    }

    public boolean isWrongMatchedStmt(UniversalTreeNode stmt, boolean isSrc, String algorithm){
        if (isSrc)
            return algorithmWrongMatchedSrcStmts.get(algorithm).contains(stmt);
        else
            return algorithmWrongMatchedDstStmts.get(algorithm).contains(stmt);
    }
}
