package cs.zju.eva.editfw.metrics.correctness;

import cs.zju.eva.editfw.FileRevisionMeasures;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.eva.statistics.StatisticCalculator;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

public class AmbiguityAnalysis {
    private static String[] projects = {
            "activemq"
    };

    // "commons-io", "commons-lang","commons-math",
    //            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"

    private static final String SRC2DST_AMBIGUITY = "srcToDstAmbiguity";
    private static final String DST2SRC_AMBIGUITY = "dstToSrcAmbiguity";
    private static final String AMB_ACTION = "numOfAmbiguousAction";
    private static final String UNAMB_ACTION = "numOfNonAmbiguousCount";

    public static void main(String[] args) throws Exception{
        String[] headers = getHeaders();
        List<String[]> records = new ArrayList<>();
        for (String project: projects){
            System.out.println(project);
            List<String> record = new ArrayList<>();
            List<String> gtRecord = getAnalysisResultsForMapMethod(project, "gt");
            List<String> mtdRecord = getAnalysisResultsForMapMethod(project, "mtdiff");
            List<String> ijmRecord = getAnalysisResultsForMapMethod(project, "ijm");

            record.add(project);
            record.addAll(gtRecord);
            record.addAll(mtdRecord);
            record.addAll(ijmRecord);

            records.add(record.toArray(new String[record.size()]));
        }

        String path = PathResolver.getAmbiguityAnalysisCsvPath();
        CsvOperationsUtil.writeCSV(path, headers, records);
    }

    private static List<String> getAnalysisResultsForMapMethod(String project, String mapMethod) throws Exception {
        String csvPath = PathResolver.getCombineResultCsvPath(project, mapMethod);
        CsvDataExtractor extractor = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders());
        RealVector srcToDst = extractor.getColValuesVector(SRC2DST_AMBIGUITY);
        RealVector dstToSrc = extractor.getColValuesVector(DST2SRC_AMBIGUITY);
        RealVector ambiguousActionVector = extractor.getColValuesVector(AMB_ACTION);
        RealVector unambiguousActionVector = extractor.getColValuesVector(UNAMB_ACTION);
        List<Integer> srcToDstNonZeroIdxes = StatisticCalculator.nonZeroIndexes(srcToDst);
        List<Integer> dstToSrcNonZeroIdxes = StatisticCalculator.nonZeroIndexes(dstToSrc);

        System.out.println(srcToDstNonZeroIdxes.size() / (double)srcToDst.getDimension());
        List<String> record = new ArrayList<>();
        RealVector subtractionVector = StatisticCalculator.vectorSubtraction(ambiguousActionVector, unambiguousActionVector);
        List<Integer> nonZeroIdxes = StatisticCalculator.nonZeroIndexes(subtractionVector);
        record.add(Integer.toString(srcToDstNonZeroIdxes.size()));
        record.add(Integer.toString(dstToSrcNonZeroIdxes.size()));
        if (mapMethod.equals("gt") || mapMethod.equals("mtdiff")){
            record.add(Integer.toString(nonZeroIdxes.size()));
            double sum = StatisticCalculator.sumOfRealVector(subtractionVector);
            record.add(Double.toString(sum / nonZeroIdxes.size()));
        }
        return record;
    }

    private static String[] getHeaders(){
        String[] headers = {"Projects", "GT-SRC", "GT-DST", "GT-diff-action",  "GT-diff", "MTD-SRC", "MTD-DST",
                "MTD-diff-action", "MTD-diff",
                "IJM-SRC", "IJM-DST"};
        return headers;
    }
}
