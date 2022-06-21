package cs.zju.stm.match;

import cs.zju.stm.TokenRange;
import cs.zju.stm.TreeTokensMap;

import java.util.*;

public class TokenRangeTypeMap {
    private Map<TokenRange, String> tokenTypeMap;
    private Map<String, Set<String>> nameTypeMap;
    private List<TokenRange> tokens;

    // First phase: get all types except method receiver
    // Second phase: leverage the string map to get the type of remaining tokens
    public TokenRangeTypeMap(TreeTokensMap ttMap){
        tokenTypeMap = new HashMap<>();
        nameTypeMap = new HashMap<>();
        tokens = ttMap.getTokenRanges();

        for (TokenRange token: tokens){
            NameTypeCalculator calculator = new NameTypeCalculator(token, ttMap);
            String type = calculator.getTypeOfNode();
            String tokenStr = ttMap.getTokenByRange(token);

            tokenTypeMap.put(token, type);
            if (!nameTypeMap.containsKey(tokenStr))
                nameTypeMap.put(tokenStr, new HashSet<>());
            nameTypeMap.get(tokenStr).add(type);
        }

        for (TokenRange token: tokens){
            if (getTokenType(token) != null && !getTokenType(token).equals(NameTypeCalculator.QUALIFIED_PATH_NAME))
                continue;
            NameTypeCalculator calculator = new NameTypeCalculator(token, ttMap);
            String type = calculator.getTypeOfNodeFromNameTypeMap(nameTypeMap);
            if (type != null)
                tokenTypeMap.put(token, type);
        }
    }

    public String getTokenType(TokenRange token){
        return tokenTypeMap.get(token);
    }


    public String toString(TreeTokensMap ttMap) {
        String ret = "";
        for (TokenRange token: tokens){
            ret += token.toString(ttMap) + ": " + tokenTypeMap.get(token) + "\n";
        }
        return ret;
    }
}
