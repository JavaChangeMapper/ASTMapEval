package cs.zju.stm.match;

public class TokenComparisonType {
    public final static int CHANGE_TOKEN_TYPE_AND_VALUE = 4;
    public final static int TOKEN_NOT_MAPPED_IN_SAME_STMT = 3;
    public final static int SHOULD_MAP_IDENTICAL_TOKEN = 2;
    public final static int CHANGE_POSITION_IN_STMT = 1;

    public static String getComparisonTypeString(int type) {
        switch (type) {
            case CHANGE_POSITION_IN_STMT:
                return "Position of the token is changed";
            case TOKEN_NOT_MAPPED_IN_SAME_STMT:
                return "The token can be mapped in the same statement";
            case CHANGE_TOKEN_TYPE_AND_VALUE:
                return "Change type and value of a token";
            case SHOULD_MAP_IDENTICAL_TOKEN:
                return "Should map identical token";
            default:
                return "";
        }
    }
}
