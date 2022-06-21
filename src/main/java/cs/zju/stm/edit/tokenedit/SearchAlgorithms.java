package cs.zju.stm.edit.tokenedit;

import java.util.ArrayList;
import java.util.List;

public class SearchAlgorithms {
    public static int findIndexSmallerOrEqualPos(List<Integer> posIndexes, int pos){
        List<Integer> tmpIndexes = new ArrayList<>(posIndexes);
        if (tmpIndexes.get(0) > pos)
            return -1;
        if (tmpIndexes.size() == 1)
            return 0;
        if (tmpIndexes.size() == 2){
            int right = tmpIndexes.get(1);
            if (right > pos)
                return 0;
            else
                return 1;
        }
        int medIdx = tmpIndexes.size() / 2;
        List<Integer> leftIndexes = tmpIndexes.subList(0, medIdx);
        List<Integer> rightIndexes = tmpIndexes.subList(medIdx, posIndexes.size());
        int med = tmpIndexes.get(medIdx);
        int medRight = tmpIndexes.get(medIdx + 1);

        if (med <= pos && medRight > pos)
            return medIdx;

        if (med > pos)
            return findIndexSmallerOrEqualPos(leftIndexes, pos);
        else
            return medIdx + findIndexSmallerOrEqualPos(rightIndexes, pos);
    }

    public static int findIndexLargerOrEqualEndPos(List<Integer> posIndexes, int pos){
        List<Integer> tmpIndexes = new ArrayList<>(posIndexes);
        if (tmpIndexes.get(tmpIndexes.size()-1) < pos)
            return -1;
        if (tmpIndexes.size() == 1)
            return 0;
        if (tmpIndexes.size() == 2){
            int left = tmpIndexes.get(0);
            if (left >= pos)
                return 0;
            else
                return 1;
        }
        int medIdx = tmpIndexes.size() / 2;
        List<Integer> leftIndexes = tmpIndexes.subList(0, medIdx + 1);
        List<Integer> rightIndexes = tmpIndexes.subList(medIdx, posIndexes.size());
        int med = tmpIndexes.get(medIdx);
        int medRight = tmpIndexes.get(medIdx + 1);

        if (med < pos && medRight >= pos)
            return medIdx + 1;

        if (med >= pos)
            return findIndexLargerOrEqualEndPos(leftIndexes, pos);
        else
            return medIdx + findIndexLargerOrEqualEndPos(rightIndexes, pos);
    }
}
