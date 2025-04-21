package process.contact;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

//根据index排序的比较器
public class ComparatorReadAT implements Comparator<ReadAT> {

    @Override
    public int compare(ReadAT o1, ReadAT o2) {
        if (o1.index > o2.index){
            return 1;
        }else if (o1.index < o2.index){
            return -1;
        }else {
            return 0;
        }
    }
}
