package cs.zju.stm.edit;


import java.util.ArrayList;
import java.util.List;

public class EditUtils<T> {
    List<T> allElements;
    List<T> mappedElements;

    public EditUtils(List<T> allElements, List<T> mappedElements){
        this.allElements = allElements;
        this.mappedElements = mappedElements;
    }

    public List<List<Integer>> getConsecutiveIndexesOfMappedElements(){
        List<List<Integer>> list = new ArrayList<>();
        List<Integer> allIndexes = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < allElements.size(); i++){
            T ele = allElements.get(i);
            if (!mappedElements.contains(ele)){
                indexes.add(i);
            }
            allIndexes.add(i);
        }

        for (int i = 0; i < indexes.size(); i++){
            int idx = indexes.get(i);
            if (i == 0 && idx > 0){
                list.add(allIndexes.subList(0, idx));
            }
            if (i < indexes.size() - 1){
                int idx2 = indexes.get(i + 1);
                if (idx + 1 < idx2){
                    list.add(allIndexes.subList(idx + 1, idx2));
                }
            }
            if (i == indexes.size() - 1){
                int size = allIndexes.size();
                if (idx + 1 < size){
                    list.add(allIndexes.subList(idx + 1, size));
                }
            }
        }
        return list;
    }

}
