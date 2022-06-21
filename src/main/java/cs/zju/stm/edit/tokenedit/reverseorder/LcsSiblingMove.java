package cs.zju.stm.edit.tokenedit.reverseorder;

import java.util.List;

public class LcsSiblingMove {
    public final List<Integer> movedTargetIdxes;
    public final String moveType;

    public LcsSiblingMove(List<Integer> movedTargetIdxes, String moveType){
        this.movedTargetIdxes = movedTargetIdxes;
        this.moveType = moveType;
    }

    @Override
    public String toString() {
        return "LcsSiblingMove{" +
                "movedTargetIdxes=" + movedTargetIdxes +
                ", moveType='" + moveType + '\'' +
                '}';
    }
}
