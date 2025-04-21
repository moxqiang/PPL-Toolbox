package process.mapping;


import errors.MyError;
import org.apache.commons.lang3.StringUtils;

public class ReadBed {
    public String chr;
    public long start;
    public long end;
    public String readName;
    public String scoreS;
    public int mapq;
    public int AS=-1;
    public char strand;
    public String cigar;
    public int readLen;
    public int rS;
    public int rE;

    public ReadBed(String[] bedLine) {
        if (bedLine.length<7){
            MyError.BedError(bedLine.length);
        }
        this.chr = bedLine[0];
        this.start = Integer.parseInt(bedLine[1]);
        this.end = Integer.parseInt(bedLine[2]);
        this.readName = bedLine[3];
        this.scoreS = bedLine[4];
        String[] scores = bedLine[4].split(":");
        this.mapq = Integer.parseInt(scores[0]);
        if (scores.length>=2) {
            this.AS = Integer.parseInt(scores[1]);
        }
        this.strand = bedLine[5].charAt(0);
        this.cigar = bedLine[6];
        this.readLen = getReadlen();
        if (this.strand=='+') {
            this.rS = getReadStart();
            this.rE = getReadEnd();
        } else if (this.strand=='-'){
            this.rS = getReadEnd();
            this.rE = getReadStart();
        } else if (this.strand=='*'){
            this.rS = getReadStart();
            this.rE = getReadEnd();
        } else {
            MyError.strandError(this.strand+"");
        }
    }

    //针对split类型的reads长读计算问题
    public ReadBed(String[] bedLine, String split) {
        if (bedLine.length < 7) {
            MyError.BedError(bedLine.length);
        }
        this.chr = bedLine[0];
        this.start = Integer.parseInt(bedLine[1]);
        this.end = Integer.parseInt(bedLine[2]);
        this.scoreS = bedLine[4];
        String[] scores = bedLine[4].split(":");
        this.mapq = Integer.parseInt(scores[0]);
        if (scores.length >= 2) {
            this.AS = Integer.parseInt(scores[1]);
        }
        this.strand = bedLine[5].charAt(0);
        this.cigar = bedLine[6];
        if (split.equalsIgnoreCase("N")) {
            this.readName = bedLine[3];
            this.readLen = getReadlen();
            if (this.strand=='+') {
                this.rS = getReadStart();
                this.rE = getReadEnd();
            } else if (this.strand=='-'){
                this.rS = getReadEnd();
                this.rE = getReadStart();
            } else if (this.strand=='*'){
                this.rS = getReadStart();
                this.rE = getReadEnd();
            } else {
                MyError.strandError(this.strand+"");
            }
        } else {
            this.readName = StringUtils.substringBeforeLast(bedLine[3], "_");
            this.readLen = getReadlen();
            if (this.strand=='+') {
                this.rS = getReadStart();
                this.rE = getReadEnd();
            } else if (this.strand=='-'){
                this.rS = getReadEnd();
                this.rE = getReadStart();
            } else if (this.strand=='*'){
                this.rS = getReadStart();
                this.rE = getReadEnd();
            } else {
                MyError.strandError(this.strand+"");
            }
            //获取该部分的read length
            //依赖于片段的readLen
            String readInfo = StringUtils.substringAfterLast(bedLine[3],"_");
            this.rS = Integer.parseInt(readInfo.split("-")[0])+rS;
            this.rE = Integer.parseInt(readInfo.split("-")[0])+rE;
            this.readLen = Integer.parseInt(readInfo.split("-")[2]);

        }


    }

    public ReadBed(String chr, long start, long end, String readName, int mapq, char strand, String cigar) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.readName = readName;
        this.mapq = mapq;
        this.strand = strand;
        this.cigar = cigar;
    }

    private int getReadlen() {
        if(cigar==null) return 0;
        String readseq = "";
        int readlen = 0;
        for(int i=0;i<cigar.length();i++) {
            if(cigar.charAt(i) >=48 && cigar.charAt(i) <=57) {
                readseq += cigar.charAt(i);
            }else if(cigar.charAt(i) == 'M') {
                readlen += Integer.parseInt(readseq);
                readseq = "";
            }else if(cigar.charAt(i) == 'I') {
                readlen += Integer.parseInt(readseq);
                readseq = "";
            }else if(cigar.charAt(i) == 'H') {
                readlen += Integer.parseInt(readseq);
                readseq = "";
            }else if(cigar.charAt(i) == 'S') {
                readlen += Integer.parseInt(readseq);
                readseq = "";
            }else {
                readseq = "";
            }
        }
        return readlen;
    }

    private int getReadStart() {
        if(cigar==null) return -1;
        String readseq = "";
        int readlen = 0;
        for(int i=0;i<cigar.length();i++) {
            //如果是数字则存入readseq，否则返回输出结果
            if(cigar.charAt(i) >=48 && cigar.charAt(i) <=57) {
                readseq += cigar.charAt(i);
            }else if(cigar.charAt(i) == 'M') {
                break;
            }else if(cigar.charAt(i) == 'I') {
                break;
            }else if(cigar.charAt(i) == 'D') {
                break;
            }else if(cigar.charAt(i) == 'H') {
                readlen += Integer.parseInt(readseq);
                break;
            }else if(cigar.charAt(i) == 'S') {
                readlen += Integer.parseInt(readseq);
                break;
            }else{
                //不可解析的cigar string
                MyError.unknowSymbolError(this.cigar);
                return -1;
            }
        }
        //区分+/-
        if (this.strand == '-'){
            readlen = this.readLen - readlen;
        }
        return readlen;
    }

    private int getReadEnd() {
        if(cigar==null) return -1;
        int rE;
        if (cigar.endsWith("H") || cigar.endsWith("S")){
            String readseq = "";
            for(int i=cigar.length()-2;i>=0;i--) {
                if(cigar.charAt(i) >=48 && cigar.charAt(i) <=57) {
                    readseq = cigar.charAt(i) + readseq;
                }else if(cigar.charAt(i) == 'M') {
                    break;
                }else if(cigar.charAt(i) == 'I') {
                    break;
                }else if(cigar.charAt(i) == 'D') {
                    break;
                }else if(cigar.charAt(i) == 'H') {
                    break;
                }else if(cigar.charAt(i) == 'S') {
                    break;
                }
            }
            rE = this.readLen - Integer.parseInt(readseq);
        }else {
            rE = this.readLen;
        }

        if (this.strand == '-'){
            rE = this.readLen - rE;
        }
        return rE;
    }

    public String toAlignTable(){
        String atLine = chr + '\t'
        + start + '\t'
        + end + '\t'
        + readName + '\t'
        + rS + '\t'
        + rE + '\t'
        + readLen + '\t'
        + scoreS + '\t'
        + strand;
        return atLine;
    }

    public int getReadLen() {
        return readLen;
    }

    public void setReadLen(int readLen) {
        this.readLen = readLen;
    }
}
