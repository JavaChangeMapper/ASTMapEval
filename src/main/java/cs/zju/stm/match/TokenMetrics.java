package cs.zju.stm.match;

public class TokenMetrics {
    private int mapped = 0;
    private int sameTypeMapped = 0;
    private int sameStmtMapped = 0;
    private int sameValueMapped = 0;
    private int inLcsTokensMapped = 0;

    public TokenMetrics(){}

    public TokenMetrics(TokenMetrics metrics){
        this.mapped = metrics.mapped;
        this.sameTypeMapped = metrics.sameTypeMapped;
        this.sameValueMapped = metrics.sameValueMapped;
        this.sameStmtMapped = metrics.sameStmtMapped;
        this.inLcsTokensMapped = metrics.inLcsTokensMapped;
}

    public void setMapped(int mapped) {
        this.mapped = mapped;
    }

    public void setSameTypeMapped(int sameTypeMapped) {
        this.sameTypeMapped = sameTypeMapped;
    }

    public void setSameValueMapped(int sameValueMapped) {
        this.sameValueMapped = sameValueMapped;
    }

    public void setSameStmtMapped(int sameStmtMapped) {
        this.sameStmtMapped = sameStmtMapped;
    }

    public void setInLcsTokensMapped(int inLcsTokensMapped) {
        this.inLcsTokensMapped = inLcsTokensMapped;
    }

    public boolean getSameTypeMapped(){
        return mapped == 1 && sameTypeMapped == 1;
    }

    public boolean isDiffTypeDiffValueMapped(){
        return mapped == 1 && sameTypeMapped == 0 && sameValueMapped == 0;
    }

    public boolean isSameStmtAndValue(){
        return sameStmtMapped == 1 && sameValueMapped == 1;
    }

    public static int doCompare(TokenMetrics metrics1, TokenMetrics metrics2){
        if (metrics1 == null) {
            if (metrics2 != null && metrics2.getSameTypeMapped()) {
                return TokenComparisonType.TOKEN_NOT_MAPPED_IN_SAME_STMT;
            }
            return -1;
        }
        if (metrics1.isDiffTypeDiffValueMapped())
            return TokenComparisonType.CHANGE_TOKEN_TYPE_AND_VALUE;
        if (metrics2 == null)
            return -1;
        if (metrics2.isDiffTypeDiffValueMapped())
            return -1;
//        if (metrics1.sameStmtMapped == 0 && metrics1.mapped == 1)
//            metrics1.sameStmtMapped = -1;
//        if (metrics2.sameStmtMapped == 0 && metrics2.mapped == 1)
//            metrics2.sameStmtMapped = -1;
        if (metrics1.sameStmtMapped > metrics2.sameStmtMapped)
            return -1;
        if (metrics1.sameStmtMapped < metrics2.sameStmtMapped)
            return TokenComparisonType.TOKEN_NOT_MAPPED_IN_SAME_STMT;
        if (metrics1.isSameStmtAndValue() && !metrics2.isSameStmtAndValue())
            return -1;
        if (!metrics1.isSameStmtAndValue() && metrics2.isSameStmtAndValue())
            return TokenComparisonType.SHOULD_MAP_IDENTICAL_TOKEN;
        if (metrics1.isSameStmtAndValue() && metrics2.isSameStmtAndValue()) {
            if (metrics1.inLcsTokensMapped > metrics2.inLcsTokensMapped)
                return -1;
            if (metrics1.inLcsTokensMapped < metrics2.inLcsTokensMapped)
                return TokenComparisonType.CHANGE_POSITION_IN_STMT;
        }
        return 0;
    }
}
