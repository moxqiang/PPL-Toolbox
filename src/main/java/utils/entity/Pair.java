package utils.entity;

public class Pair {
    int start=-1;
    int end=-1;
    int score=-1;
    int endLinker=-1;

    public Pair() {
    }

    public Pair(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getEndLinker() {
        return endLinker;
    }

    public void setEndLinker(int endLinker) {
        this.endLinker = endLinker;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "start=" + start +
                ", end=" + end +
                ", score=" + score +
                '}';
    }
}
