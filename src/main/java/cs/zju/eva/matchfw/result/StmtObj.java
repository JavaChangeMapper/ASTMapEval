package cs.zju.eva.matchfw.result;


public class StmtObj {
    private boolean isSrc;
    private String project = "";
    private String commitId = "";
    private String filePath = "";
    private int startPos;

    public StmtObj(String project, String commitId, String filePath, int startPos, boolean isSrc){
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
        this.startPos = startPos;
        this.isSrc = isSrc;
    }

    public StmtObj(ComparisonRecord record, boolean isSrc, String project){
        this.isSrc = isSrc;
        this.project = project;
        this.commitId = record.getCommitId();
        this.filePath = record.getFilePath();
        if (isSrc)
            this.startPos = record.getSrcCharPos();
        else
            this.startPos = record.getDstCharPos();
    }

    public boolean isSrc() {
        return isSrc;
    }

    public int getStartPos() {
        return startPos;
    }

    public boolean isNull(){
        return startPos == -1;
    }

    public String[] toCsvRecord(){
        String[] record = {
                project, commitId, filePath,
                isSrc ? "1" : "0",
                Integer.toString(startPos)
        };
        return record;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StmtObj obj = (StmtObj) o;
        if (obj.isSrc != isSrc)
            return false;
        if (!obj.project.equals(project))
            return false;
        if (!obj.commitId.equals(commitId))
            return false;
        if (!obj.filePath.equals(filePath))
            return false;
        if (obj.startPos != startPos)
            return false;


        return true;
    }

    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + Boolean.hashCode(isSrc);
        hash  = hash * 31 + commitId.hashCode();
        hash = hash * 31 + filePath.hashCode();
        hash = hash * 31 + Integer.hashCode(startPos);
        return hash;
    }

    @Override
    public String toString() {
        return "StmtObj{" +
                "isSrc=" + isSrc +
                ", project='" + project + '\'' +
                ", commitId='" + commitId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", startPos=" + startPos +
                '}';
    }

    public static String[] getHeaders(){
        String[] headers = {
                "project", "commitId", "filePath", "isSrc", "startPos"
        };
        return headers;
    }
}
