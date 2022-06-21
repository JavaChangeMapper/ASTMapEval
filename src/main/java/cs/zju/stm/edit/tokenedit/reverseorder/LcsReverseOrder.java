package cs.zju.stm.edit.tokenedit.reverseorder;

import cs.zju.stm.edit.sequence.MySequenceAlgorithm;

import java.util.*;

public class LcsReverseOrder {
    private List<Integer> list;
    private List<Integer> reorderList;
    private List<Integer> targetSortList;

    private int moveEdits = 0;
    private List<LcsSiblingMove> moveList;
    private List<Integer> targetLeaveNumbers;

    public LcsReverseOrder(List<Integer> list, int targetStmtLeafSize){
        this.list = list;
        reorderList = new ArrayList<>(list);
        targetSortList = new ArrayList<>(list);

        Collections.sort(targetSortList);
        targetLeaveNumbers = new ArrayList<>();
        moveList = new ArrayList<>();
        for (int i = 0 ; i < targetStmtLeafSize; i++)
            targetLeaveNumbers.add(i);
        moveEdits = getMoveOpNumbers();
    }

    public int getMoveEdits() {
        return moveEdits;
    }

    private int getMoveOpNumbers(){
        if (list.size() == 0)
            return 0;
        if (list.size() == 1)
            return 0;
        List<int[]> lcs = lcs(reorderList, targetSortList);
        if (lcs.size() == targetSortList.size())
            return 0;
        List<List<Integer>> consecutiveNums = new ArrayList<>();
        List<String> types = new ArrayList<>();
        findConsecutiveNumbersNotMapped(lcs, consecutiveNums, types);
        int idx = 0;
        for (List<Integer> nums: consecutiveNums){
            String type = types.get(idx);
            moveList.add(new LcsSiblingMove(nums, type));
            idx ++;
        }
        return moveList.size();
    }

    private void findConsecutiveNumbersNotMapped(List<int[]> lcs, List<List<Integer>> consecutiveNums,
                                                 List<String> types) {
        Map<Integer, Integer> mappedIdxes = new HashMap<>();
        for (int[] indexPair: lcs){
            mappedIdxes.put(indexPair[0], indexPair[1]);
        }
        List<Integer> tmpConsecutiveNums = new ArrayList<>();
        for (int i = 0; i < reorderList.size(); i++){
            if (mappedIdxes.containsKey(i)){
                int curNum = reorderList.get(i);
                if (tmpConsecutiveNums.size() > 0) {
                    consecutiveNums.add(tmpConsecutiveNums);
                    if (tmpConsecutiveNums.get(tmpConsecutiveNums.size() - 1) > curNum)
                        types.add("right");
                    else
                        types.add("left");
                    tmpConsecutiveNums = new ArrayList<>();
                }
                continue;
            }
            int curNum = reorderList.get(i);
            if (tmpConsecutiveNums.size() > 0){
                if (tmpConsecutiveNums.get(tmpConsecutiveNums.size() - 1) == curNum - 1)
                    tmpConsecutiveNums.add(curNum);
                else{
                    consecutiveNums.add(tmpConsecutiveNums);
                    int nextMappedNum = getNextMappedNum(mappedIdxes, i);
                    if (nextMappedNum == -1)
                        types.add("left");
                    else if (tmpConsecutiveNums.get(tmpConsecutiveNums.size() - 1) > nextMappedNum)
                        types.add("right");
                    else
                        types.add("left");
                    tmpConsecutiveNums = new ArrayList<>();
                    tmpConsecutiveNums.add(curNum);
                }
            } else {
                tmpConsecutiveNums.add(curNum);
            }
        }

        if (tmpConsecutiveNums.size() > 0){
            consecutiveNums.add(tmpConsecutiveNums);
            types.add("left");
        }
    }

    private int getNextMappedNum(Map<Integer, Integer> map, int idx){
        for (int i = idx + 1; i < reorderList.size(); i++){
            if (map.containsKey(i))
                return reorderList.get(i);
        }
        return -1;
    }

    private static List<int[]> lcs(List<Integer> srcNums, List<Integer> dstNums){
        int[][] lengths = new int[srcNums.size() + 1][dstNums.size() + 1];
        for (int i = 0; i < srcNums.size(); i++){
            for (int j = 0; j < dstNums.size(); j++){
                if (srcNums.get(i).equals(dstNums.get(j)))
                    lengths[i+1][j+1] = lengths[i][j] + 1;
                else
                    lengths[i+1][j+1] = Math.max(lengths[i+1][j], lengths[i][j+1]);
            }
        }
        return MySequenceAlgorithm.extractIndexes(lengths, srcNums.size(), dstNums.size());
    }

    public List<List<Integer>> getInsertedOrRemovedNumberList(Set<Integer> mappedEleIndexes){
        List<List<Integer>> ret = new ArrayList<>();
        reorderList = targetSortList;
        int targetStmtSize = targetLeaveNumbers.size();

        if (reorderList.size() == 0){
            ret.add(targetLeaveNumbers);
        }

        for (int i = 0; i < reorderList.size(); i++){
            int idx = reorderList.get(i);
            if (i == 0 && idx > 0){
                List<List<Integer>> numberList = getNumberList(targetLeaveNumbers.subList(0, idx),
                        mappedEleIndexes);
                ret.addAll(numberList);
            }
            if (i < reorderList.size() -  1){
                int nextIdx = reorderList.get(i + 1);
                if (nextIdx > idx + 1) {
                    List<List<Integer>> numberList = getNumberList(targetLeaveNumbers.subList(idx + 1, nextIdx),
                            mappedEleIndexes);
                    ret.addAll(numberList);
                }
            }
            if (i == reorderList.size() - 1){
                if (idx < targetStmtSize - 1) {
                    List<List<Integer>> numberList = getNumberList(targetLeaveNumbers.subList(idx + 1, targetStmtSize),
                            mappedEleIndexes);
                    ret.addAll(numberList);
                }
            }
        }
        return ret;
    }

    public List<List<Integer>> getNumberList(List<Integer> numbers, Set<Integer> mappedEleIndexes){
        List<List<Integer>> ret = new ArrayList<>();
        List<Integer> tmp = new ArrayList<>();
        for (int num: numbers){
            if (mappedEleIndexes.contains(num)){
                if (tmp.size() != 0){
                    ret.add(new ArrayList<>(tmp));
                    tmp = new ArrayList<>();
                }
            } else {
                tmp.add(num);
            }
        }
        if (tmp.size() > 0)
            ret.add(tmp);
        return ret;
    }

    public List<LcsSiblingMove> getReverseOrderMoveList(){
        return moveList;
    }
}
