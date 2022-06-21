package cs.zju.framework;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import cs.zju.framework.match.UniversalTreeNodeMaps;
import cs.zju.stm.MatchingCreator;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.match.TokenRangeTypeMap;
import cs.zju.treeutils.RangeCalculation;

import java.util.List;
import java.util.Set;

public class ComparisonInfo {
    private String project;
    private String commitId;
    private String mapMethod;
    private int srcToDstAmbiguousCount = 0;
    private int dstToSrcAmbiguousCount = 0;
    private double mapTime;
    private double treeActionGenerateTime;
    private List<Action> ambiguousActionList;
    private List<Action> unambiguousActionList;
    private int srcTreeCount;
    private int dstTreeCount;
    private MatchingCreator matchingCreator;
    private ITree srcRoot;
    private ITree dstRoot;
    private RangeCalculation srcRc;
    private RangeCalculation dstRc;
    private String srcFilePath;
    private String dstFilePath;
    private TreeTokensMap srcTreeTokenMap;
    private TreeTokensMap dstTreeTokenMap;
    private TokenRangeTypeMap srcTokenTypeMap;
    private TokenRangeTypeMap dstTokenTypeMap;
    private List<ITree> allSrcStmts;
    private List<ITree> allDstStmts;
    private Set<ITree> removedSrcStmts;
    private Set<ITree> removedDstStmts;

    private UniversalTreeNodeMaps srcUniversalNodeMaps;
    private UniversalTreeNodeMaps dstUniversalNodeMaps;

    public void setProject(String project) {
        this.project = project;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public void setMapMethod(String mapMethod) {
        this.mapMethod = mapMethod;
    }

    public void setSrcToDstAmbiguousCount(int srcToDstAmbiguousCount) {
        this.srcToDstAmbiguousCount = srcToDstAmbiguousCount;
    }

    public void setDstToSrcAmbiguousCount(int dstToSrcAmbiguousCount) {
        this.dstToSrcAmbiguousCount = dstToSrcAmbiguousCount;
    }

    public void setMapTime(double mapTime) {
        this.mapTime = mapTime;
    }

    public void setAmbiguousActionList(List<Action> ambiguousActionList) {
        this.ambiguousActionList = ambiguousActionList;
    }

    public void setUnambiguousActionList(List<Action> unambiguousActionList) {
        this.unambiguousActionList = unambiguousActionList;
    }

    public void setSrcTreeCount(int srcTreeCount) {
        this.srcTreeCount = srcTreeCount;
    }

    public void setDstTreeCount(int dstTreeCount) {
        this.dstTreeCount = dstTreeCount;
    }

    public void setSrcRoot(ITree srcRoot) {
        this.srcRoot = srcRoot;
    }

    public void setDstRoot(ITree dstRoot) {
        this.dstRoot = dstRoot;
    }

    public void setSrcRc(RangeCalculation srcRc) {
        this.srcRc = srcRc;
    }

    public void setDstRc(RangeCalculation dstRc) {
        this.dstRc = dstRc;
    }

    public void setSrcFilePath(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }

    public void setDstFilePath(String dstFilePath) {
        this.dstFilePath = dstFilePath;
    }

    public void setSrcTreeTokenMap(TreeTokensMap srcTreeTokenMap) {
        this.srcTreeTokenMap = srcTreeTokenMap;
    }

    public void setDstTreeTokenMap(TreeTokensMap dstTreeTokenMap) {
        this.dstTreeTokenMap = dstTreeTokenMap;
    }

    public void setAllDstStmts(List<ITree> allDstStmts) {
        this.allDstStmts = allDstStmts;
    }

    public void setAllSrcStmts(List<ITree> allSrcStmts) {
        this.allSrcStmts = allSrcStmts;
    }

    public void setRemovedDstStmts(Set<ITree> removedDstStmts) {
        this.removedDstStmts = removedDstStmts;
    }

    public void setRemovedSrcStmts(Set<ITree> removedSrcStmts) {
        this.removedSrcStmts = removedSrcStmts;
    }

    public void setTreeActionGenerateTime(double treeActionGenerateTime) {
        this.treeActionGenerateTime = treeActionGenerateTime;
    }

    public void setMatchingCreator(MatchingCreator matchingCreator) {
        this.matchingCreator = matchingCreator;
    }

    public void setSrcUniversalNodeMaps(UniversalTreeNodeMaps srcUniversalNodeMaps) {
        this.srcUniversalNodeMaps = srcUniversalNodeMaps;
    }

    public void setDstUniversalNodeMaps(UniversalTreeNodeMaps dstUniversalNodeMaps) {
        this.dstUniversalNodeMaps = dstUniversalNodeMaps;
    }

    public void setSrcTokenTypeMap(TokenRangeTypeMap srcTokenTypeMap) {
        this.srcTokenTypeMap = srcTokenTypeMap;
    }

    public void setDstTokenTypeMap(TokenRangeTypeMap dstTokenTypeMap) {
        this.dstTokenTypeMap = dstTokenTypeMap;
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

    public int getSrcToDstAmbiguousCount() {
        return srcToDstAmbiguousCount;
    }

    public int getDstToSrcAmbiguousCount() {
        return dstToSrcAmbiguousCount;
    }

    public double getMapTime() {
        return mapTime;
    }

    public List<Action> getAmbiguousActionList() {
        return ambiguousActionList;
    }

    public List<Action> getUnambiguousActionList() {
        return unambiguousActionList;
    }

    public int getSrcTreeCount() {
        return srcTreeCount;
    }

    public int getDstTreeCount() {
        return dstTreeCount;
    }

    public MatchingCreator getMatchingCreator() {
        return matchingCreator;
    }

    public RangeCalculation getSrcRc() {
        return srcRc;
    }

    public RangeCalculation getDstRc() {
        return dstRc;
    }

    public TreeTokensMap getSrcTreeTokenMap() {
        return srcTreeTokenMap;
    }

    public TreeTokensMap getDstTreeTokenMap() {
        return dstTreeTokenMap;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public String getDstFilePath() {
        return dstFilePath;
    }

    public List<ITree> getAllSrcStmts() {
        return allSrcStmts;
    }

    public List<ITree> getAllDstStmts() {
        return allDstStmts;
    }

    public ITree getSrcRoot() {
        return srcRoot;
    }

    public ITree getDstRoot() {
        return dstRoot;
    }

    public Set<ITree> getRemovedSrcStmts() {
        return removedSrcStmts;
    }

    public Set<ITree> getRemovedDstStmts() {
        return removedDstStmts;
    }

    public double getTreeActionGenerateTime() {
        return treeActionGenerateTime;
    }

    public UniversalTreeNodeMaps getDstUniversalNodeMaps() {
        return dstUniversalNodeMaps;
    }

    public UniversalTreeNodeMaps getSrcUniversalNodeMaps() {
        return srcUniversalNodeMaps;
    }

    public TokenRangeTypeMap getSrcTokenTypeMap() {
        return srcTokenTypeMap;
    }

    public TokenRangeTypeMap getDstTokenTypeMap() {
        return dstTokenTypeMap;
    }
}
