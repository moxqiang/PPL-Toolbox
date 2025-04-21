package utils;

import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CoverageReads {
    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.CoverageReads <monomers> <coverage>");
            System.exit(0);
        }
        String cF=args[0];
        String cover=args[1];
        String mode = "valid";

        //此为流程特供代码，用于提取正确结果的代码
        Judgement judgement = new Judgement();
        if (args.length==3) {
            mode = args[2];
        }


        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter cWriter = new BufferedWriter(new FileWriter(cover));

        RangeSet<Integer> rangeSet= TreeRangeSet.create();

        long sumCover=0;
        long sumLen=0;

        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);

            if (one.getReadName().equals("1c6b17db-2ebe-4baf-9937-d4397c5482d0"))
                System.out.println(one.toContactLine());

            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                long num=0;
                for (ReadAT at :
                        concatemer) {
                    //此为流程特供代码，用于提取正确结果的代码
                    if (mode.equalsIgnoreCase("valid")){
                        if (!judgement.judgeConfidentFrags(at)){
                            continue;
                        }
                    }
                    rangeSet.add(Range.closedOpen(at.rS, at.rE));
                }

                int mappingLength=0;
                String mappingRegionsS="";
                for (Range<Integer> range : rangeSet.asRanges()) {
                    mappingLength+=range.upperEndpoint()-range.lowerEndpoint();
                    mappingRegionsS+=range.toString()+",";
                }
                mappingRegionsS=StringUtils.substringBeforeLast(mappingRegionsS,",");

                cWriter.write(concatemer.get(0).readName+"\t"
                        +concatemer.get(0).readLen+"\t"
                        +mappingLength+"\t"
                        +(double) mappingLength/(double) concatemer.get(0).readLen+"\t"
                        +mappingRegionsS
                );
                cWriter.newLine();

                sumCover+=mappingLength;
                sumLen+=concatemer.get(0).readLen;
                concatemer.clear();
                concatemer.add(one);
                rangeSet.clear();
            }

        }

        //last one
        {
            long num=0;
            for (ReadAT at :
                    concatemer) {
                rangeSet.add(Range.closed(at.rS, at.rE));
            }

            int mappingLength=0;
            String mappingRegionsS="";
            for (Range<Integer> range : rangeSet.asRanges()) {
                mappingLength+=range.upperEndpoint()-range.lowerEndpoint()+1;
                mappingRegionsS+=range.toString()+",";
            }
            mappingRegionsS=StringUtils.substringBeforeLast(mappingRegionsS,",");

            cWriter.write(concatemer.get(0).readName+"\t"
                    +concatemer.get(0).readLen+"\t"
                    +mappingLength+"\t"
                    +(double) mappingLength/(double) concatemer.get(0).readLen+"\t"
                    +mappingRegionsS
            );
            cWriter.newLine();

            sumCover+=mappingLength;
            sumLen+=concatemer.get(0).readLen;
            concatemer.clear();
            rangeSet.clear();
        }

        System.out.println(MyUtil.getTimeFormatted()+" CoverageReads summary: "+
                "Coverage of valid mappings (length of all mapped regions / length of all mapped reads): \n\t"+(double)sumCover/(double)sumLen );

        mmReader.close();
        cWriter.close();
    }
}
