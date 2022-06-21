package cs.zju.framework;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import cs.zju.gitops.GitService;
import cs.zju.treeutils.GumTreeUtil;
import cs.zju.treeutils.RangeCalculation;
import cs.zju.utils.Pair;
import cs.zju.utils.PathResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class MatchASTAndTokensOnDiskFile extends MatchASTAndTokens {
    public MatchASTAndTokensOnDiskFile(String srcFilePathInDisk, String dstFilePathInDisk,
                                       String fileName,
                                       String mapMethod,
                                       String tmpProjectName, boolean useFile){

        String projectFolder = PathResolver.projectFolder(tmpProjectName);
        this.srcFilePath = fileName;
        this.dstFilePath = fileName;
        this.project = tmpProjectName;
//        System.out.println(srcFilePathInDisk);
        try {
            Pair<String, String> commitIds = GitService.createGitRepoForFileRevision(srcFilePathInDisk,
                    dstFilePathInDisk, fileName, projectFolder);
            this.commitId = commitIds.second;
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("create repo error");
        }
        this.mapMethod = mapMethod;

        try {
            srcFileContent = FileUtils.readFileToString(new File(srcFilePathInDisk), "UTF-8");
            dstFileContent = FileUtils.readFileToString(new File(dstFilePathInDisk), "UTF-8");
        } catch (Exception e){
            throw new RuntimeException("read file error");
        }

        try{
            srcRoot = GumTreeUtil.getITreeRoot(srcFileContent, mapMethod);
            srcRc = new RangeCalculation(srcFileContent);
            calAllStmts(srcRoot, true);
            dstRoot = GumTreeUtil.getITreeRoot(dstFileContent, mapMethod);
            dstRc = new RangeCalculation(dstFileContent);
            calAllStmts(dstRoot, false);
        } catch (Exception e){
            this.srcFilePath = null;
            this.dstFilePath = null;
            throw new RuntimeException(e.getMessage());
        }

        for (ITree t: srcRoot.preOrder()){
            System.out.println(t);
        }

        System.out.println("DST");

        for (ITree t: dstRoot.preOrder()){
            System.out.println(t);
        }

        init();
        for (Action action: originUnambiguousActionList){
            System.out.println(action);
        }
    }
}
