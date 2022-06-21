package cs.zju.utils;

import cs.zju.config.MyConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class PathResolver {

    private final static String rootPath = MyConfig.getRootPath();

    public static String projectFolder(String projectName){
        return new File(new File(rootPath), "projects/" + projectName).getAbsolutePath();
    }

    public static String commitCheckoutFolder(String projectName, String commitId){
        String commitFolder = "commit-checkout/tmp-" + commitId;
        return new File(new File(projectFolder(projectName)), commitFolder).getAbsolutePath();
    }

    public static String getResultCsv(String project, String mapMethod, int csvId){
        File resultFolder = new File(new File(rootPath), "eva-result");
        File projectDir = new File(resultFolder, project);
        if (!projectDir.exists())
            projectDir.mkdirs();
        File csvFile = new File(resultFolder, project + "/" + mapMethod + "-" + csvId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtResultCsv(String project, String mapMethod, int csvId){
        File resultFolder = new File(new File(rootPath), "eva-stmt-result");
        File projectDir = new File(resultFolder, project);
        if (!projectDir.exists())
            projectDir.mkdirs();
        File csvFile = new File(resultFolder, project + "/" + mapMethod + "-" + csvId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getCombineResultCsvPath(String project, String mapMethod){
        File resultFolder = new File(new File(rootPath), "eva-result");
        File csvFile = new File(resultFolder, project + "-" + mapMethod + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getCombineStmtResultCsvPath(String project, String mapMethod){
        File resultFolder = new File(new File(rootPath), "eva-stmt-result");
        File csvFile = new File(resultFolder, project + "-" + mapMethod + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAmbiguityAnalysisCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "ambiguity.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getUnderstandLoadAnalysisCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "understand.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAccuracyAnalysisCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "accuracy.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAccuracyAnalysisCsvPath(String project, String mapMethod, String metric){
        File resultFolder = new File(new File(rootPath), "analysis/" + metric + "/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, project + "-suspicious-" + mapMethod + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAccuracyAnalysisCECCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "CEC-accuracy.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAccuracyAnalysisSDCCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "SDC-accuracy.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getAccuracyAnalysisMMTCCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder,"MMTC-accuracy.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getManualAnalysisScriptPath(String metric){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder,metric + ".txt");
        return csvFile.getAbsolutePath();
    }

    public static String getManualAnalysisCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "manual.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getManualAnalysisResultCsvPath(){
        File resultFolder = new File(new File(rootPath), "analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "manual-result.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchDiffResultCsvPath(String project, String algorithm1, String algorithm2, int idx){
        File resultFolder = new File(new File(rootPath), "match-analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project + "/groups/");
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, algorithm1 + "-" + algorithm2 + "-" + idx + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchDiffRecordCsvPath(String project, int idx){
        File resultFolder = new File(new File(rootPath), "match-analysis/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project + "/groups/");
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "record-" + idx + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchDiffRecordBackupCsvPath(String project, int idx){
        File resultFolder = new File(new File(rootPath), "run-record/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "record" + idx + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchManualAnalysisCsvPath(String project, String method1, String method2){
        File resultFolder = new File(new File(rootPath), "manual/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, method1 + "-" + method2 + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchManualAnalysisScriptPath(String project, String method1, String method2){
        File resultFolder = new File(new File(rootPath), "manual/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, method1 + "-" + method2 + ".txt");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchManualResultCsvPath(String project, String method1, String method2){
        File resultFolder = new File(new File(rootPath), "manual-results/");
        File projectFolder = new File(resultFolder, project);
        File csvFile = new File(projectFolder, method1 + "-" + method2 + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMatchResultCsvPath(String project){
        File resultFolder = new File(new File(rootPath), "all-results/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, project + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getUserStudyScriptPath(int revId){
        File resultFolder = new File(new File(rootPath), "user-manual/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File scriptFile = new File(resultFolder, "" + revId + ".md");
        return scriptFile.getAbsolutePath();
    }

    public static String getUserStudyCsvPath(int revId){
        File resultFolder = new File(new File(rootPath), "user-manual/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "" + revId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getUserStudyInfoCsvPath(){
        File resultFolder = new File(new File(rootPath), "user-manual/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File csvFile = new File(resultFolder, "info.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getUserStudyResultDirPath(){
        File resultFolder = new File(new File(rootPath), "human-analysis");
        return resultFolder.getAbsolutePath();
    }

    public static String getUserStudyRunningPath(String project, String algorithm1, String algorithm2){
        File resultFolder = new File(new File(rootPath), "match-results/");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, algorithm1 + "-" + algorithm2 + "-1.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getUserStudyAnalysisCsvPath(){
        File csvFile = new File(new File(rootPath), "user-manual-analysis.csv");
        return csvFile.getAbsolutePath();
    }
}
