package cs.zju.framework;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import cs.zju.framework.edit.EvalComparisonResultWithEdit;
import cs.zju.framework.match.EvalASTMatchResult;
import cs.zju.framework.match.UniversalTreeNodeMaps;
import cs.zju.gitops.GitUtils;
import cs.zju.stm.MatchingCreator;
import cs.zju.stm.TreePruner;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.match.TokenRangeTypeMap;
import cs.zju.treeutils.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class MatchASTAndTokens {
    protected String project;
    protected String commitId;
    protected String mapMethod;

    protected String srcFilePath;
    protected String dstFilePath;
    protected ITree srcRoot = null;
    protected ITree dstRoot = null;

    private ITree rawSrcRoot;
    private ITree rawDstRoot;

    protected String srcFileContent;
    protected String dstFileContent;
    protected RangeCalculation srcRc;
    protected RangeCalculation dstRc;

    protected List<Action> originAmbiguousActionList;
    protected List<Action> originUnambiguousActionList;

    private int srcToDstAmbiguousCount = 0;
    private int dstToSrcAmbiguousCount = 0;
    private int srcTreeCount = 0;
    private int dstTreeCount = 0;

    private TreeTokensMap srcTreeTokenMap;
    private TreeTokensMap dstTreeTokenMap;

    private MappingStore originMs;
    private MappingStore supplementaryMatching;

    private List<ITree> allSrcStmts = new ArrayList<>();
    private List<ITree> allDstStmts = new ArrayList<>();

    private Set<Integer> srcRemovedPositions = new HashSet<>();
    private Set<Integer> dstRemovedPositions = new HashSet<>();

    private Set<ITree> prunedSrcStmts = new HashSet<>();
    private Set<ITree> prunedDstStmts = new HashSet<>();

    private double originMapTime;
    private double originTreeActionGenerateTime;
    private MatchingCreator matchingCreator = null;

    private UniversalTreeNodeMaps srcUniversalNodeMaps;
    private UniversalTreeNodeMaps dstUniversalNodeMaps;

    private TokenRangeTypeMap srcTokenTypeMap;
    private TokenRangeTypeMap dstTokenTypeMap;

    public MatchASTAndTokens(){
    }

    public MatchASTAndTokens(String project, String commitId, String baseCommitId,
                             String srcFilePath, String dstFilePath, String mapMethod) {
        this.project = project;
        this.commitId = commitId;
        this.mapMethod = mapMethod;

        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;

        try {
            ByteArrayOutputStream srcFileStream = GitUtils
                    .getFileContentOfCommitFile(project, baseCommitId, srcFilePath);
            srcFileContent = srcFileStream.toString("UTF-8");
            if (srcFileContent.equals("")) {
                this.srcFilePath = null;
                return;
            }
            ByteArrayOutputStream dstFileStream = GitUtils
                    .getFileContentOfCommitFile(project, commitId, dstFilePath);

            dstFileContent = dstFileStream.toString("UTF-8");

            if (dstFileContent.equals("")) {
                this.dstFilePath = null;
                return;
            }
            srcRoot = GumTreeUtil.getITreeRoot(srcFileStream, mapMethod);
            srcRc = new RangeCalculation(srcFileContent);
            calAllStmts(srcRoot, true);
            dstRoot = GumTreeUtil.getITreeRoot(dstFileStream, mapMethod);
            dstRc = new RangeCalculation(dstFileContent);
            calAllStmts(dstRoot, false);
        } catch (Exception e){
            this.srcFilePath = null;
            this.dstFilePath = null;
            throw new RuntimeException(e.getMessage());
        }
        supplementaryMatching = null;
        init();
    }

    private void calculateTreeTokenMap(){
        this.srcTreeTokenMap = new TreeTokensMap(srcRc, srcRoot, rawSrcRoot, srcRemovedPositions);
        this.dstTreeTokenMap = new TreeTokensMap(dstRc, dstRoot, rawDstRoot, dstRemovedPositions);
    }

    protected void init(){
        // Create parent relation maps
        srcUniversalNodeMaps = new UniversalTreeNodeMaps(srcRoot);
        dstUniversalNodeMaps = new UniversalTreeNodeMaps(dstRoot);

        try {
            if (this.srcFilePath != null && this.dstFilePath != null) {
                System.out.println(srcFilePath);
                rawSrcRoot = GumTreeUtil.getITreeRoot(srcFileContent, "gumtree");
                rawDstRoot = GumTreeUtil.getITreeRoot(dstFileContent, "gumtree");
                srcTreeCount = TreeUtils.preOrder(srcRoot).size();
                dstTreeCount = TreeUtils.preOrder(dstRoot).size();

                if (mapMethod.equals("gt")) {
                    pruneImportsAndJavadoc();
                    calculateTreeTokenMap();
                    createMatching("gumtree");
                    originMs = matchingCreator.getOriginalMs();
                    calAmbiguousMappingCount(originMs);
                    originMapTime = matchingCreator.getOriginRunTime();
                    originAmbiguousActionList = GumTreeUtil.getEditActions(originMs);
                    originMs = handleAmbiguousMapping(originMs);
                    long start = System.currentTimeMillis();
                    originUnambiguousActionList = GumTreeUtil.getEditActions(originMs);
                    long end = System.currentTimeMillis();
                    originTreeActionGenerateTime = end - start;
                } else if (mapMethod.equals("mtdiff")) {
                    pruneImportsAndJavadoc();
                    calculateTreeTokenMap();
                    createMatching("mtdiff");
                    originMs = matchingCreator.getOriginalMs();
                    calAmbiguousMappingCount(originMs);
                    originMapTime = matchingCreator.getOriginRunTime();
                    originAmbiguousActionList = GumTreeUtil.getEditActions(originMs);
                    originMs = handleAmbiguousMapping(originMs);
                    long start = System.currentTimeMillis();
                    originUnambiguousActionList = GumTreeUtil.getEditActions(originMs);
                    long end = System.currentTimeMillis();
                    originTreeActionGenerateTime = end - start;
                } else if (mapMethod.equals("ijm")){
                    pruneImportsAndJavadoc();
                    calculateTreeTokenMap();
                    createMatching("ijm");
                    originMs = matchingCreator.getOriginalMs();
                    calAmbiguousMappingCount(originMs);
                    originMapTime = matchingCreator.getOriginRunTime();
                    originAmbiguousActionList = GumTreeUtil.getEditActions(originMs);
                    originMs = handleAmbiguousMapping(originMs);
                    long start = System.currentTimeMillis();
                    originUnambiguousActionList = GumTreeUtil.getEditActions(originMs);
                    long end = System.currentTimeMillis();
                    originTreeActionGenerateTime = end - start;
                } else if (mapMethod.equals("change-distiller")){
                    pruneImportsAndJavadoc();
                    calculateTreeTokenMap();
                    createMatching("change-distiller");
                    originMs = matchingCreator.getOriginalMs();
                    calAmbiguousMappingCount(originMs);
                    originMapTime = matchingCreator.getOriginRunTime();
                    originAmbiguousActionList = GumTreeUtil.getEditActions(originMs);
                    originMs = handleAmbiguousMapping(originMs);
                    long start = System.currentTimeMillis();
                    originUnambiguousActionList = GumTreeUtil.getEditActions(originMs);
                    long end = System.currentTimeMillis();
                    originTreeActionGenerateTime = end - start;
                }
                srcRoot.setParent(null);
                dstRoot.setParent(null);
            }
        } catch (Exception e){
            e.printStackTrace();
            this.srcFilePath = null;
            this.dstFilePath = null;
            throw new GumTreeException(e.getMessage());
        }
    }

    public TokenRangeTypeMap getSrcTokenTypeMap() {
        if (srcTokenTypeMap == null){
            TreeTokensMap ttMap = new TreeTokensMap(srcRc, rawSrcRoot, rawSrcRoot, new HashSet<>());
            srcTokenTypeMap = new TokenRangeTypeMap(ttMap);
        }
        return srcTokenTypeMap;
    }

    public TokenRangeTypeMap getDstTokenTypeMap(){
        if (dstTokenTypeMap == null){
            TreeTokensMap ttMap = new TreeTokensMap(dstRc, rawDstRoot, rawDstRoot, new HashSet<>());
            dstTokenTypeMap = new TokenRangeTypeMap(ttMap);
        }
        return dstTokenTypeMap;
    }

    public void setSrcTokenTypeMap(TokenRangeTypeMap srcTokenTypeMap) {
        this.srcTokenTypeMap = srcTokenTypeMap;
    }

    public void setDstTokenTypeMap(TokenRangeTypeMap dstTokenTypeMap) {
        this.dstTokenTypeMap = dstTokenTypeMap;
    }

    private void pruneImportsAndJavadoc(){
        TreePruner pruner = new TreePruner(srcRoot, dstRoot, srcFileContent, dstFileContent);
        pruner.pruneImportAndJavaDoc();
        srcRemovedPositions.addAll(pruner.getRemovedSrcContentPositions());
        dstRemovedPositions.addAll(pruner.getRemovedDstContentPositions());
    }

    private void pruneTree(){
        TreePruner pruner = new TreePruner(srcRoot, dstRoot, srcFileContent, dstFileContent);
        pruner.prune(false);
        srcRemovedPositions.addAll(pruner.getRemovedSrcContentPositions());
        dstRemovedPositions.addAll(pruner.getRemovedDstContentPositions());
        prunedSrcStmts = pruner.getRemovedSrcStmts();
        prunedDstStmts = pruner.getRemovedDstStmts();
        supplementaryMatching = pruner.getMatching();
    }

    private void createMatching(String matcherId) {
        matchingCreator = new MatchingCreator(srcRoot, dstRoot, matcherId, supplementaryMatching);
    }

    // This is a bug of GumTree, it can generate
    // a mapping containing map from a src node to multiple dst nodes
    private MappingStore handleAmbiguousMapping(MappingStore ms){
        MappingStore tmp = new MappingStore(srcRoot, dstRoot);
        for (ITree t: srcRoot.preOrder()){
            if (ms.isSrcMapped(t)){
                ITree dstT = ms.getDstForSrc(t);
                if (!tmp.isDstMapped(dstT)){
                    if (ms.isDstMapped(dstT) && ms.getSrcForDst(dstT) == t)
                        tmp.addMapping(t, dstT);
                }
            }
        }
        return tmp;
    }

    protected void calAllStmts(ITree root, boolean isSrc){
        for (ITree t: root.breadthFirst()){
            if (CheckJDTNodeType.isStatementNode(t)){
                if (isSrc)
                    allSrcStmts.add(t);
                else
                    allDstStmts.add(t);
            }
        }
    }

    private void calAmbiguousMappingCount(MappingStore ms){
        for (ITree t: srcRoot.preOrder()){
            if (ms.isSrcMapped(t)) {
                ITree dstT = ms.getDstForSrc(t);
                if (ms.getSrcForDst(dstT) != t)
                    srcToDstAmbiguousCount ++;
            }
        }

        for (ITree t: dstRoot.preOrder()){
            if (ms.isDstMapped(t)){
                ITree srcT = ms.getSrcForDst(t);
                if (ms.getDstForSrc(srcT) != t){
                    dstToSrcAmbiguousCount ++;
                }
            }
        }
    }

    public EvalASTMatchResult getASTMatchResult(){
        if (srcFilePath == null || dstFilePath == null)
            return null;
        if (allSrcStmts.size() == 0 || allDstStmts.size() == 0)
            return null;
        return new EvalASTMatchResult(getOriginComparisonInfo(), originMs);
    }

    public EvalComparisonResultWithEdit getOriginComparisonResult(){
        if (srcFilePath == null || dstFilePath == null)
            return null;
        if (allSrcStmts.size() == 0 || allDstStmts.size() == 0)
            return null;
        return new EvalComparisonResultWithEdit(getOriginComparisonInfo(), originMs);
    }

    public ComparisonInfo getOriginComparisonInfo(){
        ComparisonInfo ci = new ComparisonInfo();
        ci.setProject(project);
        ci.setCommitId(commitId);
        ci.setMapMethod(mapMethod);
        ci.setSrcToDstAmbiguousCount(srcToDstAmbiguousCount);
        ci.setDstToSrcAmbiguousCount(dstToSrcAmbiguousCount);
        ci.setMapTime(originMapTime);
        ci.setAmbiguousActionList(originAmbiguousActionList);
        ci.setUnambiguousActionList(originUnambiguousActionList);
        ci.setSrcTreeCount(srcTreeCount);
        ci.setDstTreeCount(dstTreeCount);
        ci.setMatchingCreator(null);
        ci.setSrcRoot(srcRoot);
        ci.setDstRoot(dstRoot);
        ci.setSrcRc(srcRc);
        ci.setDstRc(dstRc);
        ci.setSrcFilePath(srcFilePath);
        ci.setDstFilePath(dstFilePath);
        ci.setSrcTreeTokenMap(srcTreeTokenMap);
        ci.setDstTreeTokenMap(dstTreeTokenMap);
        ci.setSrcTokenTypeMap(getSrcTokenTypeMap());
        ci.setDstTokenTypeMap(getDstTokenTypeMap());
        ci.setAllSrcStmts(allSrcStmts);
        ci.setAllDstStmts(allDstStmts);
        ci.setRemovedSrcStmts(getRemovedSrcStmts());
        ci.setRemovedDstStmts(getRemovedDstStmts());
        ci.setTreeActionGenerateTime(originTreeActionGenerateTime);
        ci.setSrcUniversalNodeMaps(srcUniversalNodeMaps);
        ci.setDstUniversalNodeMaps(dstUniversalNodeMaps);
        return ci;
    }

    public Set<ITree> getRemovedSrcStmts(){
        Set<ITree> ret = new HashSet<>();
        ret.addAll(prunedSrcStmts);
        return ret;
    }

    public Set<ITree> getRemovedDstStmts(){
        Set<ITree> ret = new HashSet<>();
        ret.addAll(prunedDstStmts);
        return ret;
    }
}
