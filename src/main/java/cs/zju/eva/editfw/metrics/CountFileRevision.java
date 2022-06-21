package cs.zju.eva.editfw.metrics;

import cs.zju.eva.editfw.FileRevisionMeasures;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.utils.PathResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CountFileRevision {

    private final static String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private final static String COMMIT_ID = "commitId";

    public static void main(String[] args) throws Exception{
        int sum = 0;
        int sum2 = 0;
        for (String project: projects){
            System.out.println(project);
            String csvPath = PathResolver.getCombineResultCsvPath(project, "gt");
            CsvDataExtractor extractor = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders());
            List<String[]> records = extractor.getRecords();
            System.out.println(records.size());
            sum += records.size();
            Set<String> commits = new HashSet<>();
            for (String[] record: records){
                String commitId = extractor.getColVal(record, COMMIT_ID);
                commits.add(commitId);
            }
            sum2 += commits.size();
            System.out.println(commits.size());
        }

        System.out.println(sum);
        System.out.println(sum2);
    }
}
