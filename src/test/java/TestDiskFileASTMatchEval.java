import cs.zju.framework.match.EvalASTMatchForDiskFiles;
import cs.zju.framework.match.EvalASTMatchResult;
import cs.zju.stm.match.StmtMatchDiffDesc;

import java.util.ArrayList;
import java.util.List;

public class TestDiskFileASTMatchEval {
    public static void main(String[] args){
        String srcFilePath = "G://MYStudy/ASTMatchEval/src/test/resources/source/Test1.java";
        String dstFilePath = "G://MyStudy/ASTMatchEval/src/test/resources/destination/Test1.java";
        List<String> algorithms = new ArrayList<>();
        algorithms.add("gt");
//        algorithms.add("mtdiff");
        algorithms.add("ijm");

        EvalASTMatchForDiskFiles eval = new EvalASTMatchForDiskFiles(srcFilePath, dstFilePath, algorithms);
        List<StmtMatchDiffDesc> matchingDiffDesc =  eval.doCompare("gt", "ijm");
//
        for (StmtMatchDiffDesc desc: matchingDiffDesc){
            System.out.println(desc);
        }
    }
}
