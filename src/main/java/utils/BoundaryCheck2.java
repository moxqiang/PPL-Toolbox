package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import process.contact.ReadAT;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BoundaryCheck2 {
    public static void main(String[] args) throws IOException {
        if (args.length < 3){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.BoundaryCheck2 <chrName:start:end:status> <RestrictionSite> <distanceAllReads>");
            System.exit(0);
        }

        String mF = args[0];
        String restrictionFile = args[1];
        String oF = args[2];

        BufferedReader reader = new BufferedReader(new FileReader(new File(restrictionFile)));
        //按染色体存储酶切区间
        Map<String,RangeSet<Long>> rangeSetMap = new HashMap<String,RangeSet<Long>>();
        Map<String,Long> disMap = new HashMap<String,Long>();
        Map<String,Long> disMapMin = new HashMap<String,Long>();
        Map<String,Long> countMap = new HashMap<String,Long>();
        RangeSet<Long> rangeSet ;
        Long dis;
        Long count;
        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF));

        //读取酶切文件
        String line;
        while ((line=reader.readLine())!=null){
            String[] fields = line.split("\t");
            if (rangeSetMap.containsKey(fields[0])) {
                rangeSetMap.get(fields[0]).add(Range.open(Long.parseLong(fields[1]), Long.parseLong(fields[2])));
            } else {
                rangeSetMap.put(fields[0], TreeRangeSet.create());
                rangeSetMap.get(fields[0]).add(Range.open(Long.parseLong(fields[1]), Long.parseLong(fields[2])));
            }
        }
        reader.close();
        for (String chr :
                rangeSetMap.keySet()) {
            countMap.put(chr,(long)0);
            disMap.put(chr,(long)0);
            disMapMin.put(chr,(long)0);
        }

        //只分配酶切文件中包含的chr
        Set<String> chrSet = countMap.keySet();

        //匹配酶切末端
        Range<Long> range;
        Range<Long> rangeL;
        Range<Long> rangeR;
        reader = new BufferedReader(new FileReader(mF));
        Judgement judgement = new Judgement();
        while ((line=reader.readLine())!=null){
            String[] fields = line.split("\t");
            if (!judgement.judgeS(fields[3])){
                continue;
            }
            if (!chrSet.contains(fields[0])){
                continue;
            }
            rangeSet = rangeSetMap.get(fields[0]);
            range = Range.openClosed(Long.parseLong(fields[1]), Long.parseLong(fields[2]));
            rangeL = rangeSet.rangeContaining(range.lowerEndpoint());
            rangeR = rangeSet.rangeContaining(range.upperEndpoint());
            long disL1 = 0;
            long disL2 = 0;
            if (rangeL != null) {
                disL1 = range.lowerEndpoint() - rangeL.lowerEndpoint();
                disL2 = rangeL.upperEndpoint() - range.lowerEndpoint();
            }
            long disR1 = 0;
            long disR2 = 0;
            if (rangeR != null) {
                disR1 = range.upperEndpoint() - rangeR.lowerEndpoint();
                disR2 = rangeR.upperEndpoint() - range.upperEndpoint();
            }
            long dis1 = disL1<disL2?disL1:disL2;
            long dis2 = disR1<disR2?disR1:disR2;

            disMap.put(fields[0], disMap.get(fields[0])+dis1+dis2);
            disMapMin.put(fields[0], disMapMin.get(fields[0])+(dis1<dis2?dis1:dis2)*2);
            countMap.put(fields[0], countMap.get(fields[0])+2);
            String outLine = +dis1+"\t"+dis2;
            oWriter.write(outLine);
            oWriter.newLine();
        }
        oWriter.close();

        //打印基本统计信息
        long sumFragments=0;
        long sumDis=0;
        long sumDisMin=0;
        long sumCount=0;
        long lenGenome=0;
        System.out.println("Summary:\nchr\tnumFrags\tnumMapps\tAvgDis\tAvgDisMin");
        for (String chr : disMap.keySet()) {
            if (countMap.get(chr)!=0) {
                System.out.println(chr + "\t" +
                        rangeSetMap.get(chr).asRanges().size() + "\t" +
                        countMap.get(chr) / 2 + "\t" +
                        disMap.get(chr) / countMap.get(chr) + "\t" +
                        disMapMin.get(chr) / countMap.get(chr)
                );
            }else {
                System.out.println("WARNING: mappings count of "+chr+" is 0");
            }
            sumFragments+=rangeSetMap.get(chr).asRanges().size();
            sumDis+=disMap.get(chr);
            sumDisMin+=disMapMin.get(chr);
            sumCount+=countMap.get(chr);
            lenGenome+=rangeSetMap.get(chr).span().upperEndpoint();
        }
        System.out.println( "\nall\t"+
                sumFragments+"\t"+
                sumCount/2+"\t"+
                sumDis/sumCount+"\t"+
                sumDisMin/sumCount
        );
        System.out.println("\nAverage length of all fragments: "+lenGenome/sumFragments);
    }
}
