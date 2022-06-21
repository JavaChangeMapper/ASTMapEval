package cs.zju.treeutils;

import com.github.gumtreediff.tree.ITree;

import java.util.*;

public class ITreeToStatement {
    // retrieve the related statements of ITree
    public static List<ITree> getAllStatementsOfITree(ITree node){
        if (node == null)
            return new ArrayList<>();
        List<ITree> ret = getAllStatementsFromDescendants(node);
        if (CheckJDTNodeType.isStatementNode(node))
            ret.add(node);
        if (ret.size() == 0 || CheckJDTNodeType.isAnonymousClassDec(node)){
            ITree st = getStatementFromAncestor(node);
            if (st != null)
                ret.add(st);
        }
        return ret;
    }

    public static boolean ITreeIncludeStmt(ITree node){
        for (ITree t: node.preOrder()){
            if (CheckJDTNodeType.isStatementNode(t))
                return true;
        }
        return false;
    }

    public static ITree getStatementFromAncestor(ITree t){
        ITree parentNode = t.getParent();
        while (parentNode != null && !CheckJDTNodeType.isStatementNode(parentNode)){
            parentNode = parentNode.getParent();
        }
        return parentNode;
    }

    public static List<ITree> getAllStatementsFromDescendants(ITree t){
        List<ITree> ret = new ArrayList<>();
        for (ITree node: t.preOrder()){
            if (node == t)
                continue;
            if (CheckJDTNodeType.isStatementNode(node))
                ret.add(node);
        }
        return ret;
    }
}
