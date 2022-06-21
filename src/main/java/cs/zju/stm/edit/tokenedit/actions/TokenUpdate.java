package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.tree.ITree;

public class TokenUpdate extends TokenEditAction {

    public TokenUpdate(ITree srcStmt, ITree dstStmt){
        super(srcStmt, dstStmt);
        this.type = "UPDATE";
    }

    @Override
    public String getName() {
        return "Update: " + srcTokens.get(0) + " -> " + dstTokens.get(0);
    }
}
