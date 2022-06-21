package cs.zju.eva.utils;

import cs.zju.eva.editfw.FileRevisionMeasures;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.io.File;
import java.util.*;

public class EvalCommitFileInfo {
    private Map<String, Set<String>> goodCommitFiles = new HashMap<>();

    public static void main(String[] args) throws Exception{
        String project = "activemq";
        Map<String, Set<String>> commitFileMap = getProcessedCommitFilesInCsv(project, "mtdiff");
        for (String p: commitFileMap.keySet()){
            System.out.println(p);
            System.out.println(commitFileMap.get(p));
        }
    }

    public static Map<String, Set<String>> getProcessedCommitFilesInCsv(String project, String mapMethod) throws Exception{
        Map<String, Set<String>> ret = new HashMap();
        String[] headers = FileRevisionMeasures.getHeaders();

        String resultCsvPath = PathResolver.getCombineResultCsvPath("backup6/" + project, mapMethod);
        if (!new File(resultCsvPath).exists())
            return null;

        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, headers);
        for (String[] record: records){
            String commitId = record[2];
            String filePath = record[3];
            if (!ret.containsKey(commitId))
                ret.put(commitId, new HashSet<>());
            ret.get(commitId).add(filePath);
        }
        return ret;
    }

}
