package cs.zju.eva.editfw.metrics.manual;

import cs.zju.utils.PathResolver;
import cs.zju.utils.mycsv.CsvData;
import cs.zju.utils.mycsv.CsvLoader;
import cs.zju.utils.mycsv.CsvRecord;
import cs.zju.utils.mycsv.CsvWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManualCsvAnalysis {
    private static final String originPath = PathResolver.getManualAnalysisCsvPath();
    private static final String manualAnalysisResultPath = PathResolver.getManualAnalysisResultCsvPath();

    private static final String METRIC = "metric";
    private static final String PROJECT = "project";
    private static final String MAP_METHOD = "mapMethod";
    private static final String REASON = "Reason";

    private static Map<String, String> reasonMap;

    private static Map<String, Map<String, Integer>> reasonResultMap  = new HashMap<>();

    public static void main(String[] args) throws IOException {
        reasonMap = new HashMap<>();
        reasonMap.put("0", "The affected statement has accurate code differences.");
        reasonMap.put("1", "Source statement should be deleted and destination statement should be added");
        reasonMap.put("2", "There is a better match for the source or destination statement.");
        reasonMap.put("3", "The source or destination statement should be matched to a statement in another file.");
        reasonMap.put("4", "Inaccurate code differences in a statement");
        reasonMap.put("7", "Suboptimal matching between program elements from the source and destination statement.");
        reasonMap.put("8", "Meaningless moveTokenSeq actions between different affected statements");
        reasonMap.put("9", "Suboptimal moveTokenSeq actions between different affected statements.");
        reasonMap.put("10", "The matched source or destination statement is missed.");
        reasonMap.put("11", "The statement is considered to be moved but it is actually not moved.");

        CsvData data = CsvLoader.loadCsv(originPath);

        for (String reason: reasonMap.keySet()){
            reasonResultMap.put(reason, new HashMap<>());
        }

        List<CsvRecord> recordList = data.records;
        int index = 2;
        for (CsvRecord record: recordList){
            String metric = record.getValueOfCol(METRIC);
            String mapMethod = record.getValueOfCol(MAP_METHOD);
            String reason = record.getValueOfCol(REASON);
            if (reason.equals("4"))
                reason = record.getAdditionalEntries().get(0);
            String metricMethod = metric + "-" + mapMethod;

            System.out.println(index);
            index ++;

            if (!reasonResultMap.get(reason).containsKey(metricMethod))
                reasonResultMap.get(reason).put(metricMethod, 0);

            int curVal = reasonResultMap.get(reason).get(metricMethod);
            reasonResultMap.get(reason).put(metricMethod, curVal + 1);
        }

        System.out.println(reasonResultMap.keySet());

        String[] reasons = {
                "1", "2", "3", "10", "7", "8", "9", "11", "0"
        };
        CsvData outData = new CsvData();
        outData.headers = getHeaders();
        outData.records = new ArrayList<>();
        for (String reason: reasons){
            System.out.println(reason);
            System.out.println(reasonResultMap.get(reason));
            List<String> recordList2 = new ArrayList<>();
            recordList2.add(reasonMap.get(reason));
            String[] tmpHeaders = {
                    "CEC-gt", "CEC-mtdiff", "CEC-ijm", "SDC-gt", "SDC-mtdiff", "SDC-ijm",
                    "MMTC-gt", "MMTC-mtdiff", "MMTC-ijm"
            };

            for (String header: tmpHeaders){
                Integer val = reasonResultMap.get(reason).get(header);
                if (val == null)
                    val = 0;
                String valStr = Integer.toString(val);
                recordList2.add(valStr);
            }

            CsvRecord record = new CsvRecord();
            record.setRecords(recordList2, getHeaders());
            outData.records.add(record);
        }

        CsvWriter.writeCsv(outData, manualAnalysisResultPath);
    }

    public static String[] getHeaders(){
        String[] headers = {
                "Reason", "CEC-gt", "CEC-mtdiff", "CEC-ijm", "SDC-gt", "SDC-mtdiff", "SDC-ijm",
                "MMTC-gt", "MMTC-mtdiff", "MMTC-ijm"
        };
        return headers;
    }
}
