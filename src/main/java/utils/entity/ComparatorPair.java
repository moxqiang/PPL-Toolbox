package utils.entity;

import java.util.Comparator;

public class ComparatorPair implements Comparator<Pair> {
    @Override
    public int compare(Pair o1, Pair o2) {
        if (o1.getStart() > o2.getStart()) {
            return 1;
        } else if (o1.getStart() < o2.getStart()) {
            return -1;
        } else {
            if (o1.getEnd() > o2.getEnd()) {
                return 1;
            } else if (o1.getEnd() < o2.getEnd()) {
                return -1;
            }
            return 0;
        }
    }
}
