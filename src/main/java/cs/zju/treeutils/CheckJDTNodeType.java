package cs.zju.treeutils;

import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckJDTNodeType {

    private static final List<String> specialStmts = specialStmts();

    private static List<String> specialStmts(){
        String[] specialStmts = {
                "TypeDeclaration",
                "AnnotationTypeDeclaration",
                "AnnotationTypeMemberDeclaration",
                "EnumDeclaration",
                "EnumConstantDeclaration",
                "MethodDeclaration",
                "SwitchCase",
                "SuperConstructorInvocation",
                "ConstructorInvocation",
                "FieldDeclaration",
                "CatchClause",
                "Block",
                "Initializer"
        };
        return new ArrayList<>(Arrays.asList(specialStmts));
    }

    public static boolean isStatementNode(ITree t){
        String type = CheckJDTNodeType.getITreeNodeTypeName(t);
        if (type.endsWith("Statement"))
            return true;
        return specialStmts.contains(type);
    }


    public static boolean isJavaDoc(ITree node){
        return getITreeNodeTypeName(node).equals("Javadoc");
    }

    public static boolean isTypeDec(ITree node){
        return getITreeNodeTypeName(node).equals("TypeDeclaration");
    }

    public static boolean isMethodDec(ITree node){
        return getITreeNodeTypeName(node).equals("MethodDeclaration");
    }

    public static boolean isSimpleName(ITree node){
        return getITreeNodeTypeName(node).equals("SimpleName");
    }

    public static boolean isBlock(ITree node){
        return getITreeNodeTypeName(node).equals("Block");
    }

    public static boolean isCompilationUnit(ITree node){
        return getITreeNodeTypeName(node).equals("CompilationUnit");
    }

    public static boolean isImportDec(ITree node){
        return getITreeNodeTypeName(node).equals("ImportDeclaration");
    }

    public static boolean isPackageDec(ITree node){
        return getITreeNodeTypeName(node).equals("PackageDeclaration");
    }

    public static boolean isFieldDec(ITree node){
        return getITreeNodeTypeName(node).equals("FieldDeclaration");
    }

    public static boolean isAnonymousClassDec(ITree node){
        return getITreeNodeTypeName(node).equals("AnonymousClassDeclaration");
    }

    public static boolean isIfStatement(ITree node){
        return getITreeNodeTypeName(node).equals("IfStatement");
    }

    public static boolean isEnumDec(ITree node){
        return getITreeNodeTypeName(node).equals("EnumDeclaration");
    }

    public static boolean isAnnotationTypeDec(ITree node){
        return getITreeNodeTypeName(node).equals("AnnotationTypeDeclaration");
    }

    public static boolean isInfixExpression(ITree node){
        return getITreeNodeTypeName(node).equals("InfixExpression");
    }

    public static boolean isClassInstanceCreation(ITree node){
        return getITreeNodeTypeName(node).equals("ClassInstanceCreation");
    }

    public static boolean isStringLiteral(ITree node){
        return getITreeNodeTypeName(node).equals("StringLiteral");
    }

    public static boolean isCharacterLiteral(ITree node){
        return getITreeNodeTypeName(node).equals("CharacterLiteral");
    }

    public static boolean isNumberLiteral(ITree node) {
        return getITreeNodeTypeName(node).equals("NumberLiteral");
    }

    public static boolean isInfixExpressionOperator(ITree node){
        return getITreeNodeTypeName(node).equals("INFIX_EXPRESSION_OPERATOR");
    }

    public static boolean isModifier(ITree node){
        return getITreeNodeTypeName(node).equals("Modifier");
    }

    public static boolean isThisExpr(ITree node) {
        return getITreeNodeTypeName(node).equals("ThisExpression");
    }

    public static boolean isFieldAccess(ITree node){
        return getITreeNodeTypeName(node).equals("FieldAccess");
    }

    public static String getITreeNodeTypeName(ITree node){
        if (node == null || node.getType() == null || node.getType().name == null)
            return "";
        return node.getType().name;
    }

    public static boolean isStatement(String type){
        if (type.endsWith("Statement"))
            return true;
        return specialStmts.contains(type);
    }
}
