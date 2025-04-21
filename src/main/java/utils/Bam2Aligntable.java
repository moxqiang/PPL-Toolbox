package utils;

import com.google.common.collect.RangeMap;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import process.mapping.ReadBed;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static utils.AssignFragment.assignFrag;
import static utils.AssignFragment.loadResSiteFile;

public class Bam2Aligntable {

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

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java -cp pore-c_tool.jar utils.Bam2Aligntable <input.bam> <output.bed> <splitReads? default: N>");
            System.exit(1);
        }
        String inputBam = args[0];
        String outputBed = args[1];
//        String restrictionFile = args[2];
        String splitReads = "N";
        if (args.length > 2) {
            MyUtil.checkNY(args[2]);
            splitReads = args[2];
        }

        SamReader reader = SamReaderFactory.makeDefault().open(new File(inputBam));
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Bam2Aligntable: reading bam and writting bed.withAS ...");
        long countMapped = 0;
        long countUnmapped = 0;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputBed)));
//            Map<String, RangeMap<Long,Long>> rangeMapMap = new HashMap<String,RangeMap<Long,Long>>();
//            Map<Long,String> mapRange = new HashMap<Long,String>();
            for (SAMRecord record : reader) {
                if (record.getReadUnmappedFlag()) {
                    continue;
                }
                //to bed
                String chr = record.getReferenceName();
                String start = record.getAlignmentStart() - 1 + "";
                String end = record.getAlignmentEnd() + "";
                String readName = record.getReadName();
                String mapq = record.getMappingQuality() + "";
                String asFlag = record.getIntegerAttribute("AS") + "";
                String strand = getStrand(record);
                String cigar = record.getCigarString() ;
                String mapIndex = countMapped + "";

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
                        mapq+":"+asFlag,
                        strand,
                        cigar
                };
                ReadBed one = new ReadBed(fields, splitReads);
                //写入aligntable并加入mid和status
                String line = one.toAlignTable() + "\t"
                        + mapIndex + "\t"   //index
                        + "null";            //status
//                //to aligntable.withFrag 分配酶切片段
//                if (rangeMapMap.isEmpty()) {
//                    loadResSiteFile(
//                            restrictionFile,
//                            rangeMapMap,
//                            mapRange
//                    );
//                }
//                line = assignFrag(
//                        line,
//                        rangeMapMap,
//                        mapRange
//                );
                //输出
                if (one.start != one.end) {
                    writer.println(line);
                    if (++countMapped % 1000000 == 0){
                        System.out.println(MyUtil.getTimeFormatted()+" " +
                                "Bam2Aligntable: "+ countMapped +" records were handled ...");
                    }
                } else{
                    System.out.println(" WARNING: start site == end site \n\t"+one.toAlignTable());
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
                "Bam2Aligntable: finished");
        reader.close();
    }
}
