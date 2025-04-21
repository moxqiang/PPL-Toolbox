package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang3.StringUtils;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

public class DeDuplicateByFrag {
    public static void main(String[] args) throws IOException {
        //用于筛除PCR扩增的结果（目前只用于单细胞结果）
        //标准：当两个多交互reads同时注释到相同的fragments，后来者将会被去除
        if (args.length < 3){
            System.out.println("Usage: java -cp pore-c_tool.jar utils.DeDuplicateByFrag <contactFile> <contactFile.dedup> <statsFile>");
            System.exit(0);
        }
        System.out.println(MyUtil.getTimeFormatted()+" DeDuplicateByRegion summary: start");


        String cF=args[0];
        String dF=args[1];
        String stats =args[2];
        String mode = "valid";

        //此为流程特供代码，用于提取正确结果的代码
        Judgement judgement = new Judgement();


        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter mmWriter = new BufferedWriter(new FileWriter(dF));
        BufferedWriter cWriter = new BufferedWriter(new FileWriter(stats));

        HashMap<Long, Set<String>> fragMap = new HashMap<>(); // fragID:[readName1, readName2 ...]
        HashMap<String, Long> concatemerNusMap = new HashMap<>();
        HashSet<String> savedSet = new HashSet<>();
        Set<String> candidates = new HashSet(); //存储扩增对象的候选
        Set<String> candidatesTmp = new HashSet();
        String apliconType = "";

        long num1 = 0; //concatemer总数
        long num2 = 0; //duplicate concatemer总数
        long num21 = 0; //duplicate concatemer identical
        long num22 = 0; //duplicate concatemer included

        //fragMAP字典构建：
        while ((line= mmReader.readLine())!=null) {
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //此为流程特供代码，用于提取正确结果的代码
            if (mode.equalsIgnoreCase("valid")) {
                if (!judgement.judge(one)) {
                    continue;
                }
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                for (ReadAT at :
                        concatemer) {
                    for (long[] f :
                            at.getFrags()) {
                        long fID = f[0];
                        if (fragMap.containsKey(fID)) {
                            fragMap.get(fID).add(at.getReadName());
                        } else {
                            HashSet<String> newSet = new HashSet<>();
                            newSet.add(at.getReadName());
                            fragMap.put(fID, newSet);
                        }
                    }
                    concatemerNusMap.put(concatemer.get(0).getReadName(), (long)concatemer.size());
                }
                concatemer.clear();
                concatemer.add(one);
            }
        }
        {
            for (ReadAT at :
                    concatemer) {
                for (long[] f :
                        at.getFrags()) {
                    long fID = f[0];
                    if (fragMap.containsKey(fID)) {
                        fragMap.get(fID).add(at.getReadName());
                    } else {
                        HashSet<String> newSet = new HashSet<>();
                        newSet.add(at.getReadName());
                        fragMap.put(fID, newSet);
                    }
                }
                concatemerNusMap.put(concatemer.get(0).getReadName(), (long)concatemer.size());
            }
            concatemer.clear();
        }

        //再读一次，开始过滤
        mmReader.close();
        mmReader = new BufferedReader( new FileReader(cF));

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
                long num=0;
                boolean isAmplicon=false;
                boolean isfirst=true;
                for (ReadAT at :
                        concatemer) {
                    for (long[] f :
                            at.getFrags()) {
                        long fID = f[0];
                        if (fragMap.containsKey(fID)){
                            candidatesTmp.addAll(fragMap.get(fID)); //取并集
                        } else{
                            continue;
                        }
                    }
                    if (isfirst){
                        candidates.addAll(candidatesTmp);
                        isfirst = false;
                    }
                    candidates.retainAll(candidatesTmp); //取交集
                    candidatesTmp.clear();
                }

                apliconType="None";
                isAmplicon=false;
                if (candidates.size() <= 1){ //非duplicate
                    apliconType="None";
                    isAmplicon=false;
                } else {
                    String nameNow = concatemer.get(0).getReadName();
                    //挨个candidate判断其是否
                    for (String candidate :
                            candidates) {
                        if(candidate.equals(nameNow)) continue;
                        Long candidatesSize = concatemerNusMap.get(candidate);
                        //若存在更长的cluster则视当前结果位扩增子
                        if (candidatesSize > concatemer.size()) {
                            isAmplicon = true;
                            apliconType = "Included";
                            break;
                        }
                        //若存在等长的cluster且已存则视当前结果位扩增子
                        if (candidatesSize == concatemer.size()) {
                            if (savedSet.contains(candidate)) {
                                isAmplicon = true;
                                apliconType = "Identical";
                                break;
                            }
                        }
                    }
                }

                num1++;
                //如果不是扩增子，将重新写回文件，并记录在map
                line = concatemer.get(0).getReadName() + "\t" +candidates.toString() + "\t"+isAmplicon +"\t"+apliconType;
                cWriter.write(line);
                cWriter.newLine();
                if (!isAmplicon){
                    //写回取冗余结果
                    for (ReadAT monomer :
                            concatemer) {
                        mmWriter.write(monomer.toContactLine());
                        mmWriter.newLine();
                    }
                    //记录
                    savedSet.add(concatemer.get(0).getReadName());
//                    for (ReadAT at :
//                            concatemer) {
//                        for (long[] f :
//                                at.getFrags()) {
//                            long fID = f[0];
//                            if (fragMap.containsKey(fID)) {
//                                fragMap.get(fID).add(at.getReadName());
//                            }else {
//                                HashSet<String> newSet = new HashSet<>();
//                                newSet.add(at.getReadName());
//                                fragMap.put(fID, newSet);
//                            }
//                        }
//                    }
                } else { //否则将会被删除并写入统计信息
                    num2++;
                    if (apliconType.equalsIgnoreCase("Identical")) num21++;
                    if (apliconType.equalsIgnoreCase("Included")) num22++;
                }

                concatemer.clear();
                concatemer.add(one);
                candidates.clear();
            }

        }

        //last one
        {
            long num=0;
            boolean isAmplicon=false;
            boolean isfirst=true;
            for (ReadAT at :
                    concatemer) {
                for (long[] f :
                        at.getFrags()) {
                    long fID = f[0];
                    if (fragMap.containsKey(fID)){
                        candidatesTmp.addAll(fragMap.get(fID)); //取并集
                    } else{
                        continue;
                    }
                }
                if (isfirst){
                    candidates.addAll(candidatesTmp);
                    isfirst = false;
                }
                candidates.retainAll(candidatesTmp); //取交集
                candidatesTmp.clear();
            }

            apliconType="None";
            isAmplicon=false;
            if (candidates.size() <= 1){ //非duplicate
                apliconType="None";
                isAmplicon=false;
            } else {
                String nameNow = concatemer.get(0).getReadName();
                //挨个candidate判断其是否
                for (String candidate :
                        candidates) {
                    if(candidate.equals(nameNow)) continue;
                    Long candidatesSize = concatemerNusMap.get(candidate);
                    //若存在更长的cluster则视当前结果位扩增子
                    if (candidatesSize > concatemer.size()) {
                        isAmplicon = true;
                        apliconType = "Included";
                        break;
                    }
                    //若存在等长的cluster且已存则视当前结果位扩增子
                    if (candidatesSize == concatemer.size()) {
                        if (savedSet.contains(candidate)) {
                            isAmplicon = true;
                            apliconType = "Identical";
                            break;
                        }
                    }
                }
            }

            num1++;
            //如果不是扩增子，将重新写回文件，并记录在map
            line = concatemer.get(0).getReadName() + "\t" +candidates.toString() + "\t"+isAmplicon +"\t"+apliconType;
            cWriter.write(line);
            cWriter.newLine();
            if (!isAmplicon){
                //写回取冗余结果
                for (ReadAT monomer :
                        concatemer) {
                    mmWriter.write(monomer.toContactLine());
                    mmWriter.newLine();
                }
                //记录
                savedSet.add(concatemer.get(0).getReadName());
//                    for (ReadAT at :
//                            concatemer) {
//                        for (long[] f :
//                                at.getFrags()) {
//                            long fID = f[0];
//                            if (fragMap.containsKey(fID)) {
//                                fragMap.get(fID).add(at.getReadName());
//                            }else {
//                                HashSet<String> newSet = new HashSet<>();
//                                newSet.add(at.getReadName());
//                                fragMap.put(fID, newSet);
//                            }
//                        }
//                    }
            } else { //否则将会被删除并写入统计信息
                num2++;
                if (apliconType.equalsIgnoreCase("Identical")) num21++;
                if (apliconType.equalsIgnoreCase("Included")) num22++;
            }

            concatemer.clear();
            candidates.clear();
        }

        System.out.println(MyUtil.getTimeFormatted()+" DeDuplicateByRegion summary: end");
        System.out.println();
        line="All concatemers count: "+num1+"\n"+
                "Duplicate count: "+num2+"\t"+
                "Ratio: "+(double) num2/(double) num1+"\n"+
                "Identical Duplicate count: "+num21+"\t"+
                "Ratio: "+(double) num21/(double) num1+"\n"+
                "Included Duplicate count: "+num22+"\t"+
                "Ratio: "+(double) num22/(double) num1
        ;
        System.out.println(
            line
        );
        cWriter.write(line);
        cWriter.newLine();

        mmReader.close();
        mmWriter.close();
        cWriter.close();
    }
}
