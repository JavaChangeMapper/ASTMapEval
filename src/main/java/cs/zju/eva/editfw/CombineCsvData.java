package cs.zju.eva.editfw;

import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CombineCsvData {

    public static void combineCsvData(String project, String method, boolean isStmt) throws Exception{
        List<String[]> combineData = combineData(project, method, isStmt);
        String resultCsvPath = PathResolver.getCombineResultCsvPath(project, method);
        String[] headers = FileRevisionMeasures.getHeaders();
        if (isStmt) {
            resultCsvPath = PathResolver.getCombineStmtResultCsvPath(project, method);
            headers = StmtEvaluationMeasures.getHeaders();
        }

        CsvOperationsUtil.writeCSV(resultCsvPath, headers, combineData);
    }

    public static List<String[]> combineData(String project, String mapMethod, boolean isStmt){
        int idx = 1;
        List<String[]> ret = new ArrayList<>();
        try {
            while (true) {
                List<String[]> records = getCsvRecords(project, mapMethod, idx, isStmt);
                if (records.size() == 0)
                    break;
                ret.addAll(records);
                idx++;
            }
        } catch (Exception e){
            throw new RuntimeException("read csv error");
        }
        return ret;
    }

    private static List<String[]> getCsvRecords(String project, String mapMethod, int idx, boolean isStmt) throws Exception{
        String[] headers = FileRevisionMeasures.getHeaders();

        String resultCsvPath = PathResolver.getResultCsv(project, mapMethod, idx);
        if (isStmt) {
            resultCsvPath = PathResolver.getStmtResultCsv(project, mapMethod, idx);
            headers = StmtEvaluationMeasures.getHeaders();
        }

        if (!new File(resultCsvPath).exists())
            return new ArrayList<>();

        return CsvOperationsUtil.getCSVData(resultCsvPath, headers);
    }
}
