package cs.zju.stm.edit.tokenedit.actions;

import com.github.gumtreediff.tree.ITree;

public class TokenInsert extends TokenEditAction {

    public TokenInsert(ITree srcStmt, ITree dstStmt){
        super(srcStmt, dstStmt);
        this.type = "INSERT";
    }

    @Override
    public String getName() {
        return "Add: " + dstTokens.get(0);
    }
}
