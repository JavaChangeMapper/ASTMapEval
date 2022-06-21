package cs.zju.framework.edit;

import cs.zju.framework.MatchASTAndTokens;
import cs.zju.gitops.GitHunk;
import cs.zju.gitops.GitInfoRetrieval;
import cs.zju.gitops.GitUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvalCommitAnalysisWithEdit {
    private String project;
    private String commitId;
    private String mapMethod;

    private double wordEdits;
    private double parentNotMatchNumber;
    private double alignStmtsNumber = 0;
    private double numLowQualityMapWords;
    private double runTime = 0;

    private List<EvalComparisonResultWithEdit> originResultList = new ArrayList<>();

    private Set<String> filesToAnalyze;

    public EvalCommitAnalysisWithEdit(String project, String commitId, String mapMethod, Set<String> filesToAnalyze){
        this.project = project;
        this.commitId = commitId;
        this.mapMethod = mapMethod;
        runTime = 0;
        this.filesToAnalyze = filesToAnalyze;
        getComparisonResults();
    }

    private void getComparisonResults(){

        String baseCommitId = GitUtils.getBaseCommitId(project, commitId);
        if (baseCommitId == null)
            return;

        Map<String, String> pathMap = GitInfoRetrieval.getOldModifiedFileMap(project, commitId);
        if (pathMap == null || pathMap.size() == 0)
            return;

        for (String oldPath: pathMap.keySet()) {
            if (filesToAnalyze != null && !filesToAnalyze.contains(oldPath))
                continue;
            String newPath = pathMap.get(oldPath);
            if (newPath == null)
                continue;
            if (checkOnlyRenameOperation(project, baseCommitId, commitId, oldPath, newPath)) {
                continue;
            }
            if (checkAddedOrDeletedLines(oldPath, newPath))
                continue;

            try {
                MatchASTAndTokens fm = new MatchASTAndTokens(project, commitId,
                        baseCommitId, oldPath, newPath, mapMethod);
                EvalComparisonResultWithEdit result2 = fm.getOriginComparisonResult();
                if (result2 != null)
                    originResultList.add(result2);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("HELLO: " + commitId);
            }
        }
    }

    private boolean checkOnlyRenameOperation(String project, String baseCommitId,
                                             String commitId, String oldFilePath,
                                             String newFilePath) {
        try {
            String oldContent = GitUtils
                    .getFileContentOfCommitFile(project, baseCommitId, oldFilePath)
                    .toString("UTF-8");
            String newContent = GitUtils
                    .getFileContentOfCommitFile(project, commitId, newFilePath)
                    .toString("UTF-8");
            return oldContent.equals(newContent);
        } catch (Exception e){
            throw new RuntimeException("cannot retrieve file content");
        }
    }

    private boolean checkAddedOrDeletedLines(String srcFilePath, String dstFilePath){
        Set<Integer> addedLines = GitHunk.getAllAddedLines(project, commitId, srcFilePath, false);
        Set<Integer> deletedLines = GitHunk.getAllDeletedLines(project, commitId, srcFilePath, false);

        boolean nonAddedLines = addedLines == null || addedLines.size() == 0;
        boolean nonDeletedLines = deletedLines == null || deletedLines.size() == 0;

        return nonAddedLines && nonDeletedLines;
    }

    public boolean hasAmbiguousMapping(List<EvalComparisonResultWithEdit> resultList){
        if (resultList != null) {
            for (EvalComparisonResultWithEdit comparisonResult : resultList) {
                if (comparisonResult.hasAmbiguousMapping())
                    return true;
            }
        }
        return false;
    }

    public double getRunTime(List<EvalComparisonResultWithEdit> resultList){
        runTime = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            runTime += result.getMapTime();
        }
        return runTime;
    }

    public int getWordEdits() {
        return (int) wordEdits;
    }

    public int getMoveStmtCount() {
        return (int) parentNotMatchNumber;
    }

    public int getAlignStmtsNumber() {
        return (int) alignStmtsNumber;
    }

    public int getLowQualityMappedWordCount(){
        return (int) numLowQualityMapWords;
    }

    public List<EvalComparisonResultWithEdit> getOriginResultList() {
        return originResultList;
    }

    public String getProject() {
        return project;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getMapMethod() {
        return mapMethod;
    }

    public int getSrcTreeNodeCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getSrcTreeNodeCount();
        }
        return count;
    }

    public int getDstTreeNodeCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getDstTreeNodeCount();
        }
        return count;
    }

    public int getSrcToDstAmbiguousMappingCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getSrcToDstAmbiguosCount();
        }
        return count;
    }

    public int getDstToSrcAmbiguousMappingCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getDstToSrcAmbiguousCount();
        }
        return count;
    }

    public int getAmbiguousActionCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getAmbiguousActionCount();
        }
        return count;
    }

    public int getUnambiguousActionCount(List<EvalComparisonResultWithEdit> resultList){
        int count = 0;
        for (EvalComparisonResultWithEdit result: resultList){
            if (result == null)
                continue;
            count += result.getUnambiguousActionCount();
        }
        return count;
    }

    public void calMeasures(List<EvalComparisonResultWithEdit> resultList){
        wordEdits = 0;
        parentNotMatchNumber = 0;
        numLowQualityMapWords = 0;
        alignStmtsNumber = 0;

        for (EvalComparisonResultWithEdit result: resultList){
            wordEdits += result.getWordEdits();
            numLowQualityMapWords += result.getMeaninglessMoveCount();
            parentNotMatchNumber += result.getMoveStmtCount(true);
        }
    }

    public void close(){
        originResultList.clear();
    }
}
