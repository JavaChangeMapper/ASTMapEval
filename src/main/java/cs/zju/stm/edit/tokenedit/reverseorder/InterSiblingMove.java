package cs.zju.stm.edit.tokenedit.reverseorder;

import java.util.ArrayList;
import java.util.List;

public class InterSiblingMove {
    public List<Integer> leftList;
    public List<Integer> rightList;
    public String movedList;

    public InterSiblingMove(){
        leftList = new ArrayList<>();
        rightList = new ArrayList<>();
    }

    public void setMoveType(String moveType){

    }

    @Override
    public String toString() {
        return "InterSiblingMove{" +
                "leftList=" + leftList +
                ", rightList=" + rightList +
                '}';
    }
}