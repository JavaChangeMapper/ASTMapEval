package cs.zju.eva.editfw.metrics.accuracy;

import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.util.*;

public class Analysis {
    private static String[] projects = {
                "activemq"
    };

    public static void main(String[] args) throws Exception {
        for (String project: projects){
            System.out.println(project);
            ProjectStmtResultMap gtStmtMap = new ProjectStmtResultMap(project, "gt");
            ProjectStmtResultMap mtdStmtMap = new ProjectStmtResultMap(project, "mtdiff");
            ProjectStmtResultMap ijmStmtMap = new ProjectStmtResultMap(project, "ijm");
            double gtRatio = gtStmtMap.getLowerMADRatio();
            double mtdRatio = mtdStmtMap.getLowerMADRatio();
            double ijmRatio = ijmStmtMap.getLowerMADRatio();
            doCompare(gtStmtMap, mtdStmtMap, ijmStmtMap);
            doCompare(mtdStmtMap, gtStmtMap, ijmStmtMap);
            doCompare(ijmStmtMap, gtStmtMap, mtdStmtMap);
            writeCECCsv("gt", project, gtStmtMap, gtRatio);
            writeCECCsv("mtdiff", project, mtdStmtMap, mtdRatio);
            writeCECCsv("ijm", project, ijmStmtMap, ijmRatio);

            writeSDCCsv("gt", project, gtStmtMap, gtRatio);
            writeSDCCsv("mtdiff", project, mtdStmtMap, mtdRatio);
            writeSDCCsv("ijm", project, ijmStmtMap, ijmRatio);

            writeMMTCCsv("gt", project, gtStmtMap, gtRatio);
            writeMMTCCsv("mtdiff", project, mtdStmtMap, mtdRatio);
            writeMMTCCsv("ijm", project, ijmStmtMap, ijmRatio);

            writeAllSuspiciousCSV("gt", project, gtStmtMap, gtRatio);
            writeAllSuspiciousCSV("mtdiff", project, mtdStmtMap, mtdRatio);
            writeAllSuspiciousCSV("ijm", project, ijmStmtMap, ijmRatio);
        }
    }

    private static void writeAllSuspiciousCSV(String mapMethod, String project, ProjectStmtResultMap map, double lowerMADRatio) throws Exception{
        List<StmtRecord> stmts = map.getSuspiciousStmts();
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "ALL");
        List<String[]> records = getRecordsFromContexts(stmts, lowerMADRatio);
        CsvOperationsUtil.writeCSV(csvPath, StmtRecord.getHeaders(), records);
    }

    private static void writeCECCsv(String mapMethod, String project, ProjectStmtResultMap map, double lowerMADRatio) throws Exception{
        List<StmtRecord> stmts = map.getStmtsTestedByCEC();
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "CEC");
        List<String[]> records = getRecordsFromContexts(stmts, lowerMADRatio);
        CsvOperationsUtil.writeCSV(csvPath, StmtRecord.getHeaders(), records);
    }

    private static void writeSDCCsv(String mapMethod, String project, ProjectStmtResultMap map, double lowerMADRatio) throws Exception{
        List<StmtRecord> stmts = map.getSuspiciousStmtsBySDC();
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "SDC");
        List<String[]> records = getRecordsFromContexts(stmts, lowerMADRatio);
        CsvOperationsUtil.writeCSV(csvPath, StmtRecord.getHeaders(), records);
    }

    private static void writeMMTCCsv(String mapMethod, String project, ProjectStmtResultMap map, double lowerMADRatio) throws Exception {
        List<StmtRecord> stmts = map.getSuspiciousStmtsByMMTC();
        String csvPath = PathResolver.getAccuracyAnalysisCsvPath(project, mapMethod, "MMTC");
        List<String[]> records = getRecordsFromContexts(stmts, lowerMADRatio);
        CsvOperationsUtil.writeCSV(csvPath, StmtRecord.getHeaders(), records);
    }

    private static List<String[]> getRecordsFromContexts(List<StmtRecord> stmtRecords, double lowerMADRatio){
        List<String[]> records = new ArrayList<>();
        for (StmtRecord stmt: stmtRecords){
            records.add(stmt.getRecord(lowerMADRatio));
        }
        return records;
    }

    public static void doCompare(ProjectStmtResultMap stmtMap, ProjectStmtResultMap stmtMap1,
                                 ProjectStmtResultMap stmtMap2){
        Map<FileRevision, Set<StmtRecord>> ret = new HashMap<>();
        stmtMap.doCompare(stmtMap1);
        stmtMap.doCompare(stmtMap2);
    }
}
