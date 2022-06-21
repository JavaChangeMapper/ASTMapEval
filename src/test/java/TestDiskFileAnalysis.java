import cs.zju.framework.MatchASTAndTokensOnDiskFile;
import cs.zju.framework.edit.EvalComparisonResultWithEdit;
import cs.zju.framework.MatchASTAndTokens;
import cs.zju.stm.edit.tokenedit.actions.StmtEditActionGroup;

public class TestDiskFileAnalysis {
    public static void main(String[] args){
        String srcFilePath = "G://MyStudy/ASTMatchEval/src/test/resources/source/Test.java";
        String dstFilePath = "G://MyStudy/ASTMatchEval/src/test/resources/destination/Test.java";

        MatchASTAndTokens eval = new MatchASTAndTokensOnDiskFile(srcFilePath, dstFilePath, "Test.java",
                "ijm", "tmp", true);
        EvalComparisonResultWithEdit result = eval.getOriginComparisonResult();
        System.out.println(result.getMoveStmtCount(true));
        System.out.println(result.getWordEdits());

        for (StmtEditActionGroup group: result.getStmtEditGroupList()){
            if (group.getStmtChangeType() != null){
                System.out.println(group);
            }
        }
    }
}
