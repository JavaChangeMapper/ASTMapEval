import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class testJava {
    public static void main(String[] args){
        Integer[] a = {1,2,3,4,5,6,7,8,9,10};
        List<Integer> aList = Arrays.asList(a);
        Random test = new Random(1);
        Collections.shuffle(aList, test);
        System.out.println(aList);
    }
}
