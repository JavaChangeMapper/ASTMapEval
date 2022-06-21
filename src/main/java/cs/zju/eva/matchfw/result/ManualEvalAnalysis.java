package cs.zju.eva.matchfw.result;

import cs.zju.stm.match.StmtMatchDiffDesc;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;

import java.util.*;

public class ManualEvalAnalysis {

    private static String[] algorithms = {"gt", "mtdiff", "ijm"};
    private static String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    public static void main(String[] args) throws Exception {
        Map<Set<String>, List<StmtRecord>> allMap = new HashMap<>();
        for (String project: projects){
            Map<Set<String>, List<StmtRecord>> projectMap = new HashMap<>();
            for(int i = 0; i < algorithms.length - 1; i++){
                String algorithm1 = algorithms[i];
                for (int j = i + 1; j < algorithms.length; j++){
                    String algorithm2 = algorithms[j];
                    List<ComparisonRecord> recordList = getAllComparisonRecords(project, algorithm1, algorithm2);
                    projectMap.putAll(StmtRecord.getStmtRecords(recordList, project));
                }
            }

            printStatistics(projectMap);

            System.out.println(project);
            for (Set<String> key: projectMap.keySet()){
                System.out.println(key);
                System.out.println(projectMap.get(key).size() / 2);
                if(allMap.containsKey(key))
                    allMap.get(key).addAll(projectMap.get(key));
                else
                    allMap.put(key, projectMap.get(key));
            }
        }
        System.out.println();
        for (Set<String> key: allMap.keySet()){
            System.out.println(key);
            System.out.println(allMap.get(key).size() / 2);
        }
        printStatistics(allMap);

        calInaccurateMappingStatistics(allMap);

//        calculateFindInaccurateMappingStatistics(allMap);
    }

    private static void printStatistics(Map<Set<String>, List<StmtRecord>> projectMap){
        String[] algorithms = {"GT", "MTDIFF", "IJM"};
        for (int i = 0; i < algorithms.length - 1; i++){
            String algorithm1 = algorithms[i];
            for (int j = i + 1; j < algorithms.length; j++){
                String algorithm2 = algorithms[j];
                int stmtNum1 = calculateAlgorithmInaccurateMapping(algorithm1, algorithm2,
                        projectMap, "stmt");
                int tokenNum1 = calculateAlgorithmInaccurateMapping(algorithm1, algorithm2,
                        projectMap, "token");
                int eitherNum1 = calculateAlgorithmInaccurateMapping(algorithm1, algorithm2,
                        projectMap, "either");
                int bothNum1 = calculateAlgorithmInaccurateMapping(algorithm1, algorithm2,
                        projectMap, "both");

                int stmtNum2 = calculateAlgorithmInaccurateMapping(algorithm2, algorithm1,
                        projectMap, "stmt");
                int tokenNum2 = calculateAlgorithmInaccurateMapping(algorithm2, algorithm1,
                        projectMap, "token");
                int eitherNum2 = calculateAlgorithmInaccurateMapping(algorithm2, algorithm1,
                        projectMap, "either");
                int bothNum2 = calculateAlgorithmInaccurateMapping(algorithm2, algorithm1,
                        projectMap, "both");

                System.out.println(algorithm1 + ": " + stmtNum1 + " stmt bad match");
                System.out.println(algorithm1 + ": " + tokenNum1 + " token bad match");
                System.out.println(algorithm1 + ": " + eitherNum1 + " either bad match");
                System.out.println(algorithm1 + ": " + bothNum1 + " both bad match");

                System.out.println();
                System.out.println(algorithm2 + ": " + stmtNum2 + " stmt bad match");
                System.out.println(algorithm2 + ": " + tokenNum2 + " token bad match");
                System.out.println(algorithm2 + ": " + eitherNum2 + " either bad match");
                System.out.println(algorithm2 + ": " + bothNum2 + " both bad match");
            }
        }
    }

    private static int getOurErrorType(StmtRecord record){
        int errorType = record.getErrorType();
        boolean isStmtBadMatch = record.isStmtBadMatch();
        boolean isTokenBadMatch = record.isTokenBadMatch();
        int tokenErrorType = record.getTokenErrorType();

        if (!isStmtBadMatch && !isTokenBadMatch)
            return -1;

        if (isStmtBadMatch){
            if (errorType == 6 || (record.getRatioOfIdenticalToken() == 0 && errorType == 10))
                return 1;
            if (errorType == 4)
                return 2;
            if (errorType == 2 || errorType == 7)
                return 3;
        }
        if (isTokenBadMatch){
            if (tokenErrorType == 4)
                return 4;
            if (tokenErrorType == 2)
                return 6;
            if (errorType == 6 || errorType == 5)
                return 5;
            if (errorType == 8 && (tokenErrorType == 3))
                return 5;
            if ((errorType == 14 || errorType == 15 || errorType == 16) && (tokenErrorType == 3))
                return 5;
            if (errorType == 3)
                return 7;
        }

        if (errorType == 8)
            System.out.println(record);

        return 1000 + record.getErrorType();
    }

    private static void calInaccurateMappingStatistics(Map<Set<String>, List<StmtRecord>> projectMap){
        List<List<StmtRecord>> recordsList = new ArrayList<>(projectMap.values());
        List<StmtRecord> records = new ArrayList<>();
        for (List<StmtRecord> rs: recordsList){
            records.addAll(rs);
        }
        int numInaccurate = 0;
        Map<Integer, Integer> typeSizeMap = new HashMap<>();
        for (StmtRecord record: records){
            if (!record.getAlgorithm().equals("IJM"))
                continue;
            int ourType = getOurErrorType(record);
            if (ourType == -1)
                continue;
            if (!typeSizeMap.containsKey(ourType))
                typeSizeMap.put(ourType, 0);
            typeSizeMap.put(ourType, typeSizeMap.get(ourType) + 1);
            numInaccurate ++;
        }

        System.out.println(numInaccurate);
        System.out.println(typeSizeMap);
    }

    private static void calculateFindInaccurateMappingStatistics(Map<Set<String>, List<StmtRecord>> projectMap){
        List<List<StmtRecord>> recordsList = new ArrayList<>(projectMap.values());
        List<StmtRecord> records = new ArrayList<>();
        for (List<StmtRecord> rs: recordsList){
            records.addAll(rs);
        }
        System.out.println(records.size());
        double numInaccurateStmt = 0;
        double numFindInaccurateStmt = 0;
        double numFindInaccurateButAccurate = 0;
        double numAllReportInaccurate = 0;

        double numExceptionInaccurate = 0;
        double numExceptionInaccurateFound = 0;

        Map<Integer, Integer> stmtTypeMap = new HashMap<>();
        Map<Integer, Integer> tokenTypeMap = new HashMap<>();

        for (StmtRecord record: records){
            if(!record.getAlgorithm().equals("IJM"))
                continue;
            numInaccurateStmt += record.isStmtBadMatch() ||  record.isTokenBadMatch() ? 1 : 0;
            numFindInaccurateStmt += record.findInaccuracy() ? 1 : 0;
            numFindInaccurateButAccurate += record.findAccurateMappingInaccurate() ? 1 : 0;
            numAllReportInaccurate += record.isMethodBadMatch() ? 1 : 0;
            numExceptionInaccurate += record.getErrorType() == 100 ? 1: 0;
            numExceptionInaccurateFound += record.getErrorType() == 100 && record.isMethodBadMatch() ? 1:0;

            int stmtErrorType = record.getStmtErrorType();
            int tokenErrorType = record.getTokenErrorType();

            int[] types = {
                8, 9, 10, 13, 14, 15, 16, 17, 18
            };

            Set<Integer> typeSet = new HashSet<>();
            for (int i: types){
                typeSet.add(i);
            }

            if (tokenErrorType != -1){
                if (!tokenTypeMap.containsKey(tokenErrorType))
                    tokenTypeMap.put(tokenErrorType, 0);
                if (typeSet.contains(stmtErrorType)) {
                    tokenTypeMap.put(tokenErrorType, tokenTypeMap.get(tokenErrorType) + 1);

                }
            }

            if (stmtErrorType != 0) {
                if (!stmtTypeMap.containsKey(stmtErrorType)) {
                    stmtTypeMap.put(stmtErrorType, 0);
                }


                if (stmtErrorType == 8 || stmtErrorType == 9 || stmtErrorType == 10){
                    if (tokenErrorType == -1) {
                        stmtTypeMap.put(stmtErrorType, stmtTypeMap.get(stmtErrorType) + 1);
                        System.out.println(record);
                    }
                } else if (stmtErrorType == 13 || stmtErrorType == 14 || stmtErrorType == 15) {
                    if (tokenErrorType == -1){
                        stmtTypeMap.put(stmtErrorType, stmtTypeMap.get(stmtErrorType) + 1);
                    }
                } else if (stmtErrorType != 16 && stmtErrorType != 17 && stmtErrorType != 18){
                    stmtTypeMap.put(stmtErrorType, stmtTypeMap.get(stmtErrorType) + 1);
                }
            }

//            if ((record.isStmtBadMatch() ||  record.isTokenBadMatch()) && !record.isMethodBadMatch()){
//                System.out.println(record.getErrorType());
//                System.out.println(record.getProject());
//                System.out.println(record.getAlgorithm());
//                System.out.println(record.getCompareAlgorithm());
//                System.out.println(record.getFr());
//                System.out.println(record.getStmtCharStartPos());
//            }

            if (record.findAccurateMappingInaccurate()){
                System.out.println(record);
            }

        }

        System.out.println(stmtTypeMap);
        System.out.println(tokenTypeMap);

        System.out.println(numExceptionInaccurate);
        System.out.println(numExceptionInaccurateFound);
        System.out.println(numFindInaccurateStmt);
        System.out.println(numInaccurateStmt);
        System.out.println(numFindInaccurateStmt / numInaccurateStmt);
        System.out.println(numFindInaccurateStmt / numAllReportInaccurate);
        System.out.println(numFindInaccurateButAccurate);
    }

    private static int calculateAlgorithmInaccurateMapping(String algorithm1,
                                                           String algorithm2,
                                                           Map<Set<String>, List<StmtRecord>> projectMap,
                                                           String type){
        Set<String> algorithms = new HashSet<>();
        algorithms.add(algorithm1);
        algorithms.add(algorithm2);
        List<StmtRecord> allRecordsForComparison = projectMap.get(algorithms);
        int ret = 0;
        if (allRecordsForComparison == null)
            return ret;
        for (StmtRecord sr: allRecordsForComparison){
            if (sr.getAlgorithm().equals(algorithm1)) {
                if (sr.isStmtBadMatch() && type.equals("stmt")) {
                    ret++;
                }

                if (sr.isTokenBadMatch() && type.equals("token")) {
                    ret++;
                }

                if ((sr.isStmtBadMatch() || sr.isTokenBadMatch()) && type.equals("either")) {
                    ret++;
                }

                if (sr.isStmtBadMatch() && sr.isTokenBadMatch() && type.equals("both")){
                    ret ++;
                }
            }
        }
        return ret;
    }

    private static List<ComparisonRecord> getAllComparisonRecords(String project, String algorithm1,
                                                                  String algorithm2) throws Exception {
        String filePath = PathResolver.getStmtMatchManualResultCsvPath(project, algorithm1, algorithm2);
        List<String[]> records = CsvOperationsUtil.getCSVData(filePath, getManualCsvHeaders());
        String manualPath = PathResolver.getStmtMatchManualAnalysisCsvPath(project, algorithm1, algorithm2);
        List<String[]> newRecords = CsvOperationsUtil.getCSVData(manualPath, StmtMatchDiffDesc.getHeaders());
        List<ComparisonRecord> retList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++){
            String[] record = records.get(i);
            String[] newRecord = newRecords.get(i);
            ComparisonRecord compRecord = new ComparisonRecord(record, true, false);
            compRecord.setStmtErrorType(Integer.parseInt(newRecord[16]));
            compRecord.setSrcTokens(Integer.parseInt(newRecord[17]));
            compRecord.setDstTokens(Integer.parseInt(newRecord[18]));
            compRecord.setSrcSameMatch(Integer.parseInt(newRecord[19]));
            compRecord.setDstSameMatch(Integer.parseInt(newRecord[20]));
            compRecord.setSrcStmtOrTokenError(Integer.parseInt(newRecord[21]));
            compRecord.setDstStmtOrTokenError(Integer.parseInt(newRecord[22]));
            compRecord.setSrcCrossStmtMatchToken(Integer.parseInt(newRecord[23]));
            compRecord.setDstCrossStmtMatchToken(Integer.parseInt(newRecord[24]));
            compRecord.setSrcTokenErrorType(Integer.parseInt(newRecord[25]));
            compRecord.setDstTokenErrorType(Integer.parseInt(newRecord[26]));
            retList.add(compRecord);
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
