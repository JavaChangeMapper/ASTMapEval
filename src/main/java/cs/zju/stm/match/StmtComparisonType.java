package cs.zju.stm.match;

public class StmtComparisonType {
    public static final int BOTH_TOKEN_BAD_MATCH = 18;
    public static final int DST_TOKEN_BAD_MATCH = 17;
    public static final int SRC_TOKEN_BAD_MATCH = 16;
    public static final int BOTH_CHANGE_TYPE_AND_VALUE = 15;
    public static final int DST_CHANGE_TYPE_AND_VALUE = 14;
    public static final int SRC_CHANGE_TYPE_AND_VALUE = 13;
    public static final int LESS_TOKEN_SAME_STMT = 12; // we can find more tokens in the same statement matched
    public static final int MORE_TOKEN_SAME_STMT = -12;
    public static final int LESS_LCS_TOKEN = 11;
    public static final int MORE_LCS_TOKEN = -11;
    public static final int BOTH_REFERENCE_ERROR = 10;
    public static final int DST_REFERENCE_ERROR = 9;
    public static final int SRC_REFERENCE_ERROR = 8;
    public static final int NOT_ENOUGH_IDENTICAL_TOKEN = 7; // stmt match badly, the algorithm matches two statements but they cannot be matched.
    public static final int ENOUGH_TOKEN_UNMATCHED = 6;  // two statements have enough identical tokens but they are not matched.
    public static final int BAD_MATCHED_BLOCK = 5; // badly matched block
    public static final int WELL_MATCHED_BLOCK = -5;
    public static final int LESS_IDENTICAL_TOKEN = 4; // we can find more identical tokens matched
    public static final int MORE_IDENTICAL_TOKEN = -4;
    public static final int MORE_UNMATCHED_TOKEN_IN_STMT = 3;
    public static final int LESS_UNMATCHED_TOKEN_IN_STMT = -3;
    public static final int WORSE_ANCESTOR_MATCHED = 2;
    public static final int BETTER_ANCESTOR_MATCHED = -2;
    public static final int WORSE_ORDER_MATCHED = 1;
    public static final int BETTER_ORDER_MATCHED = -1;
    public static final int CANNOT_COMPARE = 0;

    public static String getComparisonType(int type){
        switch(type){
            case LESS_IDENTICAL_TOKEN:
                return "More identical token can be matched";
            case MORE_IDENTICAL_TOKEN:
            case MORE_TOKEN_SAME_STMT:
            case BETTER_ANCESTOR_MATCHED:
            case BETTER_ORDER_MATCHED:
            case MORE_LCS_TOKEN:
            case WELL_MATCHED_BLOCK:
            case LESS_UNMATCHED_TOKEN_IN_STMT:
                return "Good";
            case LESS_LCS_TOKEN:
                return "Longer sequence of tokens can be matched";
            case LESS_TOKEN_SAME_STMT:
                return "More token can be matched in the same stmt";
            case WORSE_ANCESTOR_MATCHED:
                return "The stmt can be matched to a statement with better position (Parent)";
            case WORSE_ORDER_MATCHED:
                return "The stmt can be matched to a statement with better position (LCS)";
            case BAD_MATCHED_BLOCK:
                return "The block is badly matched";
            case MORE_UNMATCHED_TOKEN_IN_STMT:
                return "More tokens are not matched in the same statement";
            case SRC_CHANGE_TYPE_AND_VALUE:
                return "Change type and value for a token in src statement";
            case DST_CHANGE_TYPE_AND_VALUE:
                return "Change type and value for a token in dst statement";
            case SRC_TOKEN_BAD_MATCH:
                return "Token badly match in src statement";
            case DST_TOKEN_BAD_MATCH:
                return "Token badly match in dst statement";
            case BOTH_TOKEN_BAD_MATCH:
                return "Token badly match in both statement";
            default:
                return "";
        }
    }

    public static int getSimpleErrorType(int type){
        if (type > 0)
            return type;
        return -1;
    }
}
