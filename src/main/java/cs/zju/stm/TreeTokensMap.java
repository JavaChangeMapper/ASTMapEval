package cs.zju.stm;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.edit.sequence.MySequenceAlgorithm;
import cs.zju.treeutils.CheckJDTNodeType;
import cs.zju.treeutils.GumTreeUtil;
import cs.zju.treeutils.ITreeToStatement;
import cs.zju.treeutils.RangeCalculation;
import cs.zju.utils.Pair;

import java.util.*;

/**
 * Different tree matching algorithms can use ASTs that encode different tokens as AST nodes.
 * We cannot directly compare two matchings of AST nodes that are generated by
 * different AST matching algorithms. We must compare the matching of AST nodes at the same scale.
 * Thus, it is necessary to project matching of AST nodes as the matching of tokens.
 */
public class TreeTokensMap {
    // Each token is represented as its range of the character values.
    private List<TokenRange> tokenRanges;
    private String fileContent;
    private ITree rootNode;

    private Map<TokenRange, ITree> tokenRangeTreeMap;
    private Map<ITree, List<TokenRange>> treeTokenRangeMap;
    // Each literal is treated as one token (e.g., string literal)
    private Map<Integer, ITree> posLiteralMap;
    // For example, comments and javadocs are not considered in the current version
    private Set<Integer> removedPositions;

    // Use as cache of node and token ranges map
    private Map<ITree, List<TokenRange>> nodeRangesCacheMap = new HashMap<>();
    private Map<ITree, Map<TokenRange, Integer>> nodeRangeIndexMap = new HashMap<>();
    // use as a cache of map between ranges and string values
    private Map<TokenRange, String> rangeStringMap = new HashMap<>();
    // the index of token in the file
    private Map<TokenRange, Integer> rangeIdxMap = new HashMap<>();

    // punctuations
    private static final String CHARS_SEPARATE = " \t\r\n(),;.:?{}[]@";
    private static final String CHARS_NEED_HANDLE = "<>";

    private TreePositionIndex treePosIdx;
    private RangeCalculation rc;

    public TreeTokensMap(RangeCalculation rc, ITree rootNode, ITree rawTreeRootNode,
                         Set<Integer> removedPositions){
        this.fileContent = rc.getFileContent();
        this.rc = rc;
        this.rootNode = rootNode;
        this.removedPositions = removedPositions;
        tokenRanges = new ArrayList<>();
        tokenRangeTreeMap = new HashMap<>();
        treeTokenRangeMap = new HashMap<>();
        initAllLiterals(rootNode);
        treePosIdx = new TreePositionIndex(rootNode, rawTreeRootNode);
        initTokensInNode();
        for (int i = 0; i < tokenRanges.size(); i++)
            rangeIdxMap.put(tokenRanges.get(i), i);
        handleGarbage();
    }

    private void handleGarbage(){
        posLiteralMap.clear();
        removedPositions.clear();
    }

    public List<TokenRange> getTokenRanges() {
        return tokenRanges;
    }

    public Map<TokenRange, ITree> getTokenRangeTreeMap() {
        return tokenRangeTreeMap;
    }

    public Map<ITree, List<TokenRange>> getTreeTokenRangeMap(){
        return treeTokenRangeMap;
    }

    /**
     * Get the string value of a token according to its character range
     * @param range
     * @return string value
     */
    public String getTokenByRange(TokenRange range){
        if (rangeStringMap.containsKey(range))
            return rangeStringMap.get(range);
        String token = fileContent.substring(range.first, range.second);
        rangeStringMap.put(range, token);
        return token;
    }

    /**
     * Get the line range of a statement.
     * @param stmt
     * @return a pair of integers indicating the line range of statement
     */
    public Pair<Integer, Integer> getLineRangeOfStmt(ITree stmt){
        return rc.getLineRangeOfNode(stmt);
    }

    /**
     * Get start line of a character position
     */
    public Integer getStartLineOfStmt(int pos) {
        return rc.getLineNumberOfPos(pos);
    }

    /**
     * Get the statement that have a base token with a given character range
     * @param range
     * @return a statement ITree node.
     */
    public ITree getStmtOfTokenRange(TokenRange range){
        ITree t = tokenRangeTreeMap.get(range);
        if (t != null){
            if (CheckJDTNodeType.isStatementNode(t))
                return t;
            return ITreeToStatement.getStatementFromAncestor(t);
        }
        return null;
    }

    /**
     * Get string values of tokens using ranges of the tokens
     * @param ranges
     * @return a list of string values
     */
    public List<String> getTokensByRanges(List<TokenRange> ranges){
        List<String> ret = new ArrayList<>();
        for (TokenRange wr: ranges){
            ret.add(getTokenByRange(wr));
        }
        return ret;
    }

    /**
     * Whether a given token is a base token of a statement
     * @param range
     * @param stmt
     * @return boolean value
     */
    public boolean isBaseElementTokenInStmt(TokenRange range, ITree stmt){
        ITree t = tokenRangeTreeMap.get(range);
        if (t != null){
            return GumTreeUtil.isDirectElementOfNode(t, stmt) || t == stmt;
        }
        return false;
    }

    private void initTokensInNode(){
        initAllTokensFromNode(rootNode, fileContent, tokenRanges, tokenRangeTreeMap);
        for (TokenRange range: tokenRanges){
            ITree t = tokenRangeTreeMap.get(range);
            if (!treeTokenRangeMap.containsKey(t))
                treeTokenRangeMap.put(t, new ArrayList<>());
            treeTokenRangeMap.get(t).add(range);
        }
    }

    private void addTokenRange(List<TokenRange> tokenRanges,
                               Map<TokenRange, ITree> allTokenTreeMap,
                               int start, int end, ITree t){
        TokenRange range;
        if (t == null) {
            range = new TokenRange(start, end);
            tokenRanges.add(range);
            ITree tmp = treePosIdx.findITreeOfToken(range);
            allTokenTreeMap.put(range, tmp);
        } else {
            range = new TokenRange(t.getPos(), t.getEndPos());
            tokenRanges.add(range);
            allTokenTreeMap.put(range, t);
        }
    }

    private void initAllTokensFromNode(ITree node, String fileContent,
                                       List<TokenRange> tokenRanges,
                                       Map<TokenRange, ITree> allTokenTreeMap){
        int scannerIndex = node.getPos();
        int end = node.getEndPos();
        String token = "";
        int start = node.getPos();
        while (scannerIndex < end){
            if (removedPositions.contains(scannerIndex)){
                scannerIndex ++;
                continue;
            }
            ITree tmp = findLiteralByPos(scannerIndex);
            if (tmp != null) {
                if (!token.equals(""))
                    addTokenRange(tokenRanges, allTokenTreeMap, start, scannerIndex, null);

                addTokenRange(tokenRanges, allTokenTreeMap, -1, -1, tmp);
                scannerIndex = tmp.getEndPos();
                start = tmp.getEndPos();
                token = "";
                continue;
            }

            Pair<Integer, Integer> commentRange = checkRangeOfComment(scannerIndex, fileContent);
            if (commentRange != null){
                if (!token.equals(""))
                    addTokenRange(tokenRanges, allTokenTreeMap, start, scannerIndex, null);

                scannerIndex = commentRange.second;
                start = commentRange.second;
                token = "";
                continue;
            }

            char c = fileContent.charAt(scannerIndex);

            if (treePosIdx.isStartPosOfNode(scannerIndex) ||
                    treePosIdx.isEndPosOfNode(scannerIndex)){
                if (!token.equals("")) {
                    addTokenRange(tokenRanges, allTokenTreeMap, start, scannerIndex, null);
                }
                if (isSeparateCharacter(scannerIndex)){
                    token = "";
                    scannerIndex ++;
                    start = scannerIndex;
                } else {
                    start = scannerIndex;
                    token = "" + c;
                    scannerIndex ++;
                }
                continue;
            }

            if (isSeparateCharacter(scannerIndex)){
                if (!token.equals("")){
                    addTokenRange(tokenRanges, allTokenTreeMap, start, scannerIndex, null);
                    token = "";
                }
                scannerIndex ++;
                start = scannerIndex;
            } else {
                token += c;
                scannerIndex ++;
            }
        }
    }

    private boolean isSeparateCharacter(int charIndex){
        char c = fileContent.charAt(charIndex);
        String tmp = "" + c;
        if (CHARS_SEPARATE.contains(tmp))
            return true;
        if (CHARS_NEED_HANDLE.contains(tmp)){
            ITree t = treePosIdx.findRawTreeOfToken(new TokenRange(charIndex, charIndex + 1));
            if (CheckJDTNodeType.isInfixExpression(t))
                return false;
            if (CheckJDTNodeType.isInfixExpressionOperator(t))
                return false;
            return true;
        }
        return false;
    }

    private ITree findLiteralByPos(int pos){
        return posLiteralMap.get(pos);
    }

    private void initAllLiterals(ITree node){
        posLiteralMap = new HashMap<>();
        if (node != null) {
            for (ITree t : node.preOrder()) {
                if (CheckJDTNodeType.isStringLiteral(t) || CheckJDTNodeType.isCharacterLiteral(t)){
                    posLiteralMap.put(t.getPos(), t);
                }
                if (CheckJDTNodeType.isNumberLiteral(t))
                    posLiteralMap.put(t.getPos(), t);
            }
        }
    }

    private Pair<Integer, Integer> checkRangeOfComment(int index, String fileContent){
        if (index == fileContent.length() - 1)
            return null;
        char c = fileContent.charAt(index);
        char c2 = fileContent.charAt(index + 1);
        if (c == '/' && c2 == '/'){
            int i = index + 2;
            for (; i < fileContent.length(); i++){
                char c3 = fileContent.charAt(i);
                if (c3 == '\n')
                    break;
            }
            return new Pair<>(index, i + 1);
        }

        if (c == '/' && c2 == '*'){
            int i = index + 2;
            for (; i < fileContent.length(); i++){
                char c3 = fileContent.charAt(i);
                if (c3 == '*'){
                    if (i < fileContent.length() - 1){
                        char c4 = fileContent.charAt(i + 1);
                        if (c4 == '/')
                            break;
                    }
                }
            }
            if (i < fileContent.length() - 1)
                return new Pair<>(index, i + 2);
            else
                return new Pair<>(index, fileContent.length());
        }
        return null;
    }

    /**
     * Calculate a map between token and index in a statement
     * @param node ast node
     * @return get a map
     */
    public Map<TokenRange, Integer> getTokenRangeIndexMapOfNode(ITree node){
        List<TokenRange> rangeList = getTokenRangesOfNode(node);
        if (nodeRangeIndexMap.containsKey(node))
            return nodeRangeIndexMap.get(node);
        Map<TokenRange, Integer> map = new HashMap<>();
        for (int i = 0; i < rangeList.size(); i++){
            map.put(rangeList.get(i), i);
        }
        nodeRangeIndexMap.put(node, map);
        return map;
    }

    /**
     * Calculate the character position range of base tokens of a given node
     * @param node
     * @return a list of character position range of base tokens of the node.
     */
    public List<TokenRange> getTokenRangesOfNode(ITree node){
        if (node == null)
            return new ArrayList<>();
        if (nodeRangesCacheMap.containsKey(node))
            return nodeRangesCacheMap.get(node);
        int start = node.getPos();
        int end = node.getEndPos();
        List<TokenRange> ret = new ArrayList<>();
        for (TokenRange range: tokenRanges){
            if (range.first < start)
                continue;
            if (range.second > end)
                continue;
            ITree t = tokenRangeTreeMap.get(range);
            if (GumTreeUtil.isDirectElementOfNode(t, node) || t == node){
                ret.add(range);
            }
        }
        nodeRangesCacheMap.put(node, ret);
        return ret;
    }

    /**
     * If two tokens are neighbor tokens
     * @param range1
     * @param range2
     * @return boolean value
     */
    public boolean isNeighborTokens(TokenRange range1, TokenRange range2){
        if (range1 == null || range2 == null)
            return false;
        int index1 = rangeIdxMap.get(range1);
        int index2 = rangeIdxMap.get(range2);
        if (index1 + 1 != index2)
            return false;
        ITree tree1 = tokenRangeTreeMap.get(range1);
        ITree tree2 = tokenRangeTreeMap.get(range2);
        return isSameStmt(tree1, tree2);
    }

    private boolean isSameStmt(ITree tree1, ITree tree2){
        if (tree1 == null || tree2 == null)
            return false;
        if (CheckJDTNodeType.isStatementNode(tree1) && CheckJDTNodeType.isStatementNode(tree2))
            return tree1 == tree2;
        if (CheckJDTNodeType.isStatementNode(tree1) && !ITreeToStatement.ITreeIncludeStmt(tree2)){
            ITree stmt = ITreeToStatement.getStatementFromAncestor(tree2);
            return tree1 == stmt;
        }
        if (CheckJDTNodeType.isStatementNode(tree2) && !ITreeToStatement.ITreeIncludeStmt(tree1)){
            ITree stmt = ITreeToStatement.getStatementFromAncestor(tree1);
            return tree2 == stmt;
        }
        if (!ITreeToStatement.ITreeIncludeStmt(tree1) && !ITreeToStatement.ITreeIncludeStmt(tree2)){
            ITree stmt1 = ITreeToStatement.getStatementFromAncestor(tree1);
            ITree stmt2 = ITreeToStatement.getStatementFromAncestor(tree2);
            return stmt1 == stmt2;
        }
        return false;
    }

    private static TokenRange findMappedRangeFromMultipleRanges(TokenRange range, List<int[]> matchedIdxes,
                                                                List<TokenRange> ranges1,
                                                                List<TokenRange> ranges2, boolean isSrc,
                                                                int srcLabelIdx,
                                                                int dstLabelIdx) {
        Map<Integer, Integer> matchIdxMap = new HashMap<>();
        for (int[] idxes: matchedIdxes){
            if (isSrc) {
                matchIdxMap.put(idxes[0], idxes[1]);
            } else {
                matchIdxMap.put(idxes[1], idxes[0]);
            }
        }

        if (srcLabelIdx != -1 && dstLabelIdx != -1) {
            if (isSrc) {
                matchIdxMap.put(srcLabelIdx, dstLabelIdx);
            } else {
                matchIdxMap.put(dstLabelIdx, srcLabelIdx);
            }
        }

        int idx = ranges1.indexOf(range);
        if (matchIdxMap.containsKey(idx)){
            return ranges2.get(matchIdxMap.get(idx));
        } else {
            int priorIdx = idx - 1;
            while (priorIdx >= 0 && !matchIdxMap.containsKey(priorIdx)){
                priorIdx --;
            }

            int afterIdx = idx + 1;
            while (afterIdx < ranges1.size() && !matchIdxMap.containsKey(afterIdx)){
                afterIdx ++;
            }
            int mappedPriorIdx = -1;
            if (matchIdxMap.containsKey(priorIdx))
                mappedPriorIdx = matchIdxMap.get(priorIdx);
            int mappedAfterIdx = ranges2.size();
            if (matchIdxMap.containsKey(afterIdx))
                mappedAfterIdx = matchIdxMap.get(afterIdx);
            int mappedIdx = idx - priorIdx + mappedPriorIdx;
            if (mappedIdx < mappedAfterIdx)
                return ranges2.get(mappedIdx);
        }
        return null;
    }

    /**
     * Find mapped range of a token range [Important API]
     * @param ms
     * @param tokenRange
     * @param isSrc
     * @param srcTreeTokenMap
     * @param dstTreeTokenMap
     * @return TokenRange
     */
    public static TokenRange findMappedRange(MappingStore ms, TokenRange tokenRange,
                                             boolean isSrc, TreeTokensMap srcTreeTokenMap,
                                             TreeTokensMap dstTreeTokenMap) {
        ITree tokenTree;
        ITree mappedTree = null;
        List<TokenRange> ranges;
        List<TokenRange> mappedRanges = null;
        String srcLabel = "";
        String dstLabel = "";
        if (isSrc){
            tokenTree = srcTreeTokenMap.getTokenRangeTreeMap().get(tokenRange);
            srcLabel = tokenTree.getLabel();
            ranges = srcTreeTokenMap.getTreeTokenRangeMap().get(tokenTree);
            mappedTree = ms.getDstForSrc(tokenTree);
            if (mappedTree != null){
                dstLabel = mappedTree.getLabel();
                mappedRanges = dstTreeTokenMap.getTreeTokenRangeMap().get(mappedTree);
            }
        } else {
            tokenTree = dstTreeTokenMap.getTokenRangeTreeMap().get(tokenRange);
            dstLabel = tokenTree.getLabel();
            ranges = dstTreeTokenMap.getTreeTokenRangeMap().get(tokenTree);
            mappedTree = ms.getSrcForDst(tokenTree);
            if (mappedTree != null){
                srcLabel = mappedTree.getLabel();
                mappedRanges = srcTreeTokenMap.getTreeTokenRangeMap().get(mappedTree);
            }
        }

        if (mappedRanges == null || mappedRanges.size() == 0)
            return null;

        if (ranges.size() == 1 && mappedRanges.size() == 1){
            return mappedRanges.get(0);
        }

        if (ranges.size() > 1 || mappedRanges.size() > 1){
            int srcLabelIdx = -1;
            int dstLabelIdx = -1;
            if (isSrc){
                List<String> tokens = srcTreeTokenMap.getTokensByRanges(ranges);
                List<String> targetTokens = dstTreeTokenMap.getTokensByRanges(mappedRanges);
                srcLabelIdx = tokens.indexOf(srcLabel);
                dstLabelIdx = targetTokens.indexOf(dstLabel);
                List<int[]> matchedIndexes = MySequenceAlgorithm.longestCommonSubsequenceForTokens(tokens, targetTokens);
                return findMappedRangeFromMultipleRanges(tokenRange,
                        matchedIndexes, ranges, mappedRanges, true, srcLabelIdx, dstLabelIdx);
            } else {
                List<String> tokens = dstTreeTokenMap.getTokensByRanges(ranges);
                List<String> targetTokens = srcTreeTokenMap.getTokensByRanges(mappedRanges);
                srcLabelIdx = targetTokens.indexOf(srcLabel);
                dstLabelIdx = tokens.indexOf(dstLabel);
                List<int[]> matchedIndexes = MySequenceAlgorithm.longestCommonSubsequenceForTokens(targetTokens, tokens);
                return findMappedRangeFromMultipleRanges(tokenRange,
                        matchedIndexes, ranges, mappedRanges, false, srcLabelIdx, dstLabelIdx);
            }

        }
        return null;
    }

    private static boolean isNeighborToken(TokenRange tokenRange1, TokenRange tokenRange2,
                                           boolean isSrc, TreeTokensMap srcTreeTokenMap,
                                           TreeTokensMap dstTreeTokenMap){
        if (isSrc)
            return srcTreeTokenMap.isNeighborTokens(tokenRange1, tokenRange2);
        else
            return dstTreeTokenMap.isNeighborTokens(tokenRange1, tokenRange2);
    }

    public static boolean isNeighborTokensMoved(MappingStore ms, TokenRange range1, TokenRange range2,
                                                boolean isSrc, TreeTokensMap srcTreeTokenMap,
                                                TreeTokensMap dstTreeTokenMap){
        if (!isNeighborToken(range1, range2, isSrc, srcTreeTokenMap, dstTreeTokenMap))
            return false;
        TokenRange mappedRange1 = findMappedRange(ms, range1, isSrc, srcTreeTokenMap, dstTreeTokenMap);
        TokenRange mappedRange2 = findMappedRange(ms, range2, isSrc, srcTreeTokenMap, dstTreeTokenMap);
        return isNeighborToken(mappedRange1, mappedRange2, !isSrc, srcTreeTokenMap, dstTreeTokenMap);
    }
}
