package cs.zju.stm;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import cs.zju.stm.edit.tokenedit.SearchAlgorithms;

import java.util.*;

public class TreePositionIndex {
    private List<ITree> postOrderTrees;
    private List<ITree> postOrderRawTrees;
    private List<Integer> nodeStartPositions;
    private List<Integer> rawNodeStartPositions;

    private Map<Integer, List<ITree>> posTreesMap;
    private Map<Integer, List<ITree>> posRawTreesMap;
    private final ITree rawTreeRoot;
    private Set<Integer> rawTreePosSet;
    private Set<Integer> rawTreeEndPosSet;

    public TreePositionIndex(ITree root, ITree rawTreeRoot){
        this.rawTreeRoot = rawTreeRoot;
        postOrderTrees = TreeUtils.postOrder(root);
        postOrderRawTrees = TreeUtils.postOrder(rawTreeRoot);
        initTrees();
    }

    private void initTrees(){
        Set<Integer> posSet = new HashSet<>();
        Set<Integer> rawPosSet = new HashSet<>();
        rawTreePosSet = new HashSet<>();
        rawTreeEndPosSet = new HashSet<>();
        posTreesMap = new HashMap<>();
        posRawTreesMap = new HashMap<>();

        for (ITree t: postOrderTrees){
            posSet.add(t.getPos());
            if (!posTreesMap.containsKey(t.getPos()))
                posTreesMap.put(t.getPos(), new ArrayList<>());
            posTreesMap.get(t.getPos()).add(t);
        }

        for (ITree t: postOrderRawTrees){
            rawPosSet.add(t.getPos());
            if (!posRawTreesMap.containsKey(t.getPos()))
                posRawTreesMap.put(t.getPos(), new ArrayList<>());
            posRawTreesMap.get(t.getPos()).add(t);
        }

        nodeStartPositions = new ArrayList<>(posSet);
        Collections.sort(nodeStartPositions);

        rawNodeStartPositions = new ArrayList<>(rawPosSet);
        Collections.sort(rawNodeStartPositions);

        for (ITree t: rawTreeRoot.preOrder()){
            rawTreePosSet.add(t.getPos());
            rawTreeEndPosSet.add(t.getEndPos());
        }
    }


    public boolean isStartPosOfNode(int pos) {
        return rawTreePosSet.contains(pos);
    }

    public boolean isEndPosOfNode(int endPos){
        return rawTreeEndPosSet.contains(endPos);
    }

    public ITree findRawTreeOfToken(TokenRange range){
        return findITreeOfToken(range, posRawTreesMap, rawNodeStartPositions);
    }


    public ITree findITreeOfToken(TokenRange range){
        return findITreeOfToken(range, posTreesMap, nodeStartPositions);
    }

    private static ITree findITreeOfToken(TokenRange range, Map<Integer, List<ITree>> posTreesMap, List<Integer> nodeStartPositions){
        int pos = range.first;
        if (posTreesMap.containsKey(pos)){
            ITree t = posTreesMap.get(pos).get(0);
            if (t.getEndPos() < range.second)
                throw new RuntimeException("Find Tree of Word Error!");
            return t;
        }
        int index = SearchAlgorithms.findIndexSmallerOrEqualPos(nodeStartPositions, pos);
        while (index >= 0){
            int curPos = nodeStartPositions.get(index);
            List<ITree> treeList = posTreesMap.get(curPos);
            for (ITree t: treeList){
                int endPos = t.getEndPos();
                if (endPos >= range.second)
                    return t;
            }
            index --;
        }
        return null;
    }
}
