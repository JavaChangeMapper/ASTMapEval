package cs.zju.eva.matchfw.result.userstudy;

import cs.zju.config.MyConfig;
import cs.zju.eva.matchfw.result.StmtMapEvalRecord;
import cs.zju.eva.utils.FileRevision;
import cs.zju.framework.match.EvalASTMatchForCommit;
import cs.zju.framework.match.EvalASTMatchResult;
import cs.zju.stm.match.StmtMatch;
import cs.zju.utils.CsvOperationsUtil;
import cs.zju.utils.PathResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

public class Generator2 {
    private static final String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static final String[] mapMethods = {"gt", "mtdiff", "ijm"};

    // 每个项目3个？
    private static final int numToAnalyze = 20;

    private static final int lowerLimit = 1;
    private static final int upperLimit = 20;
    private static final boolean withMetric = false;


    public static void main(String[] args) throws Exception {
        int revId  = 1;
        List<String[]> allRecords = new ArrayList<>();
        List<String[]> allInfoRecords = new ArrayList<>();
        for (String project: projects){
            Map<FileRevision, List<StmtMapEvalRecord>> revisionStmtMap = new HashMap<>();
            List<StmtMapEvalRecord> stmtRecords = new ArrayList<>();
            String csvPath = PathResolver.getStmtMatchResultCsvPath(project);
            List<String[]> records = CsvOperationsUtil.getCSVData(csvPath, StmtMapEvalRecord.getHeaders());
            for (String[] record: records) {
                StmtMapEvalRecord sr = StmtMapEvalRecord.getStmtEvalRecordFromCsv(record);
                stmtRecords.add(sr);
                FileRevision fr = sr.getFileRevision();
                if (!revisionStmtMap.containsKey(fr))
                    revisionStmtMap.put(fr, new ArrayList<>());
                revisionStmtMap.get(fr).add(sr);
            }

//            Collections.shuffle(stmtRecords, new Random(6));
            Collections.shuffle(stmtRecords, new Random(7));
            int revisionSize = 0;
            for (StmtMapEvalRecord record: stmtRecords) {
                FileRevision revision = record.getFileRevision();
                List<StmtMapEvalRecord> tmpRecords = revisionStmtMap.get(revision);
                if (tmpRecords.size() <= upperLimit && tmpRecords.size() >= lowerLimit) {
                    List<String> scripts = new ArrayList<>();
                    List<String[]> userRecords = new ArrayList<>();
                    List<String[]> infoRecords = new ArrayList<>();
                    String URL = "[COMMIT URL]( " + MyConfig.getCommitUrl(project, revision.first) + " )";
                    String filePath = "PATH: " + revision.second;
                    scripts.add(Integer.toString(revId));
                    scripts.add(URL);
                    scripts.add(filePath);
                    scripts.add("\n");
                    EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, revision, mapMethods);
                    Map<String, EvalASTMatchResult> evalResultMap = eval.getFileMatchResultMap().get(revision.second);
                    Map<String, Set<Integer>> dstStartPosMap = new HashMap<>();
                    dstStartPosMap.put("gt", new HashSet<>());
                    dstStartPosMap.put("mtdiff", new HashSet<>());
                    dstStartPosMap.put("ijm", new HashSet<>());

                    addRecordAndScriptText(project, revId, revision, record, scripts, userRecords, infoRecords,
                            evalResultMap, dstStartPosMap);
                    addRecordAndScriptTextForDst(project, revId, revision, record, scripts, userRecords, infoRecords,
                            evalResultMap, dstStartPosMap);
                    allRecords.addAll(userRecords);
                    allInfoRecords.addAll(infoRecords);
                    String userScriptPath = PathResolver.getUserStudyScriptPath(revId);

                    FileUtils.writeLines(new File(userScriptPath), scripts);
//                    String userCsvPath = PathResolver.getUserStudyCsvPath(revId);
//                    CsvOperationsUtil.writeCSV(userCsvPath, getHeaders(), userRecords);

                    revId ++;
                    revisionSize ++;
                    if (revisionSize == numToAnalyze)
                        break;
                }
            }
        }
        String userCsvPath = PathResolver.getUserStudyCsvPath(revId);
        CsvOperationsUtil.writeCSV(userCsvPath, getHeaders(), allRecords);
        String infoCsvPath = PathResolver.getUserStudyInfoCsvPath();
        CsvOperationsUtil.writeCSV(infoCsvPath, getMoreHeaders(), allInfoRecords);
    }

    private static void addRecordAndScriptText(String project, int revId,
                                               FileRevision revision,
                                               StmtMapEvalRecord record,
                                               List<String> scripts,
                                               List<String[]> userRecords,
                                               List<String[]> infoRecords,
                                               Map<String, EvalASTMatchResult> evalResultMap,
                                               Map<String, Set<Integer>> dstStartPosMap){

        if (!record.getStmtObj().isSrc())
            return;
        int gtInaccurate = record.getGtInaccurate();
        int mtdInaccurate = record.getMtdInaccurate();
        int ijmInaccurate = record.getIjmInaccurate();
        int startPos = record.getStmtObj().getStartPos();
        boolean gtPrint = false;
        boolean mtdPrint = false;
        boolean ijmPrint = false;
        EvalASTMatchResult gtResult = evalResultMap.get("gt");
        EvalASTMatchResult mtdResult = evalResultMap.get("mtdiff");
        EvalASTMatchResult ijmResult = evalResultMap.get("ijm");
        int id = 0;

        int gtMtdInconsistent = record.getGtMtdInconsistent();
        int gtIjmInconsistent = record.getGtIjmInconsistent();
        int mtdIjmInconsistent = record.getMtdIjmInconsistent();

        if (record.getGtMtdInconsistent() == 1 && (!gtPrint || !mtdPrint)){
            StmtMatch gtSm = gtResult.getStmtMatch(startPos, true);
            StmtMatch mtdSm = mtdResult.getStmtMatch(startPos, true);
            String gtMatchStr = gtSm.toString_V2(true);
            String mtdMatchStr = mtdSm.toString_V2(true);
            boolean isSrc = true;
            String[] gtUserRecord = gtSm.toRecord(revId);
            String[] mtdUserRecord = mtdSm.toRecord(revId);
            String[] gtRecord = gtSm.toRecord_V2(revId, revision.first, revision.second, gtInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);
            String[] mtdRecord = mtdSm.toRecord_V2(revId, revision.first, revision.second, mtdInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);

            int gtDstStartPos = gtSm.getDstStartPos();
            int mtdDstStartPos = mtdSm.getDstStartPos();

            dstStartPosMap.get("gt").add(gtDstStartPos);
            dstStartPosMap.get("mtdiff").add(mtdDstStartPos);

            if (!gtPrint) {
                scripts.add(gtMatchStr);
                userRecords.add(gtUserRecord);
                infoRecords.add(gtRecord);
            }
            if (!mtdPrint) {
                scripts.add(mtdMatchStr);
                userRecords.add(mtdUserRecord);
                infoRecords.add(mtdRecord);
            }
            gtPrint = true;
            mtdPrint = true;
        }

        if (record.getGtIjmInconsistent() == 1 && (!gtPrint || !ijmPrint)){
            StmtMatch gtSm = gtResult.getStmtMatch(startPos, true);
            StmtMatch ijmSm = ijmResult.getStmtMatch(startPos, true);
            String gtMatchStr = gtSm.toString_V2(true);
            String ijmMatchStr = ijmSm.toString_V2(true);
            boolean isSrc = true;
            String[] gtUserRecord = gtSm.toRecord(revId);
            String[] ijmUserRecord = ijmSm.toRecord(revId);
            String[] gtRecord = gtSm.toRecord_V2(revId, revision.first, revision.second, gtInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);
            String[] ijmRecord = ijmSm.toRecord_V2(revId, revision.first, revision.second, ijmInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);


            int gtDstStartPos = gtSm.getDstStartPos();
            int ijmDstStartPos = ijmSm.getDstStartPos();

            dstStartPosMap.get("gt").add(gtDstStartPos);
            dstStartPosMap.get("ijm").add(ijmDstStartPos);

            if (!gtPrint) {
                scripts.add(gtMatchStr);
                userRecords.add(gtUserRecord);
                infoRecords.add(gtRecord);
            }
            if (record.getMtdIjmInconsistent() == 0)
                ijmPrint = true;
            if (!ijmPrint) {
                scripts.add(ijmMatchStr);
                userRecords.add(ijmUserRecord);
                infoRecords.add(ijmRecord);
            }
        }
    }

    private static void addRecordAndScriptTextForDst(String project, int revId,
                                                     FileRevision revision,
                                                     StmtMapEvalRecord record,
                                                     List<String> scripts,
                                                     List<String[]> userRecords,
                                                     List<String[]> infoRecords,
                                                     Map<String, EvalASTMatchResult> evalResultMap,
                                                     Map<String, Set<Integer>> dstStartPosMap){
        if (record.getStmtObj().isSrc())
            return;
        int gtInaccurate = record.getGtInaccurate();
        int mtdInaccurate = record.getMtdInaccurate();
        int ijmInaccurate = record.getIjmInaccurate();
        int startPos = record.getStmtObj().getStartPos();
        boolean gtPrint = false;
        boolean mtdPrint = false;
        boolean ijmPrint = false;
        EvalASTMatchResult gtResult = evalResultMap.get("gt");
        EvalASTMatchResult mtdResult = evalResultMap.get("mtdiff");
        EvalASTMatchResult ijmResult = evalResultMap.get("ijm");

        int gtMtdInconsistent = record.getGtMtdInconsistent();
        int gtIjmInconsistent = record.getGtIjmInconsistent();
        int mtdIjmInconsistent = record.getMtdIjmInconsistent();

        if (record.getGtMtdInconsistent() == 1 && (!gtPrint || !mtdPrint)){
            StmtMatch gtSm = gtResult.getStmtMatch(startPos, false);
            StmtMatch mtdSm = mtdResult.getStmtMatch(startPos, false);
            String gtMatchStr = gtSm.toString_V2(false);
            String mtdMatchStr = mtdSm.toString_V2(false);

            boolean isSrc = false;

            String[] gtUserRecord = gtSm.toRecord(revId);
            String[] mtdUserRecord = mtdSm.toRecord(revId);
            String[] gtRecord = gtSm.toRecord_V2(revId, revision.first, revision.second, gtInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);
            String[] mtdRecord = mtdSm.toRecord_V2(revId, revision.first, revision.second, mtdInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);

            int gtDstStartPos = gtSm.getDstStartPos();
            int mtdDstStartPos = mtdSm.getDstStartPos();

            if (dstStartPosMap.get("gt").contains(gtDstStartPos))
                gtPrint = true;
            if (dstStartPosMap.get("mtdiff").contains(mtdDstStartPos))
                mtdPrint = true;

            if (!gtPrint) {
                scripts.add(gtMatchStr);
                userRecords.add(gtUserRecord);
                infoRecords.add(gtRecord);
            }
            if (!mtdPrint) {
                scripts.add(mtdMatchStr);
                userRecords.add(mtdUserRecord);
                infoRecords.add(mtdRecord);
            }
            gtPrint = true;
            mtdPrint = true;
        }

        if (record.getGtIjmInconsistent() == 1 && (!gtPrint || !ijmPrint)){
            StmtMatch gtSm = gtResult.getStmtMatch(startPos, false);
            StmtMatch ijmSm = ijmResult.getStmtMatch(startPos, false);
            String gtMatchStr = gtSm.toString_V2(false);
            String ijmMatchStr = ijmSm.toString_V2(false);
            boolean isSrc = false;

            String[] gtUserRecord = gtSm.toRecord(revId);
            String[] ijmUserRecord = ijmSm.toRecord(revId);
            String[] gtRecord = gtSm.toRecord_V2(revId, revision.first, revision.second, gtInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);
            String[] ijmRecord = ijmSm.toRecord_V2(revId, revision.first, revision.second, ijmInaccurate, isSrc,
                    gtMtdInconsistent, gtIjmInconsistent, mtdIjmInconsistent);

            int gtDstStartPos = gtSm.getDstStartPos();
            int ijmDstStartPos = ijmSm.getDstStartPos();

            if (dstStartPosMap.get("gt").contains(gtDstStartPos))
                gtPrint = true;
            if (dstStartPosMap.get("ijm").contains(ijmDstStartPos))
                ijmPrint = true;

            if (!gtPrint) {
                scripts.add(gtMatchStr);
                userRecords.add(gtUserRecord);
                infoRecords.add(gtRecord);
            }

            if (record.getMtdIjmInconsistent() == 0)
                ijmPrint = true;
            if (!ijmPrint) {
                scripts.add(ijmMatchStr);
                userRecords.add(ijmUserRecord);
                infoRecords.add(ijmRecord);
            }
        }
    }

    private static String[] getMoreHeaders(){
        String[] headers = {
                "fileId", "commitId", "filePath", "algorithm", "startPos", "isSrc", "stmtType", "srcStmtLine", "dstStmtLine",
                "inaccurate", "gtMtdInconsistent", "gtIjmInconsistent", "mtdIjmInconsistent"
        };
        return headers;
    }

    private static String[] getHeaders(){
        String[] headers = {
                "fileId", "stmtType", "srcStmtLine", "dstStmtLine", "stmt-inaccurate", "token-inaccurate"
        };
        return headers;
    }
}
