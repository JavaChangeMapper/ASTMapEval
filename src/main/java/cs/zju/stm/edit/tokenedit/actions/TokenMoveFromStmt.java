package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.tree.ITree;

public class TokenMoveFromStmt extends TokenEditAction {

    public TokenMoveFromStmt(ITree srcStmt, ITree dstStmt) {
        super(srcStmt, dstStmt);
        this.type = "MOVE FORM";
    }


    @Override
    public String getName() {
        return "Move from this stmt to dst stmt at " + dstWordsLine + ": " + srcTokens;
    }
}
