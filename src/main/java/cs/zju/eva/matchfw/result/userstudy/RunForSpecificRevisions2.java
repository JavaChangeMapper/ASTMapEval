package cs.zju.eva.matchfw.result.userstudy;

import cs.zju.eva.matchfw.result.AnalysisUtils;
import cs.zju.eva.matchfw.result.ComparisonRecord;
import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.io.File;
import java.util.*;

public class RunForSpecificRevisions2 {
    private static String[] algorithms = {"gt", "mtdiff", "ijm"};
    private static String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    public static void main(String[] args) throws Exception {
        Map<String, Set<FileRevision>> allFileRevisions = getAllFileRevisions();
        for (String project: projects){
            Set<FileRevision> revisions = allFileRevisions.get(project);
            AnalysisUtils.generateUserStudyApproachResult(project, "gt", "mtdiff", revisions);
            AnalysisUtils.generateUserStudyApproachResult(project, "gt", "ijm", revisions);
            AnalysisUtils.generateUserStudyApproachResult(project, "mtdiff", "ijm", revisions);
        }
    }

    private static Map<String, Set<FileRevision>> getAllFileRevisions() throws Exception {
        String humanAnalysisDirPath = PathResolver.getUserStudyResultDirPath();
        File dir = new File(humanAnalysisDirPath);
        File[] files = dir.listFiles();
        Map<String, Set<FileRevision>> projectRevisionMap = new HashMap<>();

        for (String project: projects){
            projectRevisionMap.put(project, new HashSet<>());
        }

        for (File file: files){
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(".csv")){
                List<String[]> records = CsvOperationsUtil.getCSVData(filePath, ResultAnalysis3.getHeaders());
                for (String[] record: records){
                    int fileId = Integer.parseInt(record[0]);
                    String commitId = record[1];
                    String revisionPath = record[2];
                    FileRevision fr = new FileRevision(commitId, revisionPath);
                    String project = projects[(fileId - 1) / 20];
                    projectRevisionMap.get(project).add(fr);
                }
            }
        }

        return projectRevisionMap;
    }
}
