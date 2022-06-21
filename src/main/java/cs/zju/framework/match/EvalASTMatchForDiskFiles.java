package cs.zju.framework.match;

import cs.zju.framework.MatchASTAndTokens;
import cs.zju.framework.MatchASTAndTokensOnDiskFile;
import cs.zju.stm.match.StmtMatch;
import cs.zju.stm.match.StmtMatchDiffDesc;

import java.util.*;

public class EvalASTMatchForDiskFiles {
    private List<String> matchAlgorithms;
    private String srcFilePath;
    private String dstFilePath;

    Map<String, EvalASTMatchResult> methodResultMap;

    public EvalASTMatchForDiskFiles(String srcFilePath, String dstFilePath,
                                    List<String> matchAlgorithms){
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;
        this.matchAlgorithms = matchAlgorithms;
        methodResultMap = new HashMap<>();
        UniversalTreeNode.initNodeFactoryMap();
        getMatchResult();
    }

    private void getMatchResult(){
        boolean nullResult = false;
        methodResultMap = new HashMap<>();
        for (String algorithm: matchAlgorithms) {
            MatchASTAndTokens eval = new MatchASTAndTokensOnDiskFile(srcFilePath, dstFilePath, "Test.java",
                    algorithm, "tmp", true);
            EvalASTMatchResult result = eval.getASTMatchResult();
            if (result == null){
                nullResult = true;
                break;
            }
            methodResultMap.put(algorithm, result);
        }
        if (nullResult){
            methodResultMap = null;
        }
    }

    public List<StmtMatchDiffDesc> doCompare(String algorithm1, String algorithm2){
        EvalASTMatchResult algorithm1Result = methodResultMap.get(algorithm1);
        EvalASTMatchResult algorithm2Result = methodResultMap.get(algorithm2);

        List<StmtMatchDiffDesc> descs = new ArrayList<>();
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
