package cs.zju.framework.match;

import cs.zju.config.MyConfig;
import cs.zju.stm.match.StmtMatch;
import cs.zju.stm.match.StmtMatchDiffDesc;
import org.eclipse.jdt.core.util.OpcodeStringValues;

import java.util.*;

public class InconsistentMatch {
    private String project;
    private String commitId;
    private String filePath;
    private String curAlgorithm;
    private String compareAlgorithm;
    private List<StmtMatchDiffDesc> matchDiffDescs;
    private List<String[]> matchDiffRecords;
    private List<String> matchDiffStrs;

    private List<StmtMatchDiffDesc> combineMatchDiffDescs;
//    private List<String[]> combineMatchDiffRecords;
//    private List<String> combineMatchDiffStrs;

    public InconsistentMatch(String project, String commitId, String filePath,
                             String curAlgorithm, String compareAlgorithm,
                             Map<String, EvalASTMatchResult> algorithmMatchResultMap,
                             boolean combine){
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
        this.curAlgorithm = curAlgorithm;
        this.compareAlgorithm = compareAlgorithm;
        if (combine)
            matchDiffDescs = doCompareAndCombine(curAlgorithm, compareAlgorithm, algorithmMatchResultMap);
        else
            matchDiffDescs = doCompare(curAlgorithm, compareAlgorithm, algorithmMatchResultMap);

        this.matchDiffRecords = calAllMatchDiffRecords();
        this.matchDiffStrs = calAllMatchDiffStrings();
//        this.combineMatchDiffRecords = calAllCombinedMatchDiffRecords();
//        this.combineMatchDiffStrs = calAllCombineMatchDiffStrings();

        clearMemory();
    }

    public String getCurAlgorithm() {
        return curAlgorithm;
    }

    public String getCompareAlgorithm() {
        return compareAlgorithm;
    }

    public List<StmtMatchDiffDesc> getMatchDiffDescs() {
        return matchDiffDescs;
    }

    public List<String[]> getMatchDiffRecords() {
        return matchDiffRecords;
    }

    public List<String> getMatchDiffStrs() {
        return matchDiffStrs;
    }

//    public List<String[]> getCombineMatchDiffRecords() {
//        return combineMatchDiffRecords;
//    }

//    public List<String> getCombineMatchDiffStrs() {
//        return combineMatchDiffStrs;
//    }

    private List<String[]> calAllMatchDiffRecords(){
        List<String[]> records = new ArrayList<>();
        for (StmtMatchDiffDesc desc: matchDiffDescs){
            records.add(desc.toCsvRecord(commitId, filePath));
        }
        return records;
    }

    private List<String> calAllMatchDiffStrings(){
        List<String> stringValues = new ArrayList<>();
        for (StmtMatchDiffDesc desc: matchDiffDescs){
            stringValues.add(createCommitInfo());
            stringValues.add(desc.toString());
        }
        return stringValues;
    }

//    private List<String[]> calAllCombinedMatchDiffRecords(){
//        if (combineMatchDiffDescs == null)
//            return new ArrayList<>();
//        List<String[]> records = new ArrayList<>();
//        for (StmtMatchDiffDesc desc: combineMatchDiffDescs){
//            records.add(desc.toCsvRecord(commitId, filePath));
//        }
//        return records;
//    }

    private String createCommitInfo(){
        String url = MyConfig.getCommitUrl(project, commitId);
        String ret = url + "\n" + filePath + "\n";
        return ret;
    }

//    private List<String> calAllCombineMatchDiffStrings(){
//        if (combineMatchDiffDescs == null)
//            return new ArrayList<>();
//        List<String> stringValues = new ArrayList<>();
//        for (StmtMatchDiffDesc desc: combineMatchDiffDescs){
//            String descStr = desc.toString();
//            stringValues.add(createCommitInfo() + descStr);
//        }
//        return stringValues;
//    }

    private void clearMemory(){
        matchDiffDescs.clear();
        if (combineMatchDiffDescs != null)
            combineMatchDiffDescs.clear();
        matchDiffDescs = null;
        combineMatchDiffDescs = null;
    }

    private List<StmtMatchDiffDesc> doCompare(String algorithm1, String algorithm2,
                                              Map<String, EvalASTMatchResult> matchResultMap){
        EvalASTMatchResult algorithm1Result = matchResultMap.get(algorithm1);
        EvalASTMatchResult algorithm2Result = matchResultMap.get(algorithm2);
        return algorithm1Result.getStmtDiffDescriptors(algorithm2Result);
    }

    // compare two algorithms
    private List<StmtMatchDiffDesc> doCompareAndCombine(String algorithm1, String algorithm2,
                                                        Map<String, EvalASTMatchResult> matchResultMap){
        List<StmtMatchDiffDesc> descs = new ArrayList<>();
        EvalASTMatchResult algorithm1Result = matchResultMap.get(algorithm1);
        EvalASTMatchResult algorithm2Result = matchResultMap.get(algorithm2);

        List<StmtMatchDiffDesc> diffDescs1 = algorithm1Result.getStmtDiffDescriptors(algorithm2Result);
        List<StmtMatchDiffDesc> diffDescs2 = algorithm2Result.getStmtDiffDescriptors(algorithm1Result);

        Map<UniversalTreeNode, StmtMatchDiffDesc> srcStmtDiffDescMap = new HashMap<>();
        Map<UniversalTreeNode, StmtMatchDiffDesc> dstStmtDiffDescMap = new HashMap<>();

        for (StmtMatchDiffDesc diffDesc: diffDescs2){
            UniversalTreeNode srcUniversalStmt = diffDesc.getSrcUniversalStmt();
            UniversalTreeNode dstUniversalStmt = diffDesc.getDstUniversalStmt();
            if (!srcUniversalStmt.isNull()){
                srcStmtDiffDescMap.put(srcUniversalStmt, diffDesc);
            }
            if (!dstUniversalStmt.isNull()){
                dstStmtDiffDescMap.put(dstUniversalStmt, diffDesc);
            }
        }

        Set<StmtMatchDiffDesc> tmpSet = new HashSet<>();
        for (StmtMatchDiffDesc diffDesc: diffDescs1){
            descs.add(diffDesc);
            UniversalTreeNode srcUniversalStmt = diffDesc.getSrcUniversalStmt();
            UniversalTreeNode dstUniversalStmt = diffDesc.getDstUniversalStmt();
            if (!srcUniversalStmt.isNull() && srcStmtDiffDescMap.containsKey(srcUniversalStmt)){
                StmtMatchDiffDesc diffDesc2 = srcStmtDiffDescMap.get(srcUniversalStmt);
                if (!tmpSet.contains(diffDesc2)){
                    descs.add(diffDesc2);
                    tmpSet.add(diffDesc2);
                }
            }
            if (!dstUniversalStmt.isNull() && dstStmtDiffDescMap.containsKey(dstUniversalStmt)){
                StmtMatchDiffDesc diffDesc2 = dstStmtDiffDescMap.get(dstUniversalStmt);
                if (!tmpSet.contains(diffDesc2)){
                    descs.add(diffDesc2);
                    tmpSet.add(diffDesc2);
                }
            }
        }
        return descs;
    }
}
