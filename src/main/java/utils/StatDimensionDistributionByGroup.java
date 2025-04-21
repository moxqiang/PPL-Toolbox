package utils;

import process.contact.ReadAT;

import java.io.*;
import java.util.*;

public class StatDimensionDistributionByGroup {
    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.StatDimensionDistributionByGroup <contactFile> <outFile>");
            System.exit(0);
        }
        String cF=args[0];
        String dDF =args[1]; //distribuiton file of dimension
        String mode = "valid";

        //此为流程特供代码，用于提取正确结果的代码
        Judgement judgement = new Judgement();
        if (args.length==3) {
            mode = args[2];
        }

        System.out.println(MyUtil.getTimeFormatted()+"StatDimensionDistribution: start");


        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter cWriter = new BufferedWriter(new FileWriter(dDF));

        Map<Integer, Integer> dDMap = new TreeMap<>(); //recording distribuiton of dimension

        long sumCover=0;
        long sumLen=0;

        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //此为流程特供代码，用于提取正确结果的代码
            if (mode.equalsIgnoreCase("valid")){
                if (!judgement.judge(one)){
                    continue;
                }
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                if (dDMap.get(concatemer.size()) != null)
                    dDMap.put(concatemer.size(), dDMap.get(concatemer.size())+1);
                else dDMap.put(concatemer.size(), 1);

                concatemer.clear();
                concatemer.add(one);
            }

        }

        //last one
        {
            if (dDMap.get(concatemer.size()) != null)
                dDMap.put(concatemer.size(), dDMap.get(concatemer.size())+1);
            else dDMap.put(concatemer.size(), 1);

            concatemer.clear();
        }

        //按维数统计不同类型交互的数量
        Integer[][] ranges = {
                {2,2},
                {3,3},
                {4,4},
                {5,6},
                {7,11},
                {12,21},
                {22,Integer.MAX_VALUE}
        };

        Map<Integer[], Integer> dDGMap = splitByGroup(ranges, dDMap);

        line = "";
        for (Integer[] range:
                ranges
             ) {
            line = Arrays.toString(range)+"\t" + dDGMap.get(range);
            System.out.println(line);
            cWriter.write(line);
            cWriter.newLine();
        }

        System.out.println(MyUtil.getTimeFormatted()+"StatDimensionDistribution: end");

        mmReader.close();
        cWriter.close();
    }

    public static Map<Integer[],Integer> splitByGroup(Integer[][] ranges, Map<Integer, Integer> dDMap){
        HashMap<Integer[], Integer> dDGMap;
        dDGMap = new HashMap<Integer[], Integer>();
        Integer max = Collections.max(dDMap.keySet());
        for (Integer[] range :
                ranges) {
            Integer start = range[0];
            Integer end = range[1]>max? max: range[1];
            Integer val = 0;
            for (int i = start; i <= end; i++) {
                if (dDMap.containsKey(i)){
                    val += dDMap.get(i);
                }else {
                    continue;
                }
            }
            dDGMap.put(range, val);
        }
        return dDGMap;
    }
}
