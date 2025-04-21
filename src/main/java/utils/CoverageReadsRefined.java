package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang3.StringUtils;
import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverageReadsRefined {
    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.CoverageReadsRefined <monomers> <coverage> <resSites>");
            System.exit(0);
        }
        String cF=args[0];
        String cover=args[1];
        String resSites=args[2];
        String mode = "valid";

        //此为流程特供代码，用于提取正确结果的代码
        Judgement judgement = new Judgement();
        if (args.length==4) {
            mode = args[3];
        }


        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedReader resReader = new BufferedReader( new FileReader(resSites));
        BufferedWriter cWriter = new BufferedWriter(new FileWriter(cover));

        RangeSet<Integer> rangeSet= TreeRangeSet.create();

        long sumCover=0;
        long sumLen=0;

        Map<String,List<Integer>> resSitesMap = new HashMap<>();
        while ((line= resReader.readLine())!=null){
            String[] fields = line.split("\t");
            if (resSitesMap.containsKey(fields[0]))
                resSitesMap.get(fields[0]).add(Integer.parseInt(fields[1]));
            else {
                resSitesMap.put(fields[0], new ArrayList<Integer>());
                resSitesMap.get(fields[0]).add(Integer.parseInt(fields[1]));
            }
        }
        resReader.close();

        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);

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
                    refineBoundaryOnReads(at, resSitesMap);
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
                //此为流程特供代码，用于提取正确结果的代码
                if (mode.equalsIgnoreCase("valid")){
                    if (!judgement.judgeConfidentFrags(at)){
                        continue;
                    }
                }
                refineBoundaryOnReads(at, resSitesMap);
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
            rangeSet.clear();
        }

        System.out.println(MyUtil.getTimeFormatted()+" CoverageReads summary: "+
                "Coverage of valid mappings (length of all mapped regions / length of all mapped reads): \n\t"+(double)sumCover/(double)sumLen );

        mmReader.close();
        cWriter.close();
    }

    public static void refineBoundaryOnReads(ReadAT at, Map<String,List<Integer>> resSitesMap){
        if (! resSitesMap.containsKey(at.getReadName())) return;

        List<Integer> list = resSitesMap.get(at.getReadName());
        List<Integer> distListS = new ArrayList<>();
        List<Integer> distListE = new ArrayList<>();
        for (Integer site :
                list) {
            distListS.add(Math.abs(site - at.getrS()));
            distListE.add(Math.abs(site - at.getrE()));
        }
        int minValueIndexS = findMinValueIndex(distListS);
        int minValueIndexE = findMinValueIndex(distListE);

        // START modification
        if (list.get(minValueIndexS) < at.getrS()) {
            // 若site小于START，则延伸过多会影响gaps的提取，因此这里最多只延伸50bp
            if (distListS.get(minValueIndexS) < 50){
                at.setrS(list.get(minValueIndexS));
            }
        } else {
            // 若site大于START，则可以延伸多一些
            if (distListS.get(minValueIndexS) < 500 && list.get(minValueIndexS)<at.getrE()){
                at.setrS(list.get(minValueIndexS));
            }
        }

        // END modification
        if (list.get(minValueIndexE) > at.getrE()) {
            // 若site小于END，则延伸过多会影响gaps的提取，因此这里最多只延伸50bp
            if (distListE.get(minValueIndexE) < 50){
                at.setrE(list.get(minValueIndexE));
            }
        } else {
            // 若site大于START，则延伸过多会影响gaps的提取，因此这里最多只延伸50bp
            if (distListE.get(minValueIndexE) < 500 && list.get(minValueIndexE)>at.getrS()){
                at.setrE(list.get(minValueIndexE));
            }
        }
    }
    public static int findMinValueIndex(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null or empty");
        }

        int minValue = numbers.get(0);
        int minIndex = 0;

        for (int i = 1; i < numbers.size(); i++) {
            int currentNumber = numbers.get(i);
            if (currentNumber < minValue) {
                minValue = currentNumber;
                minIndex = i;
            }
        }

        return minIndex;
    }
}
