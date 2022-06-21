package cs.zju.treeutils;

import com.github.gumtreediff.tree.ITree;

import java.util.List;

public class CommitFileInfo {
    private String commitId;
    private String srcFilePath;
    private String dstFilePath;
    private RangeCalculation srcRangeCalculation;
    private RangeCalculation dstRangeCalculation;

    private ITree srcRoot;
    private ITree dstRoot;

    private String srcPkgName;
    private String dstPkgName;

    private List<GumTreeEditAction> globalActions;

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setSrcFilePath(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public void setDstFilePath(String dstFilePath) {
        this.dstFilePath = dstFilePath;
    }

    public String getDstFilePath() {
        return dstFilePath;
    }

    public void setSrcRangeCalculation(RangeCalculation srcRangeCalculation) {
        this.srcRangeCalculation = srcRangeCalculation;
    }

    public void setSrcRoot(ITree srcRoot) {
        this.srcRoot = srcRoot;
    }

    public ITree getSrcRoot() {
        return srcRoot;
    }

    public void setDstRoot(ITree dstRoot) {
        this.dstRoot = dstRoot;
    }

    public ITree getDstRoot() {
        return dstRoot;
    }

    public void setSrcPkgName(String srcPkgName) {
        this.srcPkgName = srcPkgName;
    }

    public void setDstPkgName(String dstPkgName) {
        this.dstPkgName = dstPkgName;
    }

    public void setDstRangeCalculation(RangeCalculation dstRangeCalculation) {
        this.dstRangeCalculation = dstRangeCalculation;
    }

    public void setGlobalActions(List<GumTreeEditAction> globalActions) {
        this.globalActions = globalActions;
    }

    public List<GumTreeEditAction> getGlobalActions() {
        return globalActions;
    }

    public String getSrcFileContent(){
        if (srcRangeCalculation == null)
            return null;
        return srcRangeCalculation.getFileContent();
    }

    public String getDstFileContent(){
        if (dstRangeCalculation == null)
            return null;
        return dstRangeCalculation.getFileContent();
    }
}
