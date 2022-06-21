package cs.zju.stm.edit.tokenedit.actions;


import com.github.gumtreediff.tree.ITree;

public class TokenInStmtMove extends TokenEditAction {

    public TokenInStmtMove(ITree srcStmt, ITree dstStmt) {
        super(srcStmt, dstStmt);
        this.type = "IN STMT MOVE";
    }

    @Override
    public String getName() {
        String moveDirection = this.moveDirection;
        String from = "left";
        if (moveDirection.equals("left"))
            from = "right";
        return "Intra-Stmt Move from " + from + " to " + moveDirection + ": "  + srcTokens;
    }
}
