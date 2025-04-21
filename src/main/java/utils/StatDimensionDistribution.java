package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang3.StringUtils;
import process.contact.ReadAT;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.util.*;

public class StatDimensionDistribution {
    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.StatDimensionDistribution <contactFile> <outFile>");
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

        line = "";
        for (Map.Entry<Integer, Integer> e
                : dDMap.entrySet()) {
            line = e.getKey()+"\t" + e.getValue();
            System.out.println(line);
            cWriter.write(line);
            cWriter.newLine();
        }

        System.out.println(MyUtil.getTimeFormatted()+"StatDimensionDistribution: end");

        mmReader.close();
        cWriter.close();
    }
}
