package process.contact;


import errors.MyError;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

//    chr, start, end, readID, readStart, readEnd, readLength, mapq:AS, strand, mapping id(optional), status(optional)
public class ReadAT implements Comparable<ReadAT>{
    public String chr;
    public String phase="*";
    public long start;
    public long end;
    public String readName;
    public String readNameSplit;
    public int rS;
    public int rE;
    public int readLen;
    public String scoreS;
    public int mapq;
    public char strand;
    public String indexS = "-1";
    public int AS = -1;
    //optional
    public long index = -1;
    public String status="null";
    public String fragS="null";
    public long[][] frags = null;

    public ConfidenceLevel confidenceLevel = ConfidenceLevel.None;

    public ReadAT() {
    }

    public ReadAT(String[] atLine) {
        if (atLine.length<=9){
            MyError.colNumError(atLine.length);
        }
        else {
            String[] chrINFO = atLine[0].split(".Phase");
            this.chr = chrINFO[0];
            if (chrINFO.length>1){
                this.phase = chrINFO[1];
            }
            this.start = Integer.parseInt(atLine[1]);
            this.end = Integer.parseInt(atLine[2]);
            this.readNameSplit = atLine[3];
            this.readName = StringUtils.substringBeforeLast(atLine[3], "_");
            this.rS = Integer.parseInt(atLine[4]);
            this.rE = Integer.parseInt(atLine[5]);
            this.readLen = Integer.parseInt(atLine[6]);
            this.scoreS = atLine[7];
            String[] scores= scoreS.split(":");
            this.mapq = Integer.parseInt(scores[0]);
            if (scores.length >= 2){
                this.AS = Integer.parseInt(scores[1]);
            }
            this.strand = atLine[8].charAt(0);
            if (atLine.length >= 10) {
                this.indexS = atLine[9];
                this.index = Long.parseLong(atLine[9].split(",")[0]);
            }
            if (atLine.length >= 11) {
                this.status = atLine[10];
            }
            if (atLine.length >= 12) {
                this.fragS = atLine[11];
                if (!fragS.equals("null")){

                    this.frags = new long[this.fragS.split(",").length][3];
                    int num = 0;
                    for (String frag :
                            fragS.split(",")) {
                        //4760197:chr12-59042743-59042830,4760198:chr12-59042830-59042944,4760199:chr12-59042944-59044203
                        frags[num][0] = Long.parseLong(frag.split(":")[0]);
                        frags[num][1] = Long.parseLong(frag.split(":")[1].split("-")[1]);
                        frags[num][2] = Long.parseLong(frag.split(":")[1].split("-")[2]);
                        num++;
                    }
                }
            }

            if (atLine.length >= 13) {
                this.fragS = atLine[11];
                if (!fragS.equals("null")){

                    this.frags = new long[this.fragS.split(",").length][3];
                    int num = 0;
                    for (String frag :
                            fragS.split(",")) {
                        //4760197:chr12-59042743-59042830,4760198:chr12-59042830-59042944,4760199:chr12-59042944-59044203
                        frags[num][0] = Long.parseLong(frag.split(":")[0]);
                        frags[num][1] = Long.parseLong(frag.split(":")[1].split("-")[1]);
                        frags[num][2] = Long.parseLong(frag.split(":")[1].split("-")[2]);
                        num++;
                    }
                }
            }
        }
    }


    public ReadAT(String chr, long start, long end, String readName, int rS, int rE, int readLen, String scoreS, char strand) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.readName = readName;
        this.rS = rS;
        this.rE = rE;
        this.readLen = readLen;
        this.scoreS = scoreS;
        String[] scores= scoreS.split(":");
        this.mapq = Integer.parseInt(scores[0]);
        if (scores.length >= 2){
            this.AS = Integer.parseInt(scores[1]);
        }
        this.strand = strand;
    }


    public String toContactLine(){
        String atLine = chr + '\t'
        + start + '\t'
        + end + '\t'
        + readName + '\t'
        + rS + '\t'
        + rE + '\t'
        + readLen + '\t'
        + scoreS + '\t'
        + strand + '\t'
        + indexS + '\t'
        + status + '\t'
        + fragS;
        return atLine;
    }

    public String toContactLineWithPhase(){
        String atLine = chr + ".Phase" + this.phase + '\t'
                + start + '\t'
                + end + '\t'
                + readName + '\t'
                + rS + '\t'
                + rE + '\t'
                + readLen + '\t'
                + scoreS + '\t'
                + strand + '\t'
                + indexS + '\t'
                + status + '\t'
                + fragS;
        return atLine;
    }

    @Override
    public int compareTo(@NotNull ReadAT o) {
        if (this.index > o.index){
            return 1;
        }else if (this.index < o.index){
            return -2;
        }else {
            return 0;
        }
    }

    public Boolean isPhased() {
        if (this.getPhase().equalsIgnoreCase("0") ||
            this.getPhase().equalsIgnoreCase("*"))   return false;
        return true;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public void setPhase(int phase) {
        this.phase = phase+"";
    }

    public String getIndexS() {
        return indexS;
    }

    public void setIndexS(String indexS) {
        this.indexS = indexS;
    }

    public String getFragS() {
        return fragS;
    }

    public void setFragS(String fragS) {
        this.fragS = fragS;
    }

    public long[][] getFrags() {
        return frags;
    }

    public void setFrags(long[][] frags) {
        this.frags = frags;
    }

    public Long[] getRefinedBoundaries(){
        Long start = this.frags[0][1];
        Long end = this.frags[frags.length-1][2];
        Long[] boundaries = {start,end};
        return boundaries;
    }

    public void refineBoundaries(){
        // START modification
        Long start = this.frags[0][1];
        Long end = this.frags[frags.length-1][2];
        Long[] boundaries = {start,end};
        this.start = boundaries[0];
        this.end = boundaries[1];
    }

    public void calConfidenceLevel(int cutoff) {
        long le = this.frags[0][1];
        long re = this.frags[this.frags.length-1][2];

        long ld = Math.abs(this.start - le);
        long rd = Math.abs(this.end - re);

        if (ld < cutoff){
            if (rd < cutoff){
                this.confidenceLevel = ConfidenceLevel.LR;
            }else {
                this.confidenceLevel = ConfidenceLevel.L;
            }
        } else {
            if (rd < cutoff){
                this.confidenceLevel = ConfidenceLevel.R;
            }else {
                this.confidenceLevel = ConfidenceLevel.None;
            }
        }
    }

    public ConfidenceLevel getConfidenceLevel(int cutoff) {
        if (this.fragS.equals("null") || this.frags==null){
            System.out.println("WARNING: fragment information is empty, please check the inputfile, and run AssignFragment");
            return confidenceLevel;
        }
        calConfidenceLevel(cutoff);
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public int getMappingLen(){
        return this.rE-this.rS+1;
    }

    public ReadAT merge(ReadAT at){
//        Range<Long> rangeC1 = Range.closed(this.start, this.end-1);
//        Range<Long> rangeC2 = Range.closed(at.start, at.end-1);
//        Range<Integer> rangeR1 = Range.closed(this.rS, this.rE);
//        Range<Integer> rangeR2 = Range.closed(at.rS, at.rE);
//        Range<Long> newRangeC = rangeC1.;

        String newChr = at.chr;

        long newS = Math.min(at.start, this.start);
        long newE = Math.max(at.end, this.end);

        String newRN = at.readName;

        int newRs = Math.min(at.rS, this.rS);
        int newRe = Math.max(at.rE, this.rE);

        int newReadLen = at.readLen;

        String newScoreS = "-2:-2";

        char newStrand = at.strand;


        ReadAT newAt = new ReadAT(
                newChr,
                newS,
                newE,
                newRN,
                newRs,
                newRe,
                newReadLen,
                newScoreS,
                newStrand
        );

        newAt.indexS  = at.indexS+","+this.indexS;
        newAt.setStatus("merged");

        if (this.frags!=null && at.frags!=null){
            newAt.fragS="";
            Map<Long, Long[]> tmpFrags = new TreeMap<>();
            for (String frag :
                    this.fragS.split(",")) {
                //4760197:chr12-59042743-59042830,4760198:chr12-59042830-59042944,4760199:chr12-59042944-59044203
                tmpFrags.put(Long.parseLong(frag.split(":")[0]),
                        new Long[]{
                                Long.parseLong(frag.split(":")[1].split("-")[1]),
                                Long.parseLong(frag.split(":")[1].split("-")[2])
                        });
            }
            for (String frag :
                    at.fragS.split(",")) {
                //4760197:chr12-59042743-59042830,4760198:chr12-59042830-59042944,4760199:chr12-59042944-59044203
                tmpFrags.put(Long.parseLong(frag.split(":")[0]),
                        new Long[]{
                                Long.parseLong(frag.split(":")[1].split("-")[1]),
                                Long.parseLong(frag.split(":")[1].split("-")[2])
                        });
            }
            for (Map.Entry<Long,Long[]> e :
                    tmpFrags.entrySet()) {
                newAt.fragS+=e.getKey()+":"+newAt.getChr()+"-"+e.getValue()[0]+"-"+e.getValue()[1]+",";
            }
            newAt.fragS=StringUtils.substringBeforeLast(newAt.fragS, ",");

            newAt.frags = new long[newAt.fragS.split(",").length][3];
            int num = 0;
            for (String frag :
                    newAt.fragS.split(",")) {
                //4760197:chr12-59042743-59042830,4760198:chr12-59042830-59042944,4760199:chr12-59042944-59044203
                newAt.frags[num][0] = Long.parseLong(frag.split(":")[0]);
                newAt.frags[num][1] = Long.parseLong(frag.split(":")[1].split("-")[1]);
                newAt.frags[num][2] = Long.parseLong(frag.split(":")[1].split("-")[2]);
                num++;
            }
        }

        return newAt;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getReadName() {
        return readName;
    }

    public void setReadName(String readName) {
        this.readName = readName;
    }

    public String getReadNameSplit() {
        return readNameSplit;
    }

    public void setReadNameSplit(String readNameSplit) {
        this.readNameSplit = readNameSplit;
    }

    public int getrS() {
        return rS;
    }

    public void setrS(int rS) {
        this.rS = rS;
    }

    public int getrE() {
        return rE;
    }

    public void setrE(int rE) {
        this.rE = rE;
    }

    public int getReadLen() {
        return readLen;
    }

    public void setReadLen(int readLen) {
        this.readLen = readLen;
    }

    public String getScoreS() {
        return scoreS;
    }

    public void setScoreS(String scoreS) {
        this.scoreS = scoreS;
        String[] scores= scoreS.split(":");
        this.mapq = Integer.parseInt(scores[0]);
        if (scores.length >= 2){
            this.AS = Integer.parseInt(scores[1]);
        }
    }

    public int getMapq() {
        return mapq;
    }

    public void setMapq(int mapq) {
        this.mapq = mapq;
        this.scoreS = this.mapq+":"+this.AS;
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
        this.indexS = index + "";
    }

    public int getAS() {
        return AS;
    }

    public void setAS(int AS) {
        this.AS = AS;
        this.scoreS = this.mapq+":"+this.AS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMid(){
        return (this.start+this.end)/2;
    }

}
