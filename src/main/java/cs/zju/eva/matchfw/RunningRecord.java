package cs.zju.eva.matchfw;

import java.util.Map;

public class RunningRecord {
    private String commitId;
    private String filePath;
    private int numOfActionGT;
    private int numOfActionMTDiff;
    private int numOfActionIJM;
    private double gtPhase1Time;
    private double mtdPhase1Time;
    private double ijmPhase1Time;
    private double gtMtdTime;
    private double gtIjmTime;
    private double mtdIjmTime;

    public RunningRecord(String commitId, String filePath,
                         Map<String, Integer> actionNumMap,
                         Map<String, Double> phase1TimeMap,
                         Map<String, Double> phase2TimeMap) {
        this(commitId, filePath, actionNumMap.get("gt"), actionNumMap.get("mtdiff"), actionNumMap.get("ijm"),
                phase1TimeMap.get("gt"), phase1TimeMap.get("mtdiff"), phase1TimeMap.get("ijm"),
                phase2TimeMap.get("gt-mtdiff"), phase2TimeMap.get("gt-ijm"),
                phase2TimeMap.get("mtdiff-ijm"));
    }

    public RunningRecord(String commitId, String filePath, int numOfActionGT,
                         int numOfActionMTDiff, int numOfActionIJM,
                         double gtPhase1Time, double mtdPhase1Time, double ijmPhase1Time,
                         double gtMtdTime, double gtIjmTime, double mtdIjmTime){
        this.commitId = commitId;
        this.filePath = filePath;
        this.numOfActionGT = numOfActionGT;
        this.numOfActionMTDiff = numOfActionMTDiff;
        this.numOfActionIJM = numOfActionIJM;
        this.gtPhase1Time = gtPhase1Time;
        this.mtdPhase1Time = mtdPhase1Time;
        this.ijmPhase1Time = ijmPhase1Time;
        this.gtMtdTime = gtMtdTime;
        this.gtIjmTime = gtIjmTime;
        this.mtdIjmTime = mtdIjmTime;
    }

    public static RunningRecord fromCsvRecord(String[] record){
        return new RunningRecord(record[0],
                record[1], Integer.parseInt(record[2]),
                Integer.parseInt(record[3]),
                Integer.parseInt(record[4]),
                Double.parseDouble(record[5]),
                Double.parseDouble(record[6]),
                Double.parseDouble(record[7]),
                Double.parseDouble(record[8]),
                Double.parseDouble(record[9]),
                Double.parseDouble(record[10]));
    }

    public String getCommitId() {
        return commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String[] toCsvRecord(){
        String[] record = {
                commitId,
                filePath,
                Integer.toString(numOfActionGT),
                Integer.toString(numOfActionMTDiff),
                Integer.toString(numOfActionIJM),
                Double.toString(gtPhase1Time),
                Double.toString(mtdPhase1Time),
                Double.toString(ijmPhase1Time),
                Double.toString(gtMtdTime),
                Double.toString(gtIjmTime),
                Double.toString(mtdIjmTime)
        };
        return record;
    }

    public static String[] getHeaders(){
        String[] headers = {"commitId", "filePath", "numOfActionGT", "numOfActionMTDiff", "numOfActionIJM",
                            "gtPhase1Time", "mtdPhase1Time", "ijmPhase1Time",
                            "gtMtdTime", "gtIjmTime", "mtdIjmTime"};
        return headers;
    }
}
