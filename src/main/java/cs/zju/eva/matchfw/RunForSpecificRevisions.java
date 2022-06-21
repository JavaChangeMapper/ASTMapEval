package cs.zju.eva.matchfw;

import cs.zju.eva.matchfw.result.AnalysisUtils;
import cs.zju.eva.matchfw.result.ComparisonRecord;
import cs.zju.eva.matchfw.result.StmtRecord;
import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.util.*;

public class RunForSpecificRevisions {

    private static String[] algorithms = {"gt", "mtdiff", "ijm"};
    private static String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    public static void main(String[] args) throws Exception {
        for (String project: projects){
            for(int i = 0; i < algorithms.length - 1; i++){
                String algorithm1 = algorithms[i];
                for (int j = i + 1; j < algorithms.length; j++){
                    String algorithm2 = algorithms[j];
                    List<ComparisonRecord> recordList = getAllComparisonRecords(project, algorithm1, algorithm2);
                    List<FileRevision> revisions = getAllFileRevisions(recordList);

                    System.out.println(revisions.size());

                    AnalysisUtils.generateManualAnalysisScriptAndCsv(project, algorithm1, algorithm2,
                            revisions, -1);
                }
            }
        }
    }

    private static List<FileRevision> getAllFileRevisions(List<ComparisonRecord> recordList){
        Set<FileRevision> addedRevisions = new HashSet<>();
        List<FileRevision> ret = new ArrayList<>();
        for (ComparisonRecord record: recordList){
            FileRevision fr = new FileRevision(record.getCommitId(), record.getFilePath());
            if (addedRevisions.contains(fr))
                continue;
            ret.add(fr);
            addedRevisions.add(fr);
        }
        return ret;
    }

    private static List<ComparisonRecord> getAllComparisonRecords(String project, String algorithm1,
                                                                  String algorithm2) throws Exception {
        String filePath = PathResolver.getStmtMatchManualResultCsvPath(project, algorithm1, algorithm2);
        List<String[]> records = CsvOperationsUtil.getCSVData(filePath, getManualCsvHeaders());
        List<ComparisonRecord> retList = new ArrayList<>();
        for (String[] record: records){
            retList.add(new ComparisonRecord(record, true, false));
        }
        return retList;
    }

    public static String[] getManualCsvHeaders(){
        String[] headers = {
                "commitId", "filePath", "algorithm", "stmtType",
                "srcCharPos", "dstCharPos", "srcStmtLine", "dstStmtLine",
                "compareAlgorithm", "matchType", "srcStmtLine2", "dstStmtLine2",
                "srcStmtLine3", "dstStmtLine3", "ratioOfIdenticalToken",
                "betterAlgorithm", "errorType", "stmtMatchError",
                "tokenMatchError", "crossStmtMatchError", "srcTokenError",
                "dstTokenError"
        };
        return headers;
    }
}
