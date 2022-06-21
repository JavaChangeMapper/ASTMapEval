package cs.zju.treeutils;

import at.aau.softwaredynamics.gen.OptimizedJdtTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.cd.CdJdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import cs.zju.utils.Pair;

import java.io.*;
import java.util.*;

public class GumTreeUtil {

    private static ITree getITreeRoot(Reader reader, String matcherId){
        Run.initGenerators();
        ITree t = null;
        try {
            if (matcherId.equals("change-distiller")) {
                t = new CdJdtTreeGenerator().generate(reader).getRoot();
            } else if (matcherId.equals("ijm")) {
                t = new OptimizedJdtTreeGenerator().generate(reader).getRoot();
            } else {
                t = new JdtTreeGenerator().generate(reader).getRoot();
            }
        } catch(Exception e) {
            throw new GumTreeException(e.getMessage());
        }
        return t;
    }

    public static MappingStore getTreeMappings(ITree src, ITree dst) {
        return getTreeMappings(src, dst, null);
    }

    public static MappingStore getTreeMappings(ITree src, ITree dst, MappingStore ms){
        return getTreeMappings(src, dst, ms, "gumtree");
    }

    public static MappingStore getTreeMappings(ITree src, ITree dst, MappingStore ms, String matcherId){

        Matcher m = Matchers.getInstance().getMatcher(matcherId);
        if (matcherId != null && matcherId.equals("ijm"))
            m = new JavaMatchers.IterativeJavaMatcher_V2();
        MappingStore ms2;
        try {
            if (ms == null) {
                ms2 = m.match(src, dst);
            } else {
                MappingStore tmp = new MappingStore(ms);
                ms2 = m.match(src, dst, tmp);
            }
            return ms2;
        } catch (Exception | OutOfMemoryError e){
            e.printStackTrace();
            throw new GumTreeException(e.getMessage());
        }
    }

    public static ITree getITreeRoot(ByteArrayOutputStream stream, String matcherId) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        return getITreeRoot(reader, matcherId);
    }

    public static ITree getITreeRoot(String fileContent, String matcherId){
        Reader reader = new StringReader(fileContent);
        return getITreeRoot(reader, matcherId);
    }

    public static List<Action> getEditActions(MappingStore ms){
        if (ms == null)
            return null;
        EditScriptGenerator g = new ChawatheScriptGenerator();
        return g.computeActions(ms).asList();
    }

    public static String getNodeContent(String fileContent, ITree node){
        if (node == null)
            return "";
        int start = node.getPos();
        int end = node.getEndPos();
        if (start == -1)
            return "";
        return fileContent.substring(start, end);
    }

    public static ITree getMainTypeDecOfCompilationUnit(ITree cu){
        if (cu == null)
            return null;
        for (ITree t: cu.getChildren()){
            if (checkTypeDec(t))
                return t;
        }
        return null;
    }

    public static boolean checkTypeDec(ITree t){
        return CheckJDTNodeType.isTypeDec(t) || CheckJDTNodeType.isEnumDec(t) ||
                CheckJDTNodeType.isAnnotationTypeDec(t);
    }

    public static boolean isDirectElementOfNode(ITree t, ITree node){
        if (t == node)
            return false;
        ITree temp = t;
        while (temp != null && temp != node){
            if (CheckJDTNodeType.isStatementNode(temp))
                break;
            temp = temp.getParent();
        }
        return temp == node;
    }
}
