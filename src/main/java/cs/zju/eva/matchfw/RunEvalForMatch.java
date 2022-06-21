package cs.zju.eva.matchfw;

import java.io.IOException;

public class RunEvalForMatch {
    private static final String[] projects = {
//            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    public static void main(String[] args) throws IOException {
        for (String project: projects){
            MatchEvaluation.init(project);
            MatchEvaluation.run();
        }
    }
}
