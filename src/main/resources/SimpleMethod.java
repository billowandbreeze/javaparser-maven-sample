import java.util.ArrayList;
import java.util.List;

public class SimpleMethod {
    public void simple() {
        Integer i = 10;
        Integer j = 1;
        String s = "string";
        j = i + j;
        System.out.println(s);
        System.out.println(j);
        List<Integer> list = new ArrayList<>();
        list.add(i);
        list.add(j);
        System.out.println(list);
    }
}
