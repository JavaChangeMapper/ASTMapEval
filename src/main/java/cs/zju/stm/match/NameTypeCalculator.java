package cs.zju.stm.match;

import com.github.gumtreediff.tree.ITree;
import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;
import cs.zju.treeutils.CheckJDTNodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class NameTypeCalculator {
    public static final String TYPE_NAME = "TYPE";
    public static final String VAR_NAME = "VAR";
    public static final String METHOD_NAME = "METHOD";
    public static final String TYPE_DEC_NAME = "TYPE_DEC";
    public static final String METHOD_DEC_NAME = "METHOD_DEC";
    public static final String FIELD_DEC_NAME = "FIELD_DEC";
    public static final String ANNOTATION_NAME = "ANNOTATION";
    public static final String QUALIFIED_PATH_NAME = "PATH";

    private ITree nameNode;
    private ITree typeNode;
    private String typeNodeName;
    private TokenRange token;
    private TreeTokensMap ttMap;


    public NameTypeCalculator(TokenRange token, TreeTokensMap ttMap){
        this.token = token;
        this.ttMap = ttMap;
        this.nameNode = ttMap.getTokenRangeTreeMap().get(token);
        ITree t = nameNode;
        while(CheckJDTNodeType.getITreeNodeTypeName(t).equals("SimpleName")){
            t = t.getParent();
        }
        typeNode = t;
        typeNodeName = typeNode.getType().name;
    }

    public String getTypeOfNodeFromNameTypeMap(Map<String, Set<String>> nameTypeMap){
        String tokenStr = ttMap.getTokenByRange(token);
        Set<String> typeSet = nameTypeMap.get(tokenStr);
        if (typeSet.contains(TYPE_DEC_NAME))
            return TYPE_NAME;
        if (typeSet.contains(TYPE_NAME))
            return TYPE_NAME;
        if (typeSet.contains(VAR_NAME))
            return VAR_NAME;
        if (tokenStr.charAt(0) == tokenStr.toUpperCase().charAt(0))
            return TYPE_NAME;
        else
            return VAR_NAME;
    }

    public String getTypeOfNode(){
        if (!nameNode.getType().name.endsWith("Name"))
            return nameNode.getType().name;

        if (typeNodeName.equals("QualifiedName")){
            List<TokenRange> tokens = ttMap.getTokenRangesOfNode(typeNode);
            if (!tokens.get(tokens.size() - 1).equals(token))
                return QUALIFIED_PATH_NAME;
            ITree t = typeNode;
            while(CheckJDTNodeType.getITreeNodeTypeName(t).endsWith("Name")){
                t = t.getParent();
            }
            typeNode = t;
            typeNodeName = typeNode.getType().name;
        }

        if (isAnnotation())
            return ANNOTATION_NAME;
        if (isVar())
            return VAR_NAME;
        if (isType())
            return TYPE_NAME;
        if (isMethod())
            return METHOD_NAME;
        if (isMethodDec())
            return METHOD_DEC_NAME;
        if (isTypeDec())
            return TYPE_DEC_NAME;
        if (isFieldDec())
            return FIELD_DEC_NAME;

        if (typeNodeName.equals("SuperConstructorInvocation")){
            return getTokenTypeFromSuperConstructorInvocation(typeNode);
        }

        if (typeNodeName.equals("SuperMethodInvocation")){
            return getTokenTypeFromSuperMethodInvocation(typeNode);
        }

        return null;
    }

    private boolean isAnnotation(){
        if (typeNodeName.endsWith("Annotation"))
            return true;
        return false;
    }

    private boolean isType(){
        if (typeNodeName.endsWith("Type"))
            return true;
        if (typeNodeName.equals("ThisExpression"))
            return true;
        if (typeNodeName.equals("ArrayCreation"))
            return true;
        if (typeNodeName.equals("ImportDeclaration"))
            return true;

        return false;
    }

    public boolean isVar(){
        if (typeNodeName.equals("METHOD_INVOCATION_ARGUMENTS"))
            return true;
        if (typeNodeName.equals("FieldAccess"))
            return true;
        if (typeNodeName.equals("SuperFieldAccess"))
            return !isTypeOrVarForSuperFieldAccess(typeNode);
        if (typeNodeName.equals("ParenthesizedExpression"))
            return true;
        if (typeNodeName.equals("PostfixExpression"))
            return true;
        if (typeNodeName.equals("ConditionalExpression"))
            return true;
        if (typeNodeName.equals("PrefixExpression"))
            return true;
        if (typeNodeName.equals("InfixExpression"))
            return true;
        if (typeNodeName.equals("VariableDeclarationFragment"))
            return true;
        if (typeNodeName.equals("SingleVariableDeclaration"))
            return true;
        if (typeNodeName.equals("InstanceofExpression"))
            return true;
        if (typeNodeName.equals("CastExpression"))
            return true;
        if (typeNodeName.equals("Assignment"))
            return true;
        if (typeNodeName.equals("ArrayAccess"))
            return true;
        if (typeNodeName.equals("EnhancedForStatement"))
            return true;
        if (typeNodeName.equals("ConstructorInvocation"))
            return true;
        if (typeNodeName.equals("ReturnStatement"))
            return true;
        if (typeNodeName.equals("ClassInstanceCreation"))
            return true;
        return false;
    }

    public boolean isMethod(){
        if (typeNodeName.equals("MethodInvocation"))
            return true;
        if (typeNodeName.equals("SuperMethodInvocation")){
            return true;
        }
        return false;
    }

    public boolean isTypeDec(){
        if (typeNodeName.equals("TypeDeclaration") || typeNodeName.equals("EnumDeclaration") ||
                typeNodeName.equals("AnnotationTypeDeclaration"))
            return true;
        return false;
    }

    public boolean isMethodDec(){
        if (typeNodeName.equals("MethodDeclaration"))
            return true;
        return false;
    }

    public boolean isFieldDec(){
        if (typeNodeName.equals("FieldDeclaration"))
            return true;
        return false;
    }

    private boolean isTypeOrVarForSuperFieldAccess(ITree superFieldAccessNode){
        List<ITree> children = superFieldAccessNode.getChildren();
        if (children.size() == 1)
            return false;
        if (children.size() == 2){
            if (children.get(0) == nameNode)
                return true;
            if (children.get(1) == nameNode)
                return false;
        }
        throw new RuntimeException("Cannot handle more children for superFieldAccess");
    }

    private String getTokenTypeFromSuperMethodInvocation(ITree superMethodInvocationNode){
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(superMethodInvocationNode);
        List<String> tokenStrings = ttMap.getTokensByRanges(tokens);
        int index = tokenStrings.indexOf("super");
        TokenRange superRange = tokens.get(index);
        if (token.second < superRange.first)
            return TYPE_NAME;
        else {
            List<ITree> children = superMethodInvocationNode.getChildren();
            for (ITree t: children){
                if (t.getPos() > superRange.second){
                    if (CheckJDTNodeType.isSimpleName(t) && nameNode == t)
                        return METHOD_NAME;
                }
            }
        }
        return VAR_NAME;
    }

    private String getTokenTypeFromSuperConstructorInvocation(ITree superConsInvNode){
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(superConsInvNode);
        List<String> tokenStrings = ttMap.getTokensByRanges(tokens);
        int index = tokenStrings.indexOf("super");
        TokenRange superRange = tokens.get(index);
        if (token.second < superRange.first)
            return TYPE_NAME;
        else
            return VAR_NAME;
    }
}
