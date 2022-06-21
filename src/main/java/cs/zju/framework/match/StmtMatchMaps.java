package cs.zju.framework.match;

import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.match.StmtMatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StmtMatchMaps {
    private String matchAlgorithm;
    private final Map<UniversalTreeNode, StmtMatch> srcStmtMatchMap;
    private final Map<UniversalTreeNode, StmtMatch> dstStmtMatchMap;

    public StmtMatchMaps(List<StmtMatch> stmtMatchList, String matchAlgorithm) {
        srcStmtMatchMap = new HashMap<>();
        dstStmtMatchMap = new HashMap<>();
        for (StmtMatch sm: stmtMatchList){
            ITree srcStmt = sm.getSrcStmt();
            ITree dstStmt = sm.getDstStmt();
            if (srcStmt != null){
                UniversalTreeNode node = UniversalTreeNode.getUniversalTreeNode(srcStmt);
                srcStmtMatchMap.put(node, sm);
            }
            if (dstStmt != null){
                UniversalTreeNode node = UniversalTreeNode.getUniversalTreeNode(dstStmt);
                dstStmtMatchMap.put(node, sm);
            }
        }
        this.matchAlgorithm = matchAlgorithm;
    }

    public StmtMatch getMatchingForStmt(UniversalTreeNode stmt, boolean isSrc){
        if (stmt == null || stmt.isNull())
            return null;
        if (isSrc)
            return srcStmtMatchMap.get(stmt);
        else
            return dstStmtMatchMap.get(stmt);
    }

    public StmtMatch getMatchingForStmt(ITree stmt, boolean isSrc){
        if (stmt == null)
            return null;
        UniversalTreeNode node = UniversalTreeNode.getUniversalTreeNode(stmt);
        if (isSrc)
            return srcStmtMatchMap.get(node);
        else
            return dstStmtMatchMap.get(node);
    }

    public String getMatchAlgorithm() {
        return matchAlgorithm;
    }
}
