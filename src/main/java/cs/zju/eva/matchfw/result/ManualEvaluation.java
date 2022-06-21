package cs.zju.eva.matchfw.result;

import cs.zju.eva.editfw.metrics.accuracy.Analysis;
import cs.zju.eva.utils.FileRevision;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ManualEvaluation {

    private static String[] projects = {
            "junit4"
//            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
//            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static String[] algorithms = {
            "gt", "mtdiff", "ijm"
    };

    private final static int fileRevisionNum = 100;

    public static void main(String[] args) throws IOException {
        for (String project: projects){
            generateManualSetForEval(project);
        }
    }

    // need to know how many file revisions are impacted
    private static void generateManualSetForEval(String project) throws IOException {
        for (int i = 0; i < algorithms.length - 1; i++){
            for (int j = i + 1; j < algorithms.length; j++){
                String method1 = algorithms[i];
                String method2 = algorithms[j];
                List<FileRevision> revisions = generateManualFileRevisions(project, method1, method2);
                AnalysisUtils.generateManualAnalysisScriptAndCsv(project, method1, method2, revisions, fileRevisionNum);
            }
        }
    }

    private static List<FileRevision> generateManualFileRevisions(String project, String method1, String method2){
        List<FileRevision> inconsistentRevisions = new ArrayList<>();
        Map<FileRevision, List<ComparisonRecord>> allRecordMap = new HashMap<>();
        Map<FileRevision, List<ComparisonRecord>> errorRecordMap = new HashMap<>();

        AnalysisUtils.calAllRecords(project, method1, method2, inconsistentRevisions,
                allRecordMap, errorRecordMap, true);
        Collections.shuffle(inconsistentRevisions);
        return inconsistentRevisions.subList(0, 100);
    }
}
