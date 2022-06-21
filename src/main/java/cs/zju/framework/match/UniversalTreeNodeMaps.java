package cs.zju.framework.match;

import com.github.gumtreediff.tree.ITree;
import cs.zju.treeutils.CheckJDTNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversalTreeNodeMaps {
    private final Map<UniversalTreeNode, UniversalTreeNode> parentRelationMap;
    private final Map<UniversalTreeNode, List<UniversalTreeNode>> childrenRelationMap;
    private final Map<UniversalTreeNode, ITree> universalToITreeMap;

    public UniversalTreeNodeMaps(ITree root){
        parentRelationMap = new HashMap<>();
        universalToITreeMap = new HashMap<>();
        childrenRelationMap = new HashMap<>();
        for (ITree t: root.preOrder()){
            ITree parent = t.getParent();
            UniversalTreeNode node1 = UniversalTreeNode.getUniversalTreeNode(t);
            universalToITreeMap.put(node1, t);
            if (parent != null){
                UniversalTreeNode node2 = UniversalTreeNode.getUniversalTreeNode(parent);
                parentRelationMap.put(node1, node2);
            }
        }
        for (ITree t: root.postOrder()){
            List<ITree> children = t.getChildren();
            UniversalTreeNode node1 = UniversalTreeNode.getUniversalTreeNode(t);
            List<UniversalTreeNode> childrenNodes = new ArrayList<>();
            for (ITree tmpChild: children){
                UniversalTreeNode node2 = UniversalTreeNode.getUniversalTreeNode(tmpChild);
                childrenNodes.add(node2);
            }
            childrenRelationMap.put(node1, childrenNodes);
        }
    }

    public List<UniversalTreeNode> getAncestorNodes(UniversalTreeNode t){
        if (t == null || t.isNull())
            return new ArrayList<>();
        UniversalTreeNode tmp = t;
        List<UniversalTreeNode> ret = new ArrayList<>();
        while (parentRelationMap.containsKey(tmp)){
            tmp = parentRelationMap.get(tmp);
            ret.add(tmp);
        }
        return ret;
    }

    public List<UniversalTreeNode> getChildrenNodes(ITree t){
        if (t == null)
            return null;
        UniversalTreeNode node = UniversalTreeNode.getUniversalTreeNode(t);
        return childrenRelationMap.get(node);
    }

    public UniversalTreeNode getParentNode(ITree t){
        if (t == null)
            return null;
        UniversalTreeNode node = UniversalTreeNode.getUniversalTreeNode(t);
        return parentRelationMap.get(node);
    }

    public List<UniversalTreeNode> getChildrenNodes(UniversalTreeNode node){
        if (node == null || node.isNull())
            return null;
        return childrenRelationMap.get(node);
    }

    public List<UniversalTreeNode> getChildrenStmts(UniversalTreeNode t){
        List<UniversalTreeNode> childrenNodes = getChildrenNodes(t);
        List<UniversalTreeNode> ret = new ArrayList<>();
        for (UniversalTreeNode node: childrenNodes){
            if (CheckJDTNodeType.isStatement(node.getNodeType()))
                ret.add(node);
        }
        return ret;
    }

    public UniversalTreeNode getParentNode(UniversalTreeNode node){
        if (node == null || node.isNull())
            return null;
        return parentRelationMap.get(node);
    }

    public ITree getITreeObj(UniversalTreeNode node){
        return universalToITreeMap.get(node);
    }
}