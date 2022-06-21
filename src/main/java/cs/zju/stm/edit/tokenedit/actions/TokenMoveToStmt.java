package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.tree.ITree;

public class TokenMoveToStmt extends TokenEditAction {

    public TokenMoveToStmt(ITree srcStmt, ITree dstStmt) {
        super(srcStmt, dstStmt);
        this.type = "MOVE TO";
    }

    @Override
    public String getName(){
        return "Move from src stmt at " + srcWordsLine + " to this stmt: " + srcTokens;
    }
}
