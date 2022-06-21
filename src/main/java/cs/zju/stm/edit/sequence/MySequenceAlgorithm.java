package cs.zju.stm.edit.sequence;

import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySequenceAlgorithm {

    public static List<int[]> longestCommonSubsequenceForTokens(List<String> srcWords, List<String> dstWords){
        int[][] lengths = new int[srcWords.size() + 1][dstWords.size() + 1];
        for (int i = 0; i < srcWords.size(); i++){
            for (int j = 0; j < dstWords.size(); j++){
                if (srcWords.get(i).equals(dstWords.get(j)))
                    lengths[i+1][j+1] = lengths[i][j] + 1;
                else
                    lengths[i+1][j+1] = Math.max(lengths[i+1][j], lengths[i][j+1]);
            }
        }
        return extractIndexes(lengths, srcWords.size(), dstWords.size());
    }

    public static List<int[]> longestCommonSubsequenceForITree(List<ITree> srcLeaves, List<ITree> dstLeaves){
        int[][] lengths = new int[srcLeaves.size() + 1][dstLeaves.size() + 1];
        for (int i = 0; i < srcLeaves.size(); i++){
            for (int j = 0; j < dstLeaves.size(); j++){
                if (srcLeaves.get(i).hasSameTypeAndLabel(dstLeaves.get(j)))
                    lengths[i+1][j+1] = lengths[i][j] + 1;
                else
                    lengths[i+1][j+1] = Math.max(lengths[i+1][j], lengths[i][j+1]);
            }
        }
        return extractIndexes(lengths, srcLeaves.size(), dstLeaves.size());
    }

    public static List<int[]> extractIndexes(int[][] lengths, int length1, int length2) {
        List<int[]> indexes = new ArrayList<>();

        for (int x = length1, y = length2; x != 0 && y != 0; ) {
            if (lengths[x][y] == lengths[x - 1][y]) x--;
            else if (lengths[x][y] == lengths[x][y - 1]) y--;
            else {
                indexes.add(new int[] {x - 1, y - 1});
                x--;
                y--;
            }
        }
        Collections.reverse(indexes);
        return indexes;
    }
}
