package utils;

import com.google.common.collect.RangeMap;
import htsjdk.samtools.*;
import process.mapping.Bed2AlignTable;
import process.mapping.ReadBed;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static utils.AssignFragment.assignFrag;
import static utils.AssignFragment.loadResSiteFile;

public class Bam2AligntableWithFrag {

    private static String getStrand(SAMRecord record) {
        if (record.getReadNegativeStrandFlag()) {
            return "-";
        } else {
            return "+";
        }
    }

    private static int getReadStart(SAMRecord record) {
        return -1;
    }

    private static int getReadEnd(SAMRecord record) {
        return -1;
    }

    private static int getMatchCount(String cigarString) {
        int matchCount = 0;

        StringBuilder lengthString = new StringBuilder();
        for (int i = 0; i < cigarString.length(); i++) {
            char c = cigarString.charAt(i);
            if (Character.isDigit(c)) {
                lengthString.append(c);
            } else {
                int length = Integer.parseInt(lengthString.toString());
                lengthString.setLength(0); // 重置lengthString

                if (c == 'M') {
                    matchCount += length;
                }
            }
        }

        return matchCount;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java -cp pore-c_tool.jar utils.Bam2AligntableWithFrag <input.bam> <output.bed> <restrictionFile> <splitReads? default: N>");
            System.exit(1);
        }
        String inputBam = args[0];
        String outputBed = args[1];
        String restrictionFile = args[2];
        String splitReads = "N";
        if (args.length > 3) {
            MyUtil.checkNY(args[3]);
            splitReads = args[3];
        }

        SamReader reader = SamReaderFactory.makeDefault().open(new File(inputBam));
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Bam2AligntableWithFrag: reading bam and writting aligntable.withAS ...");
        long countMapped = 0;
        long countUnmapped = 0;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputBed)));
            Map<String, RangeMap<Long,Long>> rangeMapMap = new HashMap<String,RangeMap<Long,Long>>();
            Map<Long,String> mapRange = new HashMap<Long,String>();
            SAMRecordIterator samRecordIterator = reader.iterator();
            while (samRecordIterator.hasNext()){
                try {
                    SAMRecord record = samRecordIterator.next();
//                }
//                for (SAMRecord record : reader) {
                    ReadBed one;
                    String line;            //status
                    if (record.getReadUnmappedFlag()) {
                        //如果read没有比对的上
                        //to bed
                        String chr = "*";
                        String start = "-1";
                        String end = "-1";
                        String readName = record.getReadName();
                        int readLength = record.getReadLength();
                        String mapq = "-1";
                        String strand = "*";
                        String cigar = null;
                        String mapIndex = countMapped + "";

                        //由于AS会随match和mismatch分数的变化而变化，这里替换为match count
//                String asFlag = record.getIntegerAttribute("AS") + "";
                        String asFlag = "-1";


                        //这部分将来可能会实现，目前仍旧用原来的方法计算以下的三个属性
//                String readLen = record.getReadLength() + "";
//                String rStart = getReadStart(record) + "";
//                String rEnd = getReadEnd(record) + "";

                        //to aligntable
                        String[] fields = new String[]{
                                chr,
                                start,
                                end,
                                readName,
                                mapq + ":" + asFlag,
                                strand,
                                cigar
                        };
                        one = new ReadBed(fields, splitReads);
                        one.setReadLen(readLength);
                        //写入aligntable并加入mid和status
                        line = one.toAlignTable() + "\t"
                                + mapIndex + "\t"   //index
                                + "unmapped";

                        //to aligntable.withFrag 分配酶切片段
                        if (rangeMapMap.isEmpty()) {
                            loadResSiteFile(
                                    restrictionFile,
                                    rangeMapMap,
                                    mapRange
                            );
                        }
                        //不分配酶切片段
//                        line = assignFrag(
//                                line,
//                                rangeMapMap,
//                                mapRange
//                        );
//                        continue;
                    }else {
                        //to bed
                        String chr = record.getReferenceName();
                        String start = record.getAlignmentStart() - 1 + "";
                        String end = record.getAlignmentEnd() + "";
                        String readName = record.getReadName();
                        String mapq = record.getMappingQuality() + "";
                        String strand = getStrand(record);
                        String cigar = record.getCigarString();
                        String mapIndex = countMapped + "";

                        //由于AS会随match和mismatch分数的变化而变化，这里替换为match count
//                String asFlag = record.getIntegerAttribute("AS") + "";
                        String asFlag = getMatchCount(cigar) + "";


                        //这部分将来可能会实现，目前仍旧用原来的方法计算以下的三个属性
//                String readLen = record.getReadLength() + "";
//                String rStart = getReadStart(record) + "";
//                String rEnd = getReadEnd(record) + "";

                        //to aligntable
                        String[] fields = new String[]{
                                chr,
                                start,
                                end,
                                readName,
                                mapq + ":" + asFlag,
                                strand,
                                cigar
                        };
                        one = new ReadBed(fields, splitReads);
                        //写入aligntable并加入mid和status
                        line = one.toAlignTable() + "\t"
                                + mapIndex + "\t"   //index
                                + "null";
                        //to aligntable.withFrag 分配酶切片段
                        if (rangeMapMap.isEmpty()) {
                            loadResSiteFile(
                                    restrictionFile,
                                    rangeMapMap,
                                    mapRange
                            );
                        }
                        line = assignFrag(
                                line,
                                rangeMapMap,
                                mapRange
                        );
                    }
                    //输出
                    if (record.getReadUnmappedFlag() || one.start != one.end) {
                        writer.println(line);
                        if (++countMapped % 1000000 == 0){
                            System.out.println(MyUtil.getTimeFormatted()+" " +
                                    "Bam2AligntableWithFrag: "+ countMapped +" records were handled ...");
                        }
                    } else{
                        System.out.println(" WARNING: start site == end site \n\t"+one.toAlignTable());
                    }
                } catch (SAMFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Bam2AligntableWithFrag: finished");
        reader.close();
    }
}
