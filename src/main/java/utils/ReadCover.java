package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import errors.MyError;

//将coverage文件的一行转换成为一个对象
public class ReadCover {
    String readName = "";
    int readLen = 0;
    int mappedLen = 0;
    double coverage = 0;
    RangeSet<Integer> rangeSet = TreeRangeSet.create();
    RangeSet<Integer> rangeSetDiff = TreeRangeSet.create();

    public ReadCover() {
    }

    public ReadCover(String[] fields){
        if (fields.length!=5){
            System.out.println(String.join(",",fields));
            MyError.colNumError(fields.length);
        }
        this.readName=fields[0];
        this.readLen=Integer.parseInt(fields[1]);
        this.mappedLen=Integer.parseInt(fields[2]);
        this.coverage=Double.parseDouble(fields[3]);
        for (String regionS : fields[4].split(",")){
            regionS=regionS.substring(1,regionS.length()-1);
//            System.out.println(regionS);
            int start = Integer.parseInt(regionS.split("\\.\\.")[0]);
            int end = Integer.parseInt(regionS.split("\\.\\.")[1]);
            this.rangeSet.add(Range.closedOpen(
                start,end
            ));
        }
        //差集
        rangeSetDiff.add(Range.closedOpen(0,readLen));
        rangeSetDiff.removeAll(rangeSet);
        rangeSet.clear();

    }

    public RangeSet<Integer> getRangeSet() {
        return rangeSet;
    }

    public void setRangeSet(RangeSet<Integer> rangeSet) {
        this.rangeSet = rangeSet;
    }

    public RangeSet<Integer> getRangeSetDiff() {
        return rangeSetDiff;
    }

    public void setRangeSetDiff(RangeSet<Integer> rangeSetDiff) {
        this.rangeSetDiff = rangeSetDiff;
    }

    public String getReadName() {
        return readName;
    }

    public void setReadName(String readName) {
        this.readName = readName;
    }

    public int getReadLen() {
        return readLen;
    }

    public void setReadLen(int readLen) {
        this.readLen = readLen;
    }

    public int getMappedLen() {
        return mappedLen;
    }

    public void setMappedLen(int mappedLen) {
        this.mappedLen = mappedLen;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }
}
