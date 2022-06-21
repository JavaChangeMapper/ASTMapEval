package cs.zju.eva.matchfw.result.userstudy;

import cs.zju.eva.matchfw.result.StmtMapEvalRecord;
import cs.zju.eva.matchfw.result.StmtObj;
import cs.zju.eva.utils.FileRevision;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ResultAnalysis3 {
    private static String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static Map<StmtObj, StmtMapEvalRecord> stmtApproachResultMap = new HashMap<>();
    private static Map<StmtObj, List<Integer>> gtStmtScores = new HashMap<>();
    private static Map<StmtObj, List<Integer>> mtdStmtScores = new HashMap<>();
    private static Map<StmtObj, List<Integer>> ijmStmtScores = new HashMap<>();

    private static Map<StmtObj, Integer> stmtUserStudyId = new HashMap<>();

    public static void main(String[] args) throws Exception {
        getApproachDeterminationScores();
        getHumanDeterminationScores();

        Set<StmtObj> allStmts = new HashSet<>(gtStmtScores.keySet());

        int gtInaccurateNum = 0;
        int gtTP = 0;
        int gtFP = 0;
        int gtFN = 0;
        int mtdInaccurateNum = 0;
        int mtdTP = 0;
        int mtdFP = 0;
        int mtdFN = 0;
        int ijmInaccurateNum = 0;
        int ijmTP = 0;
        int ijmFP = 0;
        int ijmFN = 0;


        List<String[]> csvRecords = new ArrayList<>();
        for (StmtObj obj: allStmts){
            boolean gtInaccurate = stmtApproachResultMap.get(obj).getGtInaccurate() == 1;
            boolean mtdInaccurate = stmtApproachResultMap.get(obj).getMtdInaccurate() == 1;
            boolean ijmInaccurate = stmtApproachResultMap.get(obj).getIjmInaccurate() == 1;

//            boolean gtInaccurate = stmtApproachResultMap.get(obj).getCompareIjmInaccurate() == 1;
//            boolean mtdInaccurate = stmtApproachResultMap.get(obj).getCompareIjmInaccurate() == 1;

            boolean humanGtInaccurate = humanInaccurate(obj, "gt");
            boolean humanMtdInaccurate = humanInaccurate(obj, "mtdiff");
            boolean humanIjmInaccurate = humanInaccurate(obj, "ijm");

            int fileId = stmtUserStudyId.get(obj);
            if (!gtInaccurate && humanGtInaccurate) {
                StmtMapEvalRecord record = stmtApproachResultMap.get(obj);
                record.setFocusAlgorithm("gt");
                record.setFileId(fileId);
                csvRecords.add(record.toCsvRecords());
                System.out.println("gt: " + obj);
            }
            if (!mtdInaccurate && humanMtdInaccurate) {
                StmtMapEvalRecord record = stmtApproachResultMap.get(obj);
                record.setFocusAlgorithm("mtdiff");
                record.setFileId(fileId);
                csvRecords.add(record.toCsvRecords());
                System.out.println("mtd: " + obj);
            }
            if (!ijmInaccurate && humanIjmInaccurate) {
                StmtMapEvalRecord record = stmtApproachResultMap.get(obj);
                record.setFocusAlgorithm("ijm");
                record.setFileId(fileId);
                csvRecords.add(record.toCsvRecords());
                System.out.println("ijm: " + obj);
            }

            if (humanGtInaccurate)
                gtInaccurateNum ++;
            if (humanMtdInaccurate)
                mtdInaccurateNum ++;
            if (humanIjmInaccurate)
                ijmInaccurateNum ++;

            if (gtInaccurate && humanGtInaccurate)
                gtTP ++;
            if (gtInaccurate && !humanGtInaccurate)
                gtFP ++;
            if (!gtInaccurate && humanGtInaccurate)
                gtFN ++;

            if (mtdInaccurate && humanMtdInaccurate)
                mtdTP ++;
            if (mtdInaccurate && !humanMtdInaccurate)
                mtdFP ++;
            if (!mtdInaccurate && humanMtdInaccurate)
                mtdFN ++;

            if (ijmInaccurate && humanIjmInaccurate)
                ijmTP ++;
            if (ijmInaccurate && !humanIjmInaccurate)
                ijmFP ++;
            if (!ijmInaccurate && humanIjmInaccurate)
                ijmFN ++;
        }

        String csvPath = PathResolver.getUserStudyAnalysisCsvPath();
        CsvOperationsUtil.writeCSV(csvPath, StmtMapEvalRecord.getHeaders(), csvRecords);

        System.out.println("GT INACCURATE: " + gtInaccurateNum);
        System.out.println("MTD INACCURATE: " + mtdInaccurateNum);
        System.out.println("IJM INACCURATE: " + ijmInaccurateNum);

        System.out.println("GT TP: " + gtTP);
        System.out.println("GT FP: " + gtFP);
        System.out.println("GT FN: " + gtFN);
        System.out.println("GT precision: " + (double) gtTP / (gtTP + gtFP));
        System.out.println("GT recall: " + (double) gtTP / (gtTP + gtFN));

        System.out.println("MTD TP: " + mtdTP);
        System.out.println("MTD FP: " + mtdFP);
        System.out.println("MTD FN: " + mtdFN);
        System.out.println("MTD precision: " + (double) mtdTP / (mtdTP + mtdFP));
        System.out.println("MTD recall: " + (double) mtdTP / (mtdTP + mtdFN));

        System.out.println("IJM TP: " + ijmTP);
        System.out.println("IJM FP: " + ijmFP);
        System.out.println("IJM FN: " + ijmFN);
        System.out.println("IJM precision: " + (double) ijmTP / (ijmTP + ijmFP));
        System.out.println("IJM recall: " + (double) ijmTP / (ijmTP + ijmFN));
    }

    private static boolean humanInaccurate(StmtObj obj, String algorithm){
        if (algorithm.equals("gt")){
            List<Integer> scores = gtStmtScores.get(obj);
            int sum = 0;
            for (int score: scores)
                sum += score;
            return sum >= 2;
        }
        if (algorithm.equals("mtdiff")){
            List<Integer> scores = mtdStmtScores.get(obj);
            int sum = 0;
            for (int score: scores)
                sum += score;
            return sum >= 2;
        }
        if (algorithm.equals("ijm")){
            List<Integer> scores = ijmStmtScores.get(obj);
            int sum = 0;
            for (int score: scores)
                sum += score;
            return sum >= 2;
        }
        return false;
    }

    private static void getHumanDeterminationScores() throws Exception {
        String humanAnalysisDirPath = PathResolver.getUserStudyResultDirPath();
        File dir = new File(humanAnalysisDirPath);
        File[] files = dir.listFiles();
        for (File file: files){
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(".csv")){
                List<String[]> records = CsvOperationsUtil.getCSVData(filePath, getHeaders());
                for (String[] record: records){
                    String algorithm = record[3];
                    StmtObj obj = getStmtObjFromObj(record);

                    int fileId = Integer.parseInt(record[0]);
                    stmtUserStudyId.put(obj, fileId);

                    StmtMapEvalRecord sr = stmtApproachResultMap.get(obj);
                    boolean stmtInaccurate = record[9].trim().equals("1");
                    boolean tokenInaccurate = record[10].trim().equals("1");
                    int inaccurate = stmtInaccurate || tokenInaccurate ? 1 : 0;

                    if (algorithm.equals("gt")) {
                        if (!gtStmtScores.containsKey(obj))
                            gtStmtScores.put(obj, new ArrayList<>());

                        gtStmtScores.get(obj).add(inaccurate);
                        if (sr.getGtMtdInconsistent() != 1){
                            if (!mtdStmtScores.containsKey(obj))
                                mtdStmtScores.put(obj, new ArrayList<>());
                            mtdStmtScores.get(obj).add(inaccurate);
                        }
                        if (sr.getGtIjmInconsistent() != 1) {
                            if (!ijmStmtScores.containsKey(obj))
                                ijmStmtScores.put(obj, new ArrayList<>());
                            ijmStmtScores.get(obj).add(inaccurate);
                        }
                    }

                    if (algorithm.equals("mtdiff")){
                        if (!mtdStmtScores.containsKey(obj))
                            mtdStmtScores.put(obj, new ArrayList<>());
                        mtdStmtScores.get(obj).add(inaccurate);
                        if (sr.getMtdIjmInconsistent() != 1){
                            if (!ijmStmtScores.containsKey(obj))
                                ijmStmtScores.put(obj, new ArrayList<>());
                            ijmStmtScores.get(obj).add(inaccurate);
                        }
                    }


                    if (algorithm.equals("ijm")){
                        if (!ijmStmtScores.containsKey(obj))
                            ijmStmtScores.put(obj, new ArrayList<>());
                        ijmStmtScores.get(obj).add(inaccurate);
                    }

                }
            }
        }
    }

    private static StmtObj getStmtObjFromObj(String[] record){
        int fileId = Integer.parseInt(record[0]);
        String commitId = record[1];
        String filePath = record[2];
        int startPos = Integer.parseInt(record[4]);
        boolean isSrc = "1".equals(record[5].trim());
        String project = projects[(fileId - 1) / 20];
        return new StmtObj(project, commitId, filePath, startPos, isSrc);
    }

    private static void getApproachDeterminationScores() throws Exception {
        for (String project: projects) {
            System.out.println(project);
            List<StmtMapEvalRecord> stmtRecords = new ArrayList<>();
            String csvPath = PathResolver.getStmtMatchResultCsvPath(project);
            List<String[]> records = CsvOperationsUtil.getCSVData(csvPath, StmtMapEvalRecord.getHeaders());
            for (String[] record : records) {
                StmtMapEvalRecord sr = StmtMapEvalRecord.getStmtEvalRecordFromCsv(record);
                stmtRecords.add(sr);
                StmtObj obj = sr.getStmtObj();
                stmtApproachResultMap.put(obj, sr);
            }
        }

    }

    public static String[] getHeaders(){
        String[] headers = {
                "fileId", "commitId", "filePath", "algorithm", "startPos",
                "isSrc", "stmtType", "srcStmtLine", "dstStmtLine", "stmt-inaccurate", "token-inaccurate"
        };
        return headers;
    }
}
