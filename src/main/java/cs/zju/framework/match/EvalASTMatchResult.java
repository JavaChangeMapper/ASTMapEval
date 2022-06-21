package cs.zju.framework.match;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.match.StmtMatch;
import cs.zju.framework.ComparisonInfo;
import cs.zju.stm.match.StmtMatchDiffDesc;
import cs.zju.stm.match.StmtMatchErrorDesc;
import cs.zju.stm.match.TokenRangeTypeMap;
import cs.zju.treeutils.RangeCalculation;

import java.util.*;


public class EvalASTMatchResult {
    private String matchAlgorithm;
    private MappingStore ms;
    private TreeTokensMap srcTreeTokenMap;
    private TreeTokensMap dstTreeTokenMap;

    private TokenRangeTypeMap srcTokenTypeMap;
    private TokenRangeTypeMap dstTokenTypeMap;

    private List<ITree> allSrcStmts;
    private List<ITree> allDstStmts;

    private UniversalTreeNodeMaps srcUniversalNodeMaps;
    private UniversalTreeNodeMaps dstUniversalNodeMaps;

    // stmtMatchList
    private List<StmtMatch> stmtMatchList;
    // we map each statement (composed by its type and start pos) to the matching of its program elements
    private StmtMatchMaps stmtMatchingMaps;
    // edit actions
    private List<Action> actionList;
    // map of stmt match and lines
    private Map<Integer, StmtMatch> srcPosStmtMatchListMap;
    private Map<Integer, StmtMatch> dstPosStmtMatchListMap;

    private double treeMappingTime;
    private double treeActionGenerateTime;

    public EvalASTMatchResult(ComparisonInfo comparison, MappingStore ms){
        this.ms = ms;
        this.srcTreeTokenMap = comparison.getSrcTreeTokenMap();
        this.dstTreeTokenMap = comparison.getDstTreeTokenMap();
        this.srcTokenTypeMap = comparison.getSrcTokenTypeMap();
        this.dstTokenTypeMap = comparison.getDstTokenTypeMap();
        this.allSrcStmts = comparison.getAllSrcStmts();
        this.allDstStmts = comparison.getAllDstStmts();
        this.srcUniversalNodeMaps = comparison.getSrcUniversalNodeMaps();
        this.dstUniversalNodeMaps = comparison.getDstUniversalNodeMaps();
        this.matchAlgorithm = comparison.getMapMethod();
        this.actionList = comparison.getUnambiguousActionList();
        this.treeMappingTime = comparison.getMapTime();
        this.treeActionGenerateTime = comparison.getTreeActionGenerateTime();
        generateStmtMatchingMaps();
    }

    private void generateStmtMatchingMaps(){
        stmtMatchList = new ArrayList<>();
        for (ITree srcStmt: allSrcStmts){
            ITree mappedDstStmt = ms.getDstForSrc(srcStmt);
            StmtMatch sm = new StmtMatch(srcStmt, mappedDstStmt, ms,
                    srcTreeTokenMap, dstTreeTokenMap, srcTokenTypeMap, dstTokenTypeMap,
                    srcUniversalNodeMaps, dstUniversalNodeMaps, matchAlgorithm);
            stmtMatchList.add(sm);
        }
        for (ITree dstStmt: allDstStmts){
            if (!ms.isDstMapped(dstStmt)){
                StmtMatch sm = new StmtMatch(null, dstStmt, ms,
                        srcTreeTokenMap, dstTreeTokenMap, srcTokenTypeMap, dstTokenTypeMap,
                        srcUniversalNodeMaps, dstUniversalNodeMaps, matchAlgorithm);
                stmtMatchList.add(sm);
            }
        }
        stmtMatchingMaps = new StmtMatchMaps(stmtMatchList, matchAlgorithm);
        srcPosStmtMatchListMap = new HashMap<>();
        dstPosStmtMatchListMap = new HashMap<>();
        for (StmtMatch sm: stmtMatchList){
            srcPosStmtMatchListMap.put(sm.getSrcStartPos(), sm);
            dstPosStmtMatchListMap.put(sm.getDstStartPos(), sm);
        }
    }

    public List<StmtMatch> getStmtMatchList() {
        return stmtMatchList;
    }

    public StmtMatchMaps getStmtMatchingMaps() {
        return stmtMatchingMaps;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public double getTreeMappingTime() {
        return treeMappingTime;
    }

    public double getTreeActionGenerateTime() {
        return treeActionGenerateTime;
    }

    public StmtMatch getStmtMatch(int pos, boolean isSrc){
        if (isSrc){
            return srcPosStmtMatchListMap.get(pos);
        } else {
            return dstPosStmtMatchListMap.get(pos);
        }
    }

    public List<StmtMatchDiffDesc> getStmtDiffDescriptors(EvalASTMatchResult otherAlgorithmResult){
        StmtMatchMaps otherMatchMaps= otherAlgorithmResult.getStmtMatchingMaps();
        List<StmtMatchDiffDesc> ret = new ArrayList<>();
        Map<StmtMatch, StmtMatchDiffDesc> stmtMatchDescMap = new HashMap<>();
        for (StmtMatch sm: stmtMatchList){
            StmtMatchDiffDesc diffDesc = sm.calculateStmtMatchDiffDesc(otherMatchMaps);
            if (diffDesc.getType().equals(""))
                continue;
            StmtMatchErrorDesc errorDesc = StmtMatch.calculateStmtMatchErrorDesc(stmtMatchingMaps, otherMatchMaps, sm);

            if (errorDesc != null) {
                if (!errorDesc.getErrorType().equals("")) {
                    diffDesc.setErrorDesc(errorDesc);
                }
            }
            stmtMatchDescMap.put(sm, diffDesc);
        }

        for (StmtMatch sm: stmtMatchList){
            if (stmtMatchDescMap.containsKey(sm))
                ret.add(stmtMatchDescMap.get(sm));
        }
        return ret;
    }

}
