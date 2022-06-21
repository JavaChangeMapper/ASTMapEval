package cs.zju.stm;

import com.github.gumtreediff.tree.ITree;
import cs.zju.gitops.GitHunk;
import cs.zju.gitops.GitLine;
import cs.zju.treeutils.RangeCalculation;

import java.util.*;

public class GitDiffLineMap {
    private String project;
    private String commitId;
    private String srcPath;
    private int srcLineNumber;
    private int dstLineNumber;
    private GitChunkHandler gitChunkHandler = null;



    public GitDiffLineMap(String project, String commitId, String srcPath,
                          int srcLineNumber, int dstLineNumber){
        this.project = project;
        this.commitId = commitId;
        this.srcPath = srcPath;
        this.srcLineNumber = srcLineNumber;
        this.dstLineNumber = dstLineNumber;
        calChunkStmtFilters();
    }

    private void calChunkStmtFilters() {
        List<GitLine> allGitLines = GitHunk
                .getGitLinesOfFile(project, commitId, srcPath, srcLineNumber, dstLineNumber);
        if (allGitLines != null)
            gitChunkHandler = new GitChunkHandler(allGitLines, srcLineNumber, dstLineNumber);
    }

    public GitChunkHandler getGitChunkHandler() {
        return gitChunkHandler;
    }


    public static class GitChunkHandler {
        private List<GitLine> gitLines;
        private int srcLineNumber;
        private int dstLineNumber;
        private Map<Integer, Integer> srcLineGitLineIdxMap;
        private Map<Integer, Integer> dstLineGitLineIdxMap;

        public GitChunkHandler(List<GitLine> gitLines, int srcLineNumber, int dstLineNumber){
            this.gitLines = gitLines;
            this.srcLineNumber = srcLineNumber;
            this.dstLineNumber = dstLineNumber;
            srcLineGitLineIdxMap = new HashMap<>();
            dstLineGitLineIdxMap = new HashMap<>();
            int index = 0;
            for (GitLine gitLine: gitLines){
                if (gitLine.getSrcLineNumber() != -1){
                    srcLineGitLineIdxMap.put(gitLine.getSrcLineNumber(), index);
                }
                if (gitLine.getDstLineNumber() != -1){
                    dstLineGitLineIdxMap.put(gitLine.getDstLineNumber(), index);
                }
                index ++;
            }
        }

        public int getIndexOfLine(int line, boolean isDeletedLine){
            if (isDeletedLine)
                return srcLineGitLineIdxMap.get(line);
            else
                return dstLineGitLineIdxMap.get(line);
        }

        public GitLine getGitLineByIndex(int idx){
            return gitLines.get(idx);
        }

        public int getGitLineSize(){
            return gitLines.size();
        }

        public int getStmtLine(ITree stmt, boolean isSrc,
                                RangeCalculation srcRc, RangeCalculation dstRc){
            if (isSrc)
                return srcRc.getLineNumberOfPos(stmt.getPos());
            else
                return dstRc.getLineNumberOfPos(stmt.getPos());
        }
    }
}
