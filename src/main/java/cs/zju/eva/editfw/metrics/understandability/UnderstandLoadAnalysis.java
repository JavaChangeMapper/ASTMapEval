package cs.zju.eva.editfw.metrics.understandability;

import cs.zju.eva.editfw.FileRevisionMeasures;
import cs.zju.eva.utils.FileRevision;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.eva.statistics.StatisticCalculator;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnderstandLoadAnalysis {
    private static String[] projects = {
            "activemq"
    };


    private static final String COMMIT_ID = "commitId";
    private static final String FILE_PATH = "srcFilePath";
    private static final String EDIT_SCRIPT_SIZE = "numOfNonAmbiguousCount";
    private static final String CHANGED_STMTS = "numOfModifiedStmts";
    private static final String TOKEN_EDIT_COST = "numOfEdits";
    private static final String CHANGED_STMTS_NO_BLOCK = "numOfModifiedStmtsWithoutBlock";
    private static final String ESS = "numOfNonAmbiguousCount";

    public static void main(String[] args) throws Exception{
        String[] headers = getHeaders();
        List<String[]> records = new ArrayList<>();
        for (String project: projects){
            List<String> record = new ArrayList<>();
            List<String> gtRecord = getAnalysisResultsForMapMethod(project, "gt", false);
            List<String> mtdRecord = getAnalysisResultsForMapMethod(project, "mtdiff", false);
            List<String> ijmRecord = getAnalysisResultsForMapMethod(project, "ijm", false);

            List<String> betterStmtRecord = getCompareRatio(project, false, CHANGED_STMTS_NO_BLOCK);
            List<String> betterEditRecord = getCompareRatio(project, false, TOKEN_EDIT_COST);
            List<String> betterEssRecord = getCompareRatio(project, false, ESS);

            record.add(project);
            record.addAll(gtRecord);
            record.addAll(mtdRecord);
            record.addAll(ijmRecord);
            record.addAll(betterStmtRecord);
            record.addAll(betterEditRecord);
            record.addAll(betterEssRecord);

            records.add(record.toArray(new String[record.size()]));
        }

        String path = PathResolver.getUnderstandLoadAnalysisCsvPath();
        CsvOperationsUtil.writeCSV(path, headers, records);
    }

    public static List<String> getCompareRatio(String project, boolean considerBlock, String colName) throws Exception {
        String gtResultCsvPath = PathResolver.getCombineResultCsvPath(project, "gt");
        String mtdResultCsvPath = PathResolver.getCombineResultCsvPath(project, "mtdiff");
        String ijmResultCsvPath = PathResolver.getCombineResultCsvPath(project, "ijm");

        CsvDataExtractor gtExtractor = new CsvDataExtractor(gtResultCsvPath, FileRevisionMeasures.getHeaders());
        CsvDataExtractor mtdExtractor = new CsvDataExtractor(mtdResultCsvPath, FileRevisionMeasures.getHeaders());
        CsvDataExtractor ijmExtractor = new CsvDataExtractor(ijmResultCsvPath, FileRevisionMeasures.getHeaders());

        List<String[]> gtRecords = gtExtractor.getRecords();
        List<String[]> mtdRecords = mtdExtractor.getRecords();
        List<String[]> ijmRecords = ijmExtractor.getRecords();


        Map<FileRevision, Integer> gtAffectedStmtMap = getRevisionValueMap(gtRecords, gtExtractor, colName);
        Map<FileRevision, Integer> mtdAffectedStmtMap = getRevisionValueMap(mtdRecords, mtdExtractor, colName);
        Map<FileRevision, Integer> ijmAffectedStmtMap = getRevisionValueMap(ijmRecords, ijmExtractor, colName);

        double size = gtAffectedStmtMap.size();
        int gtBetterThanMTD = 0;
        int mtdBetterThanGt = 0;
        int gtBetterThanIJM = 0;
        int ijmBetterThanGt = 0;
        int mtdBetterThanIJM = 0;
        int ijmBetterThanMtd = 0;
        for (FileRevision fr: gtAffectedStmtMap.keySet()){
            Integer gtStmts = gtAffectedStmtMap.get(fr);
            Integer mtdStmts = mtdAffectedStmtMap.get(fr);
            Integer ijmStmts = ijmAffectedStmtMap.get(fr);
            if (gtStmts != null && mtdStmts != null && ijmStmts != null){
                if (gtStmts < mtdStmts)
                    gtBetterThanMTD += 1;
                if (mtdStmts < gtStmts)
                    mtdBetterThanGt += 1;
                if (gtStmts < ijmStmts)
                    gtBetterThanIJM += 1;
                if (ijmStmts < gtStmts)
                    ijmBetterThanGt += 1;
                if (mtdStmts < ijmStmts)
                    mtdBetterThanIJM += 1;
                if (ijmStmts < mtdStmts)
                    ijmBetterThanMtd += 1;
            }
        }
        List<String> ret = new ArrayList<>();
        ret.add(Double.toString(gtBetterThanMTD / size));
        ret.add(Double.toString(mtdBetterThanGt / size));
        ret.add(Double.toString(gtBetterThanIJM / size));
        ret.add(Double.toString(ijmBetterThanGt / size));
        ret.add(Double.toString(mtdBetterThanIJM / size));
        ret.add(Double.toString(ijmBetterThanMtd / size));
        return ret;
    }

    private static Map<FileRevision, Integer> getRevisionValueMap(List<String[]> records,
                                                                  CsvDataExtractor extractor, String colName){
        Map<FileRevision, Integer> ret = new HashMap<>();
        for (String[] record: records){
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            FileRevision fr = new FileRevision(commitId, filePath);
            int value = Integer.parseInt(extractor.getColVal(record, colName));
            ret.put(fr, value);
        }
        return ret;
    }

    public static List<String> getAnalysisResultsForMapMethod(String project, String mapMethod,
                                                              boolean considerBlock) throws Exception{
        String resultCsvPath = PathResolver.getCombineResultCsvPath(project, mapMethod);
        List<String> record = new ArrayList<>();
        CsvDataExtractor extractor = new CsvDataExtractor(resultCsvPath, FileRevisionMeasures.getHeaders());
        RealVector numOfStmts;
        RealVector numOfEdits = extractor.getColValuesVector(TOKEN_EDIT_COST);

        if (considerBlock) {
            numOfStmts = extractor.getColValuesVector(CHANGED_STMTS);
        } else {
            numOfStmts = extractor.getColValuesVector(CHANGED_STMTS_NO_BLOCK);
        }

        RealVector editScriptSize = extractor.getColValuesVector(EDIT_SCRIPT_SIZE);
        double avgStmts = StatisticCalculator.sumOfRealVector(numOfStmts) / numOfStmts.getDimension();
        double avgEdits = StatisticCalculator.sumOfRealVector(numOfEdits) / numOfEdits.getDimension();
        double avgEs = StatisticCalculator.sumOfRealVector(editScriptSize) / editScriptSize.getDimension();
        record.add(Double.toString(avgStmts));
        record.add(Double.toString(avgEdits));
        record.add(Double.toString(avgEs));
        return record;
    }

    private static String[] getHeaders(){
        String[] headers  = {
                "projects", "gt-avg-stmt", "gt-avg-edit", "gt-es",
                "mtd-avg-stmt", "mtd-avg-edit", "mtd-es",
                "ijm-avg-stmt", "ijm-avg-edit", "ijm-es",
                "gt-mtd-stmt", "mtd-gt-stmt", "gt-ijm-stmt", "ijm-gt-stmt", "mtd-ijm-stmt", "ijm-mtd-stmt",
                "gt-mtd-token", "mtd-gt-token", "gt-ijm-token", "ijm-gt-token", "mtd-ijm-token", "ijm-mtd-token",
                "gt-mtd-ess", "mtd-gt-ess", "gt-ijm-ess", "ijm-gt-ess", "mtd-ijm-ess", "ijm-mtd-ess"
        };
        return headers;
    }
}
