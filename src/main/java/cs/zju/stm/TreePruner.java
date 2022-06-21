package cs.zju.stm;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.treeutils.CheckJDTNodeType;
import cs.zju.treeutils.GumTreeUtil;
import cs.zju.treeutils.ITreeToStatement;

import java.util.*;

/**
 * 1. Strategy 1: we directly remove the methods that are identical
 * 2. Strategy 2: we return a matching of the methods that are identical.
 */

public class TreePruner {
    private ITree src;
    private ITree dest;
    private String preFileContent;
    private String nextFileContent;

    private Map<Integer, ITree> srcITreeHashCodeMap;
    private Map<Integer, ITree> dstITreeHashCodeMap;

    private Set<ITree> removedSrcTrees;
    private Set<ITree> removedDestTrees;

    private MappingStore matching;


    public TreePruner(ITree src, ITree dest, String preFileContent, String nextFileContent){
        this.src = src;
        this.dest = dest;
        this.preFileContent = preFileContent;
        this.nextFileContent = nextFileContent;
        removedSrcTrees = new HashSet<>();
        removedDestTrees = new HashSet<>();
        matching = new MappingStore(src, dest);
    }

    public MappingStore getMatching() {
        return matching;
    }

    private static boolean isPruneType(ITree t){
        return CheckJDTNodeType.isMethodDec(t) || CheckJDTNodeType.isTypeDec(t) ||
                CheckJDTNodeType.isFieldDec(t);
    }

    private void pruneImportDecs(ITree t, boolean isSrc){
        if (t == null)
            return;
        List<ITree> trees = t.getChildren();
        if (trees != null && trees.size() > 0) {
            List<ITree> tempTrees = new ArrayList<>(trees);
            for (ITree tempT : tempTrees) {
                if (CheckJDTNodeType.isImportDec(tempT) || CheckJDTNodeType.isPackageDec(tempT)) {
                    removeTree(tempT);
                    if (isSrc){
                        removedSrcTrees.add(tempT);
                    } else {
                        removedDestTrees.add(tempT);
                    }
                }
            }
        }
    }

    private void pruneJavadoc(ITree tempT, boolean isSrc){
        if (CheckJDTNodeType.isJavaDoc(tempT)){
            ITree tempParent = tempT.getParent();
            if (tempParent != null && tempParent.getChildPosition(tempT) == 0){
                tempParent.getChildren().remove(tempT);
                if (isSrc)
                    removedSrcTrees.add(tempT);
                else
                    removedDestTrees.add(tempT);

                int parentEndPos = tempParent.getEndPos();
                int javadocEnd = tempT.getEndPos();
                int tmpIdx = javadocEnd;
                String fileContent = isSrc? preFileContent : nextFileContent;
                char c = fileContent.charAt(tmpIdx);
                while (c == ' ' || c == '\n' || c == '\r' || c == '\t'){
                    tmpIdx ++;
                    c = fileContent.charAt(tmpIdx);
                }
                if (tmpIdx < parentEndPos){
                    tempParent.setPos(tmpIdx);
                    int length = parentEndPos - tmpIdx;
                    tempParent.setLength(length);
                }
            }
        }
    }

    private void pruneJavadoc(){
        List<ITree> trees = new ArrayList<>();
        if (src != null) {
            for (ITree t : src.preOrder()) {
                if (CheckJDTNodeType.isJavaDoc(t))
                    trees.add(t);
            }
            for (ITree t : trees)
                pruneJavadoc(t, true);
        }

        trees = new ArrayList<>();
        if (dest != null) {
            for (ITree t : dest.preOrder()) {
                if (CheckJDTNodeType.isJavaDoc(t))
                    trees.add(t);
            }
            for (ITree t : trees)
                pruneJavadoc(t, false);
        }

    }

    /**
     * prune all import statements and javadocs
     */
    public void pruneImportAndJavaDoc(){
        pruneImportDecs(src, true);
        pruneImportDecs(dest, false);
        pruneJavadoc();
    }

    /**
     * prune all import and javadocs
     */
    public void prune(boolean removeTrees){
        pruneImportDecs(src, true);
        pruneImportDecs(dest, false);

        pruneJavadoc();

        ITree classSrc = GumTreeUtil.getMainTypeDecOfCompilationUnit(src);
        ITree classDest = GumTreeUtil.getMainTypeDecOfCompilationUnit(dest);

        if (classSrc == null || classDest == null)
            return;

        String preTypeName = getTypeDecName(classSrc, true);
        if (preTypeName == null)
            return;
        String nextTypeName = getTypeDecName(classDest, false);
        if (nextTypeName == null)
            return;

        if (preTypeName.equals(nextTypeName)) {
            srcITreeHashCodeMap = new HashMap<>();
            dstITreeHashCodeMap = new HashMap<>();

            for (ITree t : classSrc.preOrder()) {
                if (t.equals(classSrc))
                    continue;
                if (isPruneType(t))
                    calHashCodeAndPutToMap(t, true);
            }

            for (ITree t : classDest.preOrder()) {
                if (t.equals(classDest))
                    continue;
                if (isPruneType(t))
                    calHashCodeAndPutToMap(t, false);
            }

            compareAndPruneSameTrees(removeTrees);
        }
    }

    private void compareAndPruneSameTrees(boolean removeTrees){
        Set<Integer> hashCodes = new HashSet<>(srcITreeHashCodeMap.keySet());
        Set<Integer> destHashCodes = new HashSet<>(dstITreeHashCodeMap.keySet());

        hashCodes.retainAll(destHashCodes);
        if (hashCodes.size() > 0){
            for (int hc: hashCodes){
                ITree srcTree = srcITreeHashCodeMap.get(hc);
                ITree destTree = dstITreeHashCodeMap.get(hc);
                matchTrees(srcTree, destTree);
                if (removeTrees)
                    removeTrees(srcTree, destTree);
            }
        }
    }

    private void matchTrees(ITree srcTree, ITree dstTree){
        matching.addMappingRecursively(srcTree, dstTree);
    }

    private void calHashCodeAndPutToMap(ITree t, boolean isSrc){
        String uniqueStr = generateUniqueTypeOrMethodDeclarationString(t, isSrc);
        if (uniqueStr != null){
            if (isSrc){
                srcITreeHashCodeMap.put(uniqueStr.hashCode(), t);
            } else {
                dstITreeHashCodeMap.put(uniqueStr.hashCode(), t);
            }
        }
    }

    private List<String> findParentClassNames(ITree t, boolean isSrc){
        List<ITree> parents = t.getParents();
        List<String> parentClassNames = new ArrayList<>();
        for (ITree parent: parents){
            if (GumTreeUtil.checkTypeDec(parent)) {
                String typeName = getTypeDecName(parent, isSrc);
                parentClassNames.add(typeName);
            } else {
                if (!CheckJDTNodeType.isBlock(parent) && !CheckJDTNodeType.isCompilationUnit(parent))
                    return null;
            }
        }
        return parentClassNames;
    }

    private String generateUniqueTypeOrMethodDeclarationString(ITree t, boolean isSrc){
        List<String> classNames = findParentClassNames(t, isSrc);
        if (classNames == null || classNames.size() == 0)
            return null;
        String prefixClassNameStr = "";
        for (String className: classNames){
            prefixClassNameStr += "[TYPE]" + className + "---";
        }
        String allStr = getNodeContent(t, isSrc);
        return prefixClassNameStr + "[METHOD-DEC]" + allStr;
    }

    private String getTypeDecName(ITree t, boolean isSrc){
        if (GumTreeUtil.checkTypeDec(t)) {
            if (t.hasLabel())
                return t.getLabel();
            for (ITree tempT : t.getChildren()) {
                if (CheckJDTNodeType.isSimpleName(tempT)) {
                    return getNodeContent(tempT, isSrc);
                }
            }
            return null;
        } else {
            throw new RuntimeException("Cannot get the type name of a non-type declaration node!");
        }
    }

    private String getNodeContent(ITree t, boolean isSrc){
        if (isSrc){
            return GumTreeUtil.getNodeContent(preFileContent, t);
        } else {
            return GumTreeUtil.getNodeContent(nextFileContent, t);
        }
    }

    private boolean checkParentsRemoved(ITree t, boolean isSrc){
        List<ITree> parents = t.getParents();
        for (ITree p: parents){
            if (isSrc && removedSrcTrees.contains(p))
                return true;
            if (!isSrc && removedDestTrees.contains(p))
                return true;
        }
        return false;
    }

    private void removeTrees(ITree srcTree, ITree destTree){
        removedSrcTrees.add(srcTree);
        removedDestTrees.add(destTree);
        if (checkParentsRemoved(srcTree, true) || checkParentsRemoved(destTree, false))
            return;
        removeTree(srcTree);
        removeTree(destTree);
    }

    public Set<Integer> getRemovedSrcContentPositions(){
        Set<Integer> ret = new HashSet<>();
        if (removedSrcTrees != null){
            for (ITree t: removedSrcTrees){
                for (int i = t.getPos(); i < t.getEndPos(); i++){
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public Set<Integer> getRemovedDstContentPositions(){
        Set<Integer> ret = new HashSet<>();
        if (removedDestTrees != null){
            for (ITree t: removedDestTrees){
                for (int i = t.getPos(); i < t.getEndPos(); i++){
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public Set<ITree> getRemovedSrcStmts(){
        Set<ITree> removedSrcStmts = new HashSet<>();
        if (removedSrcTrees != null){
            for (ITree t: removedSrcTrees){
                if (CheckJDTNodeType.isStatementNode(t)) {
                    removedSrcStmts.add(t);
                    removedSrcStmts.addAll(ITreeToStatement.getAllStatementsFromDescendants(t));
                }
            }
        }
        return removedSrcStmts;
    }

    public Set<ITree> getRemovedDstStmts(){
        Set<ITree> removedDstStmts = new HashSet<>();
        if (removedDestTrees != null){
            for (ITree t: removedDestTrees){
                if (CheckJDTNodeType.isStatementNode(t)) {
                    removedDstStmts.add(t);
                    removedDstStmts.addAll(ITreeToStatement.getAllStatementsFromDescendants(t));
                }
            }
        }
        return removedDstStmts;
    }

    private static void removeTree(ITree t){
        t.getParent().getChildren().remove(t);
        t.setParent(null);
    }

    public static Set<Integer> getPositions(ITree t){
        Set<Integer> ret = new HashSet<>();
        for (int i = t.getPos(); i < t.getEndPos(); i++){
            ret.add(i);
        }
        return ret;
    }
}
