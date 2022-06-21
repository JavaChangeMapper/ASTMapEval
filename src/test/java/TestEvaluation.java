import cs.zju.framework.edit.EvalCommitAnalysisWithEdit;
import cs.zju.framework.edit.EvalComparisonResultWithEdit;
import cs.zju.stm.edit.tokenedit.actions.StmtEditActionGroup;

import java.util.List;

public class TestEvaluation {
    public static void main(String[] args) throws Exception {
//        String project = "spring-roo";
//        String commitId = "04b76cbd1fb5f681a08305a95fba36419a323151";
//        String project = "hibernate-orm";
//        String commitId = "9d2b288c45025750cfebe401d6d99856630af2f2";
        String project = "activemq";
        String commitId = "8f40a7eeca8772a16e45a15ef424142e4f90e129";
//        String project = "commons-math";
//        String commitId = "a1dd05b8c81cc926fef50dd007babc4cb392247d";
//        String project = "activemq";
//        String commitId = "a558fef3745807fc4eb75508cd385798da4b64a5";
//        String project = "netty";
//        String commitId = "7acc333dd9ca933623944c02577b8d4dd888fc42";
        long startTime=System.currentTimeMillis();
        EvalCommitAnalysisWithEdit analysis = new EvalCommitAnalysisWithEdit(project, commitId, "gt", null);
        analysis.calMeasures(analysis.getOriginResultList());
        for (EvalComparisonResultWithEdit result: analysis.getOriginResultList()){
            System.out.println(result.getSrcFilePath());
            System.out.println(result.getWordEdits());
            System.out.println(result.getMoveStmtCount(true));

            List<StmtEditActionGroup> groups = result.getStmtEditGroupList();
            for (StmtEditActionGroup g: groups){
                if (g.getStmtChangeType() == null)
                    continue;
                System.out.println(g);
                System.out.println(g.getActions().size());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime - startTime));

        System.out.println("Total time1: " + (System.currentTimeMillis() - startTime));
        System.out.println(analysis.getWordEdits());
        System.out.println(analysis.getMoveStmtCount());
        System.out.println(analysis.getAlignStmtsNumber());
        System.out.println(analysis.getLowQualityMappedWordCount());
    }
}
