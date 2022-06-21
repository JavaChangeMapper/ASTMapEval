package cs.zju.framework.edit;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.framework.ComparisonInfo;
import cs.zju.gitops.GitLine;
import cs.zju.stm.GitDiffLineMap;
import cs.zju.stm.edit.tokenedit.TokenActionGenerator;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.TokenRange;
import cs.zju.stm.edit.tokenedit.actions.*;
import cs.zju.treeutils.CheckJDTNodeType;
import cs.zju.treeutils.RangeCalculation;

import java.util.*;

public class EvalComparisonResultWithEdit {

    GitDiffLineMap.GitChunkHandler gitChunkHandler;

    private MappingStore ms;
    private ITree srcRoot;
    private ITree dstRoot;
    private String srcFilePath;
    private String dstFilePath;
    private RangeCalculation srcRc;
    private RangeCalculation dstRc;

    private List<ITree> allSrcStmts;
    private List<ITree> allDstStmts;

    private TreeTokensMap srcTreeWordMap;
    private TreeTokensMap dstTreeWordMap;

    private Set<ITree> removedSrcStmts;
    private Set<ITree> removedDstStmts;

    private List<Action> ambiguousActionList;
    private List<Action> unambiguousActionList;
    private int srcToDstAmbiguousCount;
    private int dstToSrcAmbiguousCount;
    private int srcTreeNodeCount;
    private int dstTreeNodeCount;
    private double mapTime;
    private String mapMethod;
    private String project;
    private String commitId;
    private double treeActionGenerateTime;
    private double wordActionGenerateTime;
    private List<TokenActionGenerator> generators;

    private List<StmtEditActionGroup> stmtEditGroupList;
    private Set<TokenRange> lowQualityMappingRanges;
    private List<TokenEditAction> editsInconsistentWithDiff = new ArrayList<>();

    // Use as cache
    private Map<ITree, Integer> dstStmtIdxMap = new HashMap<>();

    // match between statements

    public EvalComparisonResultWithEdit(ComparisonInfo comparison, MappingStore ms){
        this.ms = ms;
        this.project = comparison.getProject();
        this.commitId = comparison.getCommitId();
        this.mapMethod = comparison.getMapMethod();
        this.srcToDstAmbiguousCount = comparison.getSrcToDstAmbiguousCount();
        this.dstToSrcAmbiguousCount = comparison.getDstToSrcAmbiguousCount();
        this.mapTime = comparison.getMapTime();
        this.ambiguousActionList = comparison.getAmbiguousActionList();
        this.unambiguousActionList = comparison.getUnambiguousActionList();
        this.srcTreeNodeCount = comparison.getSrcTreeCount();
        this.dstTreeNodeCount = comparison.getDstTreeCount();

        this.srcRoot = comparison.getSrcRoot();
        this.dstRoot = comparison.getDstRoot();
        this.srcRc = comparison.getSrcRc();
        this.dstRc = comparison.getDstRc();
        this.srcFilePath = comparison.getSrcFilePath();
        this.dstFilePath = comparison.getDstFilePath();
        this.srcTreeWordMap = comparison.getSrcTreeTokenMap();
        this.dstTreeWordMap = comparison.getDstTreeTokenMap();
        this.allSrcStmts = comparison.getAllSrcStmts();
        this.allDstStmts = comparison.getAllDstStmts();
        this.removedSrcStmts = comparison.getRemovedSrcStmts();
        this.removedDstStmts = comparison.getRemovedDstStmts();
        this.treeActionGenerateTime = comparison.getTreeActionGenerateTime();

        GitDiffLineMap diffLineMap = new GitDiffLineMap(project, commitId, srcFilePath,
                srcRc.getLineNumber(), dstRc.getLineNumber());
        this.gitChunkHandler = diffLineMap.getGitChunkHandler();

        initStmtIdxMap();
        lowQualityMappingRanges = new HashSet<>();
        long start = System.currentTimeMillis();
        stmtEditGroupList = calEditActionGroups();
        long end = System.currentTimeMillis();
        wordActionGenerateTime = end - start;
        getAllLowQualityMappedRanges();
    }

    private void initStmtIdxMap(){
        for (int i = 0; i < allDstStmts.size(); i++){
            dstStmtIdxMap.put(allDstStmts.get(i), i);
        }
    }

    private void getAllLowQualityMappedRanges(){
        for (TokenActionGenerator g: generators){
            lowQualityMappingRanges.addAll(g.getLowQualityMappedWordRanges(false));
        }
    }

    public MappingStore getMs() {
        return ms;
    }

    public boolean hasAmbiguousMapping(){
        return hasAmbiguousMapping(ms, srcRoot, dstRoot);
    }

    public double getWordEdits(){
        double edits = 0;
        if (stmtEditGroupList != null) {
            for (StmtEditActionGroup group : stmtEditGroupList) {
                List<TokenEditAction> actions = group.getActions();
                if (actions != null) {
                    edits += actions.size();
                }
            }
        }
        return edits;
    }

    public double getMeaninglessMoveCount(){
        double edits = 0;
        for (StmtEditActionGroup group: stmtEditGroupList){
            List<TokenEditAction> actions = group.getActions();
            if (actions != null){
                for (TokenEditAction action: actions){
                    if (action instanceof TokenMoveFromStmt){
                        List<TokenRange> ranges = action.getSrcRanges();
                        if (lowQualityMappingRanges.contains(ranges.get(0))) {
                            edits += 1;
                            group.setHasLowQualityMove();
                        }
                    }
                    if (action instanceof TokenMoveToStmt){
                        List<TokenRange> ranges = action.getSrcRanges();
                        if (lowQualityMappingRanges.contains(ranges.get(0)))
                            group.setHasLowQualityMove();
                    }
                }
            }
        }
        return edits;
    }

    public double getMoveStmtCount(boolean filterBlock){
        double count = 0;
        if (stmtEditGroupList != null){
            for (StmtEditActionGroup group: stmtEditGroupList){
                ITree srcStmt = group.getSrcStmt();
                ITree dstStmt = group.getDstStmt();
                boolean filter = false;
                if (filterBlock) {
                    if (srcStmt != null)
                        filter = CheckJDTNodeType.isBlock(srcStmt);
                    else
                        filter = CheckJDTNodeType.isBlock(dstStmt);
                }
                if (!filter) {
                    if (group.isMoved())
                        count += 1;
                }
            }
        }
        return count;
    }

    public double getChangedStmtCount(boolean filterBlock){
        if (stmtEditGroupList == null)
            return 0;
        double ret = 0;
        for (StmtEditActionGroup group: stmtEditGroupList){
            ITree srcStmt = group.getSrcStmt();
            ITree dstStmt = group.getDstStmt();
            boolean filter = false;
            if (filterBlock) {
                if (srcStmt != null)
                    filter = CheckJDTNodeType.isBlock(srcStmt);
                else
                    filter = CheckJDTNodeType.isBlock(dstStmt);
            }
            if (!filter) {
                if (group.getStmtChangeType() != null)
                    ret += 1.0;
            }
        }
        return ret;
    }

    public double getEditedStmtCount(boolean filterBlock){
        if (stmtEditGroupList == null)
            return 0;
        double ret = 0;
        for (StmtEditActionGroup group: stmtEditGroupList){
            ITree srcStmt = group.getSrcStmt();
            ITree dstStmt = group.getDstStmt();
            boolean filter = false;
            if (filterBlock) {
                if (srcStmt != null)
                    filter = CheckJDTNodeType.isBlock(srcStmt);
                else
                    filter = CheckJDTNodeType.isBlock(dstStmt);
            }

            if (!filter) {
                List<TokenEditAction> actions = group.getActions();
                if (actions != null && actions.size() > 0)
                    ret += 1.0;
            }
        }
        return ret;
    }

    public double getTreeActionGenerateTime() {
        return treeActionGenerateTime;
    }

    public double getWordActionGenerateTime(){
        return wordActionGenerateTime;
    }

    public String getProject() {
        return project;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getMapMethod() {
        return mapMethod;
    }

    public int getSrcToDstAmbiguosCount() {
        return srcToDstAmbiguousCount;
    }

    public int getDstToSrcAmbiguousCount() {
        return dstToSrcAmbiguousCount;
    }

    public double getMapTime() {
        return mapTime;
    }

    public ITree getSrcRoot() {
        return srcRoot;
    }

    public ITree getDstRoot() {
        return dstRoot;
    }

    public double getAmbiguousActionCount(){
        if (ambiguousActionList == null)
            return 0;
        return ambiguousActionList.size();
    }

    public double getUnambiguousActionCount(){
        if (unambiguousActionList == null)
            return 0;
        return unambiguousActionList.size();
    }

    public List<StmtEditActionGroup> getStmtEditGroupList(){
        return stmtEditGroupList;
    }

    private void calStmtEditActions(Map<ITree, List<TokenEditAction>> srcStmtEditMap,
                                   Map<ITree, List<TokenEditAction>> dstStmtEditMap){
        if (ms != null){
            generators = new ArrayList<>();
            for (ITree srcStmt: allSrcStmts){
                if (removedSrcStmts.contains(srcStmt))
                    continue;
                ITree dstStmt = ms.getDstForSrc(srcStmt);
                TokenActionGenerator generator = new TokenActionGenerator(ms, srcTreeWordMap,
                        dstTreeWordMap, srcStmt, dstStmt);
                generator.calWordEditActions();
                List<TokenEditAction> actions = generator.getOrderedActions();
                srcStmtEditMap.put(srcStmt, actions);
                if (dstStmt != null)
                    dstStmtEditMap.put(dstStmt, actions);
                generators.add(generator);
            }

            for (ITree dstStmt: allDstStmts){
                if (removedDstStmts.contains(dstStmt))
                    continue;
                if (dstStmtEditMap.containsKey(dstStmt))
                    continue;
                if (ms.isDstMapped(dstStmt))
                    throw new RuntimeException("Unhandled Ambiguous Mapping?");
                TokenActionGenerator generator = new TokenActionGenerator(ms, srcTreeWordMap,
                        dstTreeWordMap, null, dstStmt);
                generator.calWordEditActions();
                List<TokenEditAction> actions = generator.getOrderedActions();
                dstStmtEditMap.put(dstStmt, actions);
                generators.add(generator);
            }
        }
    }

    private List<StmtEditActionGroup> calEditActionGroups(){
        Map<ITree, List<TokenEditAction>> srcStmtEditMap = new HashMap<>();
        Map<ITree, List<TokenEditAction>> dstStmtEditMap = new HashMap<>();
        calStmtEditActions(srcStmtEditMap, dstStmtEditMap);
        List<StmtEditActionGroup> groups = new ArrayList<>();
        Map<ITree, StmtEditActionGroup> srcStmtEditGroupMap = new HashMap<>();
        Set<ITree> addedSrcStmts = new HashSet<>();
        Set<ITree> addedDstStmts = new HashSet<>();
        if (ms != null){
            for (ITree srcStmt: allSrcStmts){
                if (removedSrcStmts.contains(srcStmt)) {
                    addedSrcStmts.add(srcStmt);
                    continue;
                }
                if (addedSrcStmts.contains(srcStmt))
                    continue;
                if (ms.isSrcMapped(srcStmt)){
                    ITree dstStmt = ms.getDstForSrc(srcStmt);
                    StmtEditActionGroup g = new StmtEditActionGroup(srcStmt, dstStmt, ms,
                            srcStmtEditMap, dstStmtEditMap);
                    g.calStmtInfo(srcTreeWordMap, dstTreeWordMap, ms);
                    groups.add(g);
                    srcStmtEditGroupMap.put(srcStmt, g);
                    addedSrcStmts.add(srcStmt);
                    addedDstStmts.add(dstStmt);
                } else {
                    StmtEditActionGroup g = new StmtEditActionGroup(srcStmt, null, ms,
                            srcStmtEditMap, dstStmtEditMap);
                    g.calStmtInfo(srcTreeWordMap, dstTreeWordMap, ms);
                    groups.add(g);
                    srcStmtEditGroupMap.put(srcStmt, g);
                    addedSrcStmts.add(srcStmt);
                }
            }

            Set<ITree> movedSrcStmts = new HashSet<>();
            for (StmtEditActionGroup g: groups){
                if (g.isMoved())
                    movedSrcStmts.add(g.getSrcStmt());
            }

            for (StmtEditActionGroup g: groups){
                if (g.isMoved())
                    continue;
                g.checkMoveFromAncestorStmts(movedSrcStmts);
                if (g.isMoved())
                    movedSrcStmts.add(g.getSrcStmt());
            }

            for (ITree dstStmt: allDstStmts){
                if (removedDstStmts.contains(dstStmt)){
                    addedDstStmts.add(dstStmt);
                    continue;
                }
                if (addedDstStmts.contains(dstStmt))
                    continue;
                if (ms.isDstMapped(dstStmt))
                    throw new RuntimeException("Dst stmt mapped but not added");
                StmtEditActionGroup g = new StmtEditActionGroup(null, dstStmt, ms,
                        srcStmtEditMap, dstStmtEditMap);
                g.calStmtInfo(srcTreeWordMap, dstTreeWordMap, ms);
                groups.add(g);
                addedDstStmts.add(dstStmt);
            }
        }

        groups.sort((o1, o2) -> {
            ITree srcStmt1 = o1.getSrcStmt();
            ITree dstStmt1 = o1.getDstStmt();
            ITree srcStmt2 = o2.getSrcStmt();
            ITree dstStmt2 = o2.getDstStmt();
            int srcStmtGitLine1 = getGitLineIndexOfStmt(srcStmt1, true);
            int dstStmtGitLine1 = getGitLineIndexOfStmt(dstStmt1, false);
            int srcStmtGitLine2 = getGitLineIndexOfStmt(srcStmt2, true);
            int dstStmtGitLine2 = getGitLineIndexOfStmt(dstStmt2, false);
            int stmtGitLine1 = getSmallerOne(srcStmtGitLine1, dstStmtGitLine1);
            int stmtGitLine2 = getSmallerOne(srcStmtGitLine2, dstStmtGitLine2);
            return stmtGitLine1 - stmtGitLine2;
        });
        return groups;
    }

    private int getSmallerOne(int number1, int number2){
        if (number1 == -1)
            return number2;
        if (number2 == -1)
            return number1;
        return Math.min(number1, number2);
    }

    private int getGitLineIndexOfStmt(ITree stmt, boolean isSrc){
        if (stmt == null)
            return -1;
        if (isSrc)
            return gitChunkHandler.getIndexOfLine(srcRc.getLineNumberOfPos(stmt.getPos()), true);
        else
            return gitChunkHandler.getIndexOfLine(dstRc.getLineNumberOfPos(stmt.getPos()), false);
    }

    public double getSrcTreeNodeCount() {
        return srcTreeNodeCount;
    }

    public double getDstTreeNodeCount() {
        return dstTreeNodeCount;
    }

    private static boolean hasAmbiguousMapping(MappingStore ms, ITree srcRoot, ITree dstRoot){
        if (ms == null)
            return false;
        if (srcRoot != null) {
            for (ITree t : srcRoot.preOrder()) {
                if (ms.isSrcMapped(t)) {
                    ITree mapDst = ms.getDstForSrc(t);
                    if (mapDst == null)
                        return true;
                    if (!ms.isDstMapped(mapDst)) {
                        return true;
                    }
                    if (ms.getSrcForDst(mapDst) != t) {
                        return true;
                    }
                }
            }
        }

        if (dstRoot != null){
            for (ITree t: dstRoot.preOrder()){
                if (ms.isDstMapped(t)){
                    ITree mapSrc = ms.getSrcForDst(t);
                    if (!ms.isSrcMapped(mapSrc)) {
                        return true;
                    }
                    if (ms.getDstForSrc(mapSrc) != t) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public String getDstFilePath() {
        return dstFilePath;
    }
}
