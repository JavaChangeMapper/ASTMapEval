package cs.zju.eva.matchfw;

public class CacheRunningRecord {
    private String commitId;
    private String filePath;

    public CacheRunningRecord(String commitId, String filePath){
        this.commitId = commitId;
        this.filePath = filePath;
    }

    public String[] toCsvRecord(){
        String[] record = {commitId, filePath};
        return record;
    }

    public static String[] getHeaders(){
        String[] headers = {"commitId", "filePath"};
        return headers;
    }
}
