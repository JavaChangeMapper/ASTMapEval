package cs.zju.stm;

import com.github.gumtreediff.tree.ITree;
import cs.zju.utils.Pair;

import java.util.List;
import java.util.Map;

public class TokenRange extends Pair<Integer, Integer> {
    public TokenRange(Integer a, Integer b) {
        super(a, b);
    }

    public TokenRange(Pair<Integer, Integer> range){
        super(range.first, range.second);
    }

    public static boolean isEqualTo(TokenRange t1, TokenRange t2){
        if (t1 == t2)
            return true;
        if (t1 != null){
            return t1.equals(t2);
        }
        return false;
    }

    public String toPositionString(TreeTokensMap ttmap){
        ITree stmt = ttmap.getStmtOfTokenRange(this);
        Map<TokenRange, Integer> tokenIndexMap = ttmap.getTokenRangeIndexMapOfNode(stmt);
        int index = tokenIndexMap.get(this);
        int line = ttmap.getLineRangeOfStmt(stmt).first;
        return "Line:" + line + ", Index:" + index;
    }

    public String toString(TreeTokensMap ttMap){
        return ttMap.getTokenByRange(this);
    }

    public String toString(TreeTokensMap ttmap, boolean withStmtInfo){
        String value = ttmap.getTokenByRange(this);
        if (withStmtInfo)
            value += "(" + toPositionString(ttmap) + ")";
        return value;
    }
}
