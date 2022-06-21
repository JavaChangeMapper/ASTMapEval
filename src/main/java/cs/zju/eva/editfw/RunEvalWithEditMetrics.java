package cs.zju.eva.editfw;

public class RunEvalWithEditMetrics {
    private static final String[] projects = {
            "activemq"
    };

    public static void main(String[] args) throws Exception {
        for (String project: projects){
            EvaluationWithEditMetrics.init(project);
            EvaluationWithEditMetrics.run();
        }
    }
}
