package cs.zju.framework.match;

import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TreeTokensMap;
import cs.zju.utils.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * A universal tree node represents a program element that can be uniquely specified
 */
public class UniversalTreeNode{
    private String nodeType;
    private int pos;
    private int endPos;

    private static Map<String, Map<Integer, Map<Integer, UniversalTreeNode>>> nodeFactoryMap;
    private final static UniversalTreeNode nullNode = new UniversalTreeNode("NULL", -1, -1);

    private UniversalTreeNode(String type, int pos, int endPos){
        this.nodeType = type;
        this.pos = pos;
        this.endPos = endPos;
    }

    public String getNodeType() {
        return nodeType;
    }

    public int getPos() {
        return pos;
    }

    public int getEndPos() {
        return endPos;
    }

    public boolean isNull(){
        return this == nullNode;
    }

    public static void initNodeFactoryMap(){
        nodeFactoryMap = new HashMap<>();
    }

    public static UniversalTreeNode getNullTreeNode(){
        return nullNode;
    }

    public static String getStmtLocationStr(UniversalTreeNode stmt, TreeTokensMap ttmap){
        if (stmt == null)
            return null;
        if (stmt.isNull())
            return null;
        return stmt.nodeType + "(LINE:" + ttmap.getStartLineOfStmt(stmt.pos) + ")";
    }

    public static UniversalTreeNode getUniversalTreeNode(ITree t) {
        if (t == null)
            return UniversalTreeNode.getNullTreeNode();
        String typeStr = t.getType().toString();
        int pos = t.getPos();
        int endPos = t.getEndPos();
        if (nodeFactoryMap == null)
            nodeFactoryMap = new HashMap<>();

        if (!nodeFactoryMap.containsKey(typeStr))
            nodeFactoryMap.put(typeStr, new HashMap<>());
        if (!nodeFactoryMap.get(typeStr).containsKey(pos))
            nodeFactoryMap.get(typeStr).put(pos, new HashMap<>());
        if (!nodeFactoryMap.get(typeStr).get(pos).containsKey(endPos))
            nodeFactoryMap.get(typeStr).get(pos).put(endPos, new UniversalTreeNode(typeStr, pos, endPos));
        return nodeFactoryMap.get(typeStr).get(pos).get(endPos);
    }
}
