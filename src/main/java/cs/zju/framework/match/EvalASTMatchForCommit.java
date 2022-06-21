package cs.zju.framework.match;

import cs.zju.eva.utils.FileRevision;
import cs.zju.framework.MatchASTAndTokens;
import cs.zju.gitops.GitHunk;
import cs.zju.gitops.GitInfoRetrieval;
import cs.zju.gitops.GitUtils;
import cs.zju.stm.TreeTokensMap;
import cs.zju.stm.match.StmtMatch;
import cs.zju.stm.match.TokenRangeTypeMap;

import java.util.*;

public class EvalASTMatchForCommit {
    private String project;
    private String commitId;
    private Set<String> filesToAnalyze;

    private String[] mapMethods;
    private Set<String> analyzedFiles;

    private Map<String, Map<String, EvalASTMatchResult>> fileMatchResultMap;
    private Map<String, List<InconsistentMatch>> inconsistentMatchMap;
    private Map<String, Map<String, Integer>> actionNumMap;

    private Map<String, Map<String, Double>> phase1TimeMap = new HashMap<>();
    private Map<String, Map<String, Double>> phase2TimeMap = new HashMap<>();

    private boolean keepResultMap = false;

    public EvalASTMatchForCommit(String project, FileRevision fr, String[] mapMethods){
        this.project = project;
        this.commitId = fr.first;
        this.filesToAnalyze = new HashSet<>();
        this.filesToAnalyze.add(fr.second);
        this.analyzedFiles = new HashSet<>();
        this.mapMethods = mapMethods;
        this.inconsistentMatchMap = new HashMap<>();
        this.actionNumMap = new HashMap<>();
        this.keepResultMap = true;
        this.fileMatchResultMap = new HashMap<>();
        UniversalTreeNode.initNodeFactoryMap();
        getMatchResults();
    }

    public EvalASTMatchForCommit(String project, String commitId, String[] mapMethods, Set<String> filesToAnalyze){
        this.project = project;
        this.commitId = commitId;
        this.filesToAnalyze = filesToAnalyze;
        this.mapMethods = mapMethods;
        this.analyzedFiles = new HashSet<>();
        this.inconsistentMatchMap = new HashMap<>();
        this.actionNumMap = new HashMap<>();
        UniversalTreeNode.initNodeFactoryMap();
        getMatchResults();
    }

    public Set<String> getAnalyzedFiles(){
        return analyzedFiles;
    }

    public Map<String, Map<String, Integer>> getActionNumMap() {
        return actionNumMap;
    }

    public Map<String, Map<String, EvalASTMatchResult>> getFileMatchResultMap() {
        return fileMatchResultMap;
    }

    private void addResultToMap(EvalASTMatchResult result, String algorithm, String filePath){
        if (!fileMatchResultMap.containsKey(filePath))
            fileMatchResultMap.put(filePath, new HashMap<>());
        fileMatchResultMap.get(filePath).put(algorithm, result);
    }

    // get all the matching results for GumTree, MTDiff and IJM
    // It is also possible to add more methods
    private void getMatchResults(){
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
                boolean nullResult = false;
                Map<String, EvalASTMatchResult> matchResultMap = new HashMap<>();
                Map<String, Double> algorithmTimeMap = new HashMap<>();
                TokenRangeTypeMap cacheSrcTtMap = null;
                TokenRangeTypeMap cacheDstTtMap = null;
                for (String algorithm: mapMethods) {
                    double time1 = System.currentTimeMillis();
                    MatchASTAndTokens matching = new MatchASTAndTokens(project, commitId,
                            baseCommitId, oldPath, newPath, algorithm);
                    matching.setSrcTokenTypeMap(cacheSrcTtMap);
                    matching.setDstTokenTypeMap(cacheDstTtMap);
                    cacheSrcTtMap = matching.getSrcTokenTypeMap();
                    cacheDstTtMap = matching.getDstTokenTypeMap();
                    EvalASTMatchResult result = matching.getASTMatchResult();
                    double time2 = System.currentTimeMillis();
                    double runningTime = time2 - time1;
                    if (result == null){
                        nullResult = true;
                        break;
                    }

                    if (keepResultMap){
                        addResultToMap(result, algorithm, oldPath);
                    }

                    double mappingTime = result.getTreeMappingTime();
                    double actionGenerationTime = result.getTreeActionGenerateTime();
                    runningTime = runningTime - mappingTime - actionGenerationTime;
                    matchResultMap.put(algorithm, result);
                    algorithmTimeMap.put(algorithm, runningTime);
                }

                if (nullResult)
                    continue;

                phase1TimeMap.put(oldPath, algorithmTimeMap);

                List<InconsistentMatch> comparisons = new ArrayList<>();
                for (int i = 0; i < mapMethods.length - 1; i++){
                    String algorithm1 = mapMethods[i];
                    for (int j = i + 1; j < mapMethods.length; j++){
                        String algorithm2 = mapMethods[j];
                        double time1 = System.currentTimeMillis();
                        InconsistentMatch comparison = new InconsistentMatch(project, commitId, oldPath,
                                algorithm1, algorithm2, matchResultMap, true);
                        comparisons.add(comparison);
                        double time2 = System.currentTimeMillis();
                        double runningTime = time2 - time1;

                        if (!phase2TimeMap.containsKey(oldPath))
                            phase2TimeMap.put(oldPath, new HashMap<>());
                        phase2TimeMap.get(oldPath).put(algorithm1 + "-" + algorithm2, runningTime);
                    }
                }

                inconsistentMatchMap.put(oldPath, comparisons);
                analyzedFiles.add(oldPath);

                putActionNumMap(oldPath, matchResultMap);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("HELLO: " + commitId);
            }
        }
    }

    private void putActionNumMap(String filePath, Map<String, EvalASTMatchResult> matchResultMap){
        actionNumMap.put(filePath, new HashMap<>());
        for (String algorithm: matchResultMap.keySet()) {
            actionNumMap.get(filePath).put(algorithm, matchResultMap.get(algorithm).getActionList().size());
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

    public Map<String, List<InconsistentMatch>> getInconsistentMatchMap() {
        return inconsistentMatchMap;
    }

    public Map<String, Map<String, Double>> getPhase1TimeMap() {
        return phase1TimeMap;
    }

    public Map<String, Map<String, Double>> getPhase2TimeMap(){
        return phase2TimeMap;
    }
}