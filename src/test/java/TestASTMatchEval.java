import cs.zju.framework.match.EvalASTMatchForCommit;
import cs.zju.framework.match.InconsistentMatch;
import cs.zju.stm.match.StmtMatchDiffDesc;

import java.util.*;

public class TestASTMatchEval {
    public static void main(String[] args) throws Exception {
//        String project = "spring-roo";
//        String commitId = "04b76cbd1fb5f681a08305a95fba36419a323151";
//        String project = "hibernate-orm";
//        String commitId = "9d2b288c45025750cfebe401d6d99856630af2f2";
        String project = "hibernate-search";
        String commitId = "35bd2e4eea040726507f678d9f688da046d71c19";
//        String commitId = "06bfecb6eddbc28bd971c4879ec7ef30108a6bf8";
//        String commitId = "540dd5c987855fc6b0981ece021358645ae07827";
//        String project = "commons-math";
//        String commitId = "a1dd05b8c81cc926fef50dd007babc4cb392247d";
//        String project = "activemq";
//        String commitId = "a558fef3745807fc4eb75508cd385798da4b64a5";
//        String project = "netty";
//        String commitId = "7acc333dd9ca933623944c02577b8d4dd888fc42";
//        String project = "netty";
//        String commitId = "a0758e7e60e326b13ca8eeccf1a078d413793b41";
        long startTime=System.currentTimeMillis();
        List<String> algorithms = new ArrayList<>();
        algorithms.add("gt");
//        algorithms.add("mtdiff");
        algorithms.add("ijm");
        Set<String> fileAnalyze = null;
        fileAnalyze = new HashSet<>();
        fileAnalyze.add("src/java/org/hibernate/search/backend/impl/BatchedQueueingProcessor.java");
        EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, commitId,
                algorithms.toArray(new String[algorithms.size()]), fileAnalyze);
        Map<String, List<InconsistentMatch>> matchDiff = eval.getInconsistentMatchMap();
        System.out.println(matchDiff.size());
        for (String filePath: matchDiff.keySet()){
            System.out.println(filePath);
            System.out.println(matchDiff.get(filePath).size());
            for (InconsistentMatch im: matchDiff.get(filePath)){
                for (String str: im.getMatchDiffStrs()){
                    System.out.println(str);
                }
            }
        }
    }
}
