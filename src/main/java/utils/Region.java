package utils;

public class Region {
    String chr;
    long start;
    long end;

    public Region(String chr, long start, long end) {
        this.chr = chr;
        this.start = start;
        this.end = end;
    }

    public Region(String chr, String start, String end) {
        this.chr = chr;
        this.start = Long.parseLong(start);
        this.end = Long.parseLong(end);
    }

    public Region(String line) {
        String[] fields = line.trim().split(":");
        this.chr = fields[0];
        String[] pos = fields[1].split("-");
        this.start = Long.parseLong(pos[0]);
        this.end = Long.parseLong(pos[1]);
    }

    public String toString(){
        return this.chr+"\t"+this.start+"\t"+this.end;
    }

    public String toStringNoTAB(){
        return this.chr+":"+this.start+"-"+this.end;
    }

    public String getChr() {
        return chr;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
