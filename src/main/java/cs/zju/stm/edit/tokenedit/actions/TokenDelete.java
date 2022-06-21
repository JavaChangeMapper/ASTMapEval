package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.tree.ITree;

public class TokenDelete extends TokenEditAction {

    public TokenDelete(ITree srcStmt, ITree dstStmt) {
        super(srcStmt, dstStmt);
        this.type = "DELETE";
    }

    @Override
    public String getName() {
        return "Delete: " + srcTokens.get(0);
    }

}
