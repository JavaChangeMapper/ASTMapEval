package cs.zju.eva.editfw.metrics.accuracy;

import cs.zju.eva.editfw.FileRevisionMeasures;
import cs.zju.eva.editfw.StmtEvaluationMeasures;
import cs.zju.eva.utils.FileRevision;
import cs.zju.eva.statistics.CsvDataExtractor;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatisticAnalysis {
    private static String[] projects = {
            "activemq"
    };

    private static String[] matcherMethods = {"gt", "mtdiff", "ijm"};

    private static final String COMMIT_ID = "commitId";
    private static final String FILE_PATH = "srcFilePath";
    private static final String STMT_TYPE = "stmtType";
    private static final String STMT_CHANGE_TYPE = "changeType";
    private static final String MEANINGLESS_MOVE_COUNT = "lowQualityMapCount";
    private static final String CEC = "CEC";

    public static void main(String[] args) throws Exception{
        List<String[]> recordsCEC = new ArrayList<>();

        for (String project: projects){
            System.out.println(project);
            String csvPath = PathResolver.getCombineResultCsvPath(project, "gt");
            List<String[]> fileRecords = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders()).getRecords();
            double numOfFile = fileRecords.size();
            List<String> gtRecordsCEC = getCECRecordForMatcherMethod(project, "gt", numOfFile);
            List<String> mtdRecordsCEC = getCECRecordForMatcherMethod(project, "mtdiff", numOfFile);
            List<String> ijmRecordsCEC = getCECRecordForMatcherMethod(project, "ijm", numOfFile);

            List<String> record = new ArrayList<>();
            record.add(project);
            record.addAll(gtRecordsCEC);
            record.addAll(mtdRecordsCEC);
            record.addAll(ijmRecordsCEC);

            recordsCEC.add(record.toArray(new String[recordsCEC.size()]));
        }

        String cecPath = PathResolver.getAccuracyAnalysisCECCsvPath();
        CsvOperationsUtil.writeCSV(cecPath, getHeadersCEC(), recordsCEC);


        List<String[]> recordsSDC = new ArrayList<>();

        for (String project: projects){
            System.out.println(project);
            String csvPath = PathResolver.getCombineResultCsvPath(project, "gt");
            List<String[]> fileRecords = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders()).getRecords();
            double numOfFile = fileRecords.size();
            List<String> gtRecordsSDC = getSDCRecordForMatcherMethod(project, "gt", numOfFile);
            List<String> mtdRecordsSDC = getSDCRecordForMatcherMethod(project, "mtdiff", numOfFile);
            List<String> ijmRecordsSDC = getSDCRecordForMatcherMethod(project, "ijm", numOfFile);

            List<String> record = new ArrayList<>();
            record.add(project);
            record.addAll(gtRecordsSDC);
            record.addAll(mtdRecordsSDC);
            record.addAll(ijmRecordsSDC);

            recordsSDC.add(record.toArray(new String[recordsCEC.size()]));
        }

        String sdcPath = PathResolver.getAccuracyAnalysisSDCCsvPath();
        CsvOperationsUtil.writeCSV(sdcPath, getHeadersSDC(), recordsSDC);

        List<String[]> recordsMMTC = new ArrayList<>();

        for (String project: projects){
            System.out.println(project);
            List<String> gtRecordsMMTC = getMMTCRecordForMatcherMethod(project, "gt");
            List<String> mtdRecordsMMTC = getMMTCRecordForMatcherMethod(project, "mtdiff");
            List<String> ijmRecordsMMTC = getMMTCRecordForMatcherMethod(project, "ijm");

            List<String> record = new ArrayList<>();
            record.add(project);
            record.addAll(gtRecordsMMTC);
            record.addAll(mtdRecordsMMTC);
            record.addAll(ijmRecordsMMTC);

            recordsMMTC.add(record.toArray(new String[recordsCEC.size()]));
        }

        String mmtcPath = PathResolver.getAccuracyAnalysisMMTCCsvPath();
        CsvOperationsUtil.writeCSV(mmtcPath, getHeadersMMTC(), recordsMMTC);

        List<String[]> allRecords = new ArrayList<>();

        for (String project: projects){
            System.out.println(project);
            String csvPath = PathResolver.getCombineResultCsvPath(project, "gt");
            List<String[]> fileRecords = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders()).getRecords();
            double numOfFile = fileRecords.size();
            List<String> gtRecords = getAllSuspiciousRecordForMatcherMethod(project, "gt", numOfFile);
            List<String> mtdRecords = getAllSuspiciousRecordForMatcherMethod(project, "mtdiff", numOfFile);
            List<String> ijmRecords = getAllSuspiciousRecordForMatcherMethod(project, "ijm", numOfFile);

            List<String> record = new ArrayList<>();
            record.add(project);
            record.addAll(gtRecords);
            record.addAll(mtdRecords);
            record.addAll(ijmRecords);

            allRecords.add(record.toArray(new String[recordsCEC.size()]));
        }

        String csvPath = PathResolver.getAccuracyAnalysisCsvPath();
        CsvOperationsUtil.writeCSV(csvPath, getHeaders(), allRecords);
    }

    private static List<String> getAllSuspiciousRecordForMatcherMethod(String project, String matcherMethod, double numOfFileRecords) throws Exception {

        List<String[]> suspiciousStmtRecords = getSuspiciousStmtRecords(project, matcherMethod);
        List<String[]> changedStmtRecords = getChangedStmtRecord(project, matcherMethod, false);
        double tmp1 = changedStmtRecords.size();

        CsvDataExtractor extractor = new CsvDataExtractor(suspiciousStmtRecords, StmtRecord.getHeaders());
        Set<FileRevision> affectedFrs = new HashSet<>();
        for (String[] record: suspiciousStmtRecords){
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            FileRevision fr = new FileRevision(commitId, filePath);
            affectedFrs.add(fr);
        }

        List<String> ret = new ArrayList<>();
        ret.add(Integer.toString(suspiciousStmtRecords.size()));
        ret.add(Double.toString(suspiciousStmtRecords.size() / tmp1));
        ret.add(Integer.toString(affectedFrs.size()));
        ret.add(Double.toString((double) affectedFrs.size() / numOfFileRecords));

        return ret;
    }

    private static List<String> getCECRecordForMatcherMethod(String project, String matcherMethod,
                                                             double numOfFileRecords) throws Exception {
        List<String[]> testRecordsByCEC = getSuspiciousStmtRecordsByCEC(project, matcherMethod);
        List<String[]> changedStmtRecords = getChangedStmtRecord(project, matcherMethod, false);
        double tmp1 = changedStmtRecords.size();

        CsvDataExtractor extractor = new CsvDataExtractor(testRecordsByCEC, StmtRecord.getHeaders());
        int count = 0;
        double sum = 0.0;
        Set<FileRevision> testFileRevisions = new HashSet<>();
        Set<FileRevision> affectedFrs = new HashSet<>();
        for (String[] record: testRecordsByCEC){
            double mismatchRatio = extractor.getColDoubleVal(record, CEC);
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            testFileRevisions.add(new FileRevision(commitId, filePath));
            sum += mismatchRatio;
            if (mismatchRatio == 0.0) {
                FileRevision fr = new FileRevision(commitId, filePath);
                affectedFrs.add(fr);
                count++;
            }
        }

        List<String> ret = new ArrayList<>();
        ret.add(Integer.toString(testRecordsByCEC.size()));
        ret.add(Double.toString(testRecordsByCEC.size() / tmp1));
        ret.add(Integer.toString(testFileRevisions.size()));
        ret.add(Double.toString((double)testFileRevisions.size() / numOfFileRecords));
        ret.add(Double.toString(sum / testRecordsByCEC.size()));
        ret.add(Integer.toString(count));
        ret.add(Double.toString((double)count / testRecordsByCEC.size()));
        ret.add(Integer.toString(affectedFrs.size()));
        ret.add(Double.toString((double) affectedFrs.size() / testFileRevisions.size()));

        return ret;
    }

    private static List<String> getSDCRecordForMatcherMethod(String project, String matcherMethod, double numOfFileRecords) throws Exception {

        List<String[]> suspiciousStmtRecordsBySDC = getSuspiciousStmtRecordsBySDC(project, matcherMethod);
        List<String[]> changedStmtRecords = getChangedStmtRecord(project, matcherMethod, false);
        double tmp1 = changedStmtRecords.size();

        CsvDataExtractor extractor = new CsvDataExtractor(suspiciousStmtRecordsBySDC, StmtRecord.getHeaders());
        Set<FileRevision> affectedFrs = new HashSet<>();
        for (String[] record: suspiciousStmtRecordsBySDC){
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            FileRevision fr = new FileRevision(commitId, filePath);
            affectedFrs.add(fr);
        }

        List<String> ret = new ArrayList<>();
        ret.add(Integer.toString(suspiciousStmtRecordsBySDC.size()));
        ret.add(Double.toString(suspiciousStmtRecordsBySDC.size() / tmp1));
        ret.add(Integer.toString(affectedFrs.size()));
        ret.add(Double.toString((double) affectedFrs.size() / numOfFileRecords));

        return ret;
    }


    private static List<String> getMMTCRecordForMatcherMethod(String project, String matcherMethod) throws Exception {
        String csvPath = PathResolver.getCombineResultCsvPath(project, matcherMethod);
        CsvDataExtractor fileRecordExtractor = new CsvDataExtractor(csvPath, FileRevisionMeasures.getHeaders());
        List<String[]> fileRecords = fileRecordExtractor.getRecords();
        int mmtcCount = 0;
        for (String[] record: fileRecords){
            mmtcCount += (int)fileRecordExtractor.getColDoubleVal(record, MEANINGLESS_MOVE_COUNT);
        }

        List<String[]> suspiciousStmtRecordsByMMTC = getSuspiciousStmtRecordsByMMTC(project, matcherMethod);
        List<String[]> changedStmtRecords = getChangedStmtRecord(project, matcherMethod, false);
        double tmp1 = changedStmtRecords.size();

        CsvDataExtractor extractor = new CsvDataExtractor(suspiciousStmtRecordsByMMTC, StmtRecord.getHeaders());
        Set<FileRevision> affectedFrs = new HashSet<>();
        for (String[] record: suspiciousStmtRecordsByMMTC){
            String commitId = extractor.getColVal(record, COMMIT_ID);
            String filePath = extractor.getColVal(record, FILE_PATH);
            FileRevision fr = new FileRevision(commitId, filePath);
            affectedFrs.add(fr);
        }

        List<String> ret = new ArrayList<>();
        ret.add(Integer.toString(mmtcCount));
        ret.add(Integer.toString(suspiciousStmtRecordsByMMTC.size()));
        ret.add(Double.toString(suspiciousStmtRecordsByMMTC.size() / tmp1));
        ret.add(Integer.toString(affectedFrs.size()));
        ret.add(Double.toString((double) affectedFrs.size() / fileRecords.size()));

        return ret;
    }


    private static List<String[]> getSuspiciousStmtRecords(String project, String matcherMethod) throws Exception {
        String filePath = PathResolver.getAccuracyAnalysisCsvPath(project, matcherMethod, "ALL");
        CsvDataExtractor extractor = new CsvDataExtractor(filePath, StmtRecord.getHeaders());
        return extractor.getRecords();
    }

    private static List<String[]> getSuspiciousStmtRecordsByCEC(String project, String matcherMethod) throws Exception{
        String filePath = PathResolver.getAccuracyAnalysisCsvPath(project, matcherMethod, "CEC");
        CsvDataExtractor extractor = new CsvDataExtractor(filePath, StmtRecord.getHeaders());
        return extractor.getRecords();
    }

    private static List<String[]> getSuspiciousStmtRecordsBySDC(String project, String matcherMethod) throws Exception {
        String filePath = PathResolver.getAccuracyAnalysisCsvPath(project, matcherMethod, "SDC");
        CsvDataExtractor extractor = new CsvDataExtractor(filePath, StmtRecord.getHeaders());
        return extractor.getRecords();
    }

    private static List<String[]> getSuspiciousStmtRecordsByMMTC(String project, String matcherMethod) throws Exception {
        String filePath = PathResolver.getAccuracyAnalysisCsvPath(project, matcherMethod, "MMTC");
        CsvDataExtractor extractor = new CsvDataExtractor(filePath, StmtRecord.getHeaders());
        return extractor.getRecords();
    }

    private static List<String[]> getChangedStmtRecord(String project, String matcherMethod, boolean considerBlock) throws Exception{
        String filePath = PathResolver.getCombineStmtResultCsvPath(project, matcherMethod);
        CsvDataExtractor extractor = new CsvDataExtractor(filePath, StmtEvaluationMeasures.getHeaders());
        List<String[]> ret = new ArrayList<>();
        for (String[] record: extractor.getRecords()){
            String changeType = extractor.getColVal(record, STMT_CHANGE_TYPE);
            if (changeType.equals(""))
                continue;
            String StmtType = extractor.getColVal(record, STMT_TYPE);
            if (StmtType.equals("Block") && !considerBlock)
                continue;
            ret.add(record);
        }
        return ret;
    }

    private static String[] getHeaders(){
        String[] headers = {
                "project", "gt-bad-Stmt", "gt-Stmt-ratio", "gt-bad-fr", "gt-fr-ratio",
                "mtd-bad-Stmt", "mtd-Stmt-ratio", "mtd-bad-fr", "mtd-fr-ratio",
                "ijm-bad-Stmt", "ijm-Stmt-ratio", "ijm-bad-fr", "ijm-fr-ratio"
        };
        return headers;
    }

    private static String[] getHeadersCEC(){
        String[] headers = {
                "project", "gt-Stmt-num", "gt-Stmt-ratio",  "gt-fr", "gt-fr-ratio", "gt-avg-cec", "gt-zero-cec-Stmt", "gt-zero-cec-ratio",  "gt-zero-cec-fr", "gt-zero-fr-ratio",
                "mtd-Stmt-num", "mtd-Stmt-ratio", "mtd-fr", "mtd-fr-ratio", "mtd-avg-cec", "mtd-zero-cec-Stmt", "mtd-zero-cec-ratio", "mtd-zero-cec-fr", "mtd-zero-fr-ratio",
                "ijm-Stmt-num", "ijm-Stmt-ratio", "ijm-fr", "ijm-fr-ratio", "ijm-avg-cec", "ijm-zero-cec-Stmt", "ijm-zero-cec-ratio", "ijm-zero-cec-fr", "ijm-zero-fr-ratio"
        };
        return headers;
    }

    private static String[] getHeadersSDC(){
        String[] headers = {
                "project", "gt-Stmt-num", "gt-Stmt-ratio", "gt-fr", "gt-fr-ratio",
                "mtd-Stmt-num", "mtd-Stmt-ratio", "mtd-fr", "mtd-fr-ratio",
                "ijm-Stmt-num", "ijm-Stmt-ratio", "ijm-fr", "ijm-fr-ratio"
        };
        return headers;
    }

    private static String[] getHeadersMMTC(){
        String[] headers = {
                "project", "gt-action-num", "gt-Stmt-num", "gt-Stmt-ratio", "gt-fr", "gr-fr-ratio",
                "mtd-action-num", "mtd-Stmt-num", "mtd-Stmt-ratio", "mtd-fr", "mtd-fr-ratio",
                "ijm-action-num", "ijm-Stmt-num", "ijm-Stmt-ratio", "ijm-fr", "ijm-fr-ratio"
        };
        return headers;
    }
}
