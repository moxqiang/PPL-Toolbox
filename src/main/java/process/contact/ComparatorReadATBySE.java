package process.contact;

import java.util.Comparator;

//根据起始位置比较的比较器
public class ComparatorReadATBySE implements Comparator<ReadAT> {

    @Override
    public int compare(ReadAT o1, ReadAT o2) {
        if (o1.getReadName().compareTo(o2.getReadName())<0){
            return -1;
        }else if (o1.getReadName().compareTo(o2.getReadName())>0){
            return 1;
        }else {
            if (o1.rS > o2.rS) {
                return 1;
            } else if (o1.rS < o2.rS) {
                return -1;
            } else {
                if (o1.rE > o2.rE) {
                    return 1;
                } else if (o1.rE < o2.rE) {
                    return -1;
                }
                return 0;
            }
        }
    }
}
