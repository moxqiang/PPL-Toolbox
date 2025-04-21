package utils;

import process.contact.ReadAT;
import utils.entity.ChrSizes;

import java.io.*;
import java.util.*;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class StatsDistributionByBin {
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.out.println("Usage: java -cp PPL.jar utils.StatsDistributionByBin <monomers> <prefixOutputFiles> <chromSizes> <binSize, 1000000>");
            System.exit(0);
        }
        //读取参数
        String iF = args[0];
        String oF = args[1];
        String chrFile = args[2];
        Integer binSize = Integer.parseInt(args[3]);



        //用于限制区域
        boolean isRangeLimited=false;
        String range=null;
        String rangeChr=null;
        Long rangeStart=null;
        Long rangeEnd=null;
        if (args.length >= 6) {
            isRangeLimited=true;
            range = args[5];
            rangeChr = range.split(":")[0];
            if (range.contains(":")) {
                rangeStart = Long.parseLong(range.split(":")[1].split("-")[0]);
                rangeEnd = Long.parseLong(range.split(":")[1].split("-")[1]);
            } else {
                rangeStart = (long) 0;
                rangeEnd = Long.MAX_VALUE;
            }
        }

        //读取chromSizes 文件，划分bin
        String line;
        ChrSizes chrSizes = new ChrSizes(chrFile, binSize);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: chromosome sizes info:");
        for (String chrName :
                chrSizes.getChrMap().keySet()) {
            System.out.println(
                    "Chromsomes INFO: " +
                    chrName + "\t" +
                            chrSizes.getChrMap().get(chrName) + "\t" +
                            Arrays.toString(chrSizes.getChrBinRange().get(chrName))
            );

        }
        System.out.println(
                "nbins\t" +
                chrSizes.getNumBins()
        );
        System.out.println(
                "bin-size\t" +
                chrSizes.getResolution()+
                "\n"
        );

        //统计map
        HashMap<String, Long[]> statMap = new HashMap<>();
        ArrayList<String> dList = new ArrayList<String>(Arrays.asList(
            "1",
            "2",
            "3",
            "4",
            "5-6",
            "7-11",
            "12-21",
            "22-49",
            "50-",
            "sum"
        ));
        for (String d :
                dList) {
            Long[] longs = new Long[chrSizes.getNumBins()];
            Arrays.fill(longs, 0L);
            statMap.put(d, longs);
        }

        //按区间统计维数
        List<ReadAT> concatemer = new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(iF);
        BufferedReader mmReader = new BufferedReader(new FileReader(iF));

        concatemer.clear();
        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "StatsDistributionByBin: stating multi-way contact distribution: start");

        while ((line = mmReader.readLine()) != null) {
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //只使用有效的结果
            if (!judgement.judge(one)) {
                continue;
            }
            if (!chrSizes.getChrSet().contains(one.getChr())){
                continue;
            }
            if (isRangeLimited &&
                !(rangeChr.equalsIgnoreCase(one.getChr())
                    && rangeStart < one.getMid()
                    && rangeEnd > one.getMid()
            )){
                continue;
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                int size = concatemer.size();
                Long[] countList = statMap.get(getTag(size));
                for (ReadAT at :
                        concatemer) {
                    int binByPos = chrSizes.getBinByPos(at.getChr(), at.getMid());
                    try {
                        countList[binByPos]+=1;
                        statMap.get("sum")[binByPos]+=1;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("countList.length = " + countList.length);
                        System.out.println("binByPos = " + binByPos);
                    }
                }

                countReads++;
                if (countReads % 1000000 == 0) {
                    System.out.println(MyUtil.getTimeFormatted() + " " +
                            "StatsDistributionByBin: " + countReads+" of concatemers were finished......");
                }
                concatemer.clear();
                concatemer.add(one);
            }
        }

        //处理最后一个
        {
            int size = concatemer.size();
            Long[] countList = statMap.get(getTag(size));
            for (ReadAT at :
                    concatemer) {
                int binByPos = chrSizes.getBinByPos(at.getChr(), at.getMid());
                try {
                    countList[binByPos]+=1;
                    statMap.get("sum")[binByPos]+=1;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("countList.length = " + countList.length);
                    System.out.println("binByPos = " + binByPos);
                }
            }

            countReads++;
            if (countReads % 1000000 == 0) {
                System.out.println(MyUtil.getTimeFormatted() + " " +
                        "StatsDistributionByBin: " + countReads+" of concatemers were finished......");
            }
            concatemer.clear();
        }
        mmReader.close();

        System.out.println(MyUtil.getTimeFormatted() + " " +
                "StatsDistributionByBin: stating count distribution: end\n");
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "StatsDistributionByBin: writing results\n");


        //逐个维度写入
        HashMap<String, Double> averageMap = new HashMap<>();
        for (String d :
                dList) {
            BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF+"."+binSize+"."+d+".stats.bedgraph"));
            BufferedWriter oWriter2 = new BufferedWriter(new FileWriter(oF+"."+binSize+"."+d+".stats.standardized.bedgraph"));
            BufferedWriter oWriter3 = new BufferedWriter(new FileWriter(oF+"."+binSize+"."+d+".stats.standardized2.bedgraph"));
            Long[] dCountList = statMap.get(d);
            double average = calculateAverage(dCountList);
            averageMap.put(d, average);
            for (int i = 0; i < dCountList.length; i++) {
                Map.Entry<String, Integer> pos = chrSizes.getPosByBin(i);
                line= pos.getKey()+"\t"+pos.getValue()+"\t"+(pos.getValue()+binSize) + "\t" + dCountList[i];
                oWriter.write(line);
                oWriter.newLine();
                line= pos.getKey()+"\t"+pos.getValue()+"\t"+(pos.getValue()+binSize) + "\t" + (dCountList[i]/average);
                oWriter2.write(line);
                oWriter2.newLine();
                if (statMap.get("sum")[i] != 0)
                    line= pos.getKey()+"\t"+pos.getValue()+"\t"+(pos.getValue()+binSize) + "\t" + dCountList[i]/(double)statMap.get("sum")[i];
                else
                    line= pos.getKey()+"\t"+pos.getValue()+"\t"+(pos.getValue()+binSize) + "\t" + 0;
                oWriter3.write(line);
                oWriter3.newLine();
            }
            oWriter.close();
            oWriter2.close();
            oWriter3.close();
        }
        //合并维度写入

        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF+"."+binSize+".merged.stats.txt"));
        BufferedWriter oWriter2 = new BufferedWriter(new FileWriter(oF+"."+binSize+".merged.stats.standardized.txt"));
        BufferedWriter oWriter3 = new BufferedWriter(new FileWriter(oF+"."+binSize+".merged.stats.standardized2.txt"));
        for (int i = 0; i < chrSizes.getNumBins(); i++) {
            Map.Entry<String, Integer> pos = chrSizes.getPosByBin(i);
            line= pos.getKey()+"\t"+pos.getValue()+"\t"+(pos.getValue()+binSize) + "\t";
            String tmpLine1 ="";
            String tmpLine2 ="";
            String tmpLine3 ="";
            for (String d :
                    dList) {
                tmpLine1 += (statMap.get(d)[i]+"\t");
                tmpLine2 += ((statMap.get(d)[i]/averageMap.get(d))+"\t");
                if (statMap.get("sum")[i] != 0)
                    tmpLine3 += ((statMap.get(d)[i]/(double)statMap.get("sum")[i])+"\t");
                else
                    tmpLine3 += (0 +"\t");
            }
            oWriter.write(line+tmpLine1.trim());
            oWriter.newLine();
            oWriter2.write(line+tmpLine2.trim());
            oWriter2.newLine();
            oWriter3.write(line+tmpLine3.trim());
            oWriter3.newLine();
        }
        oWriter.close();
        oWriter2.close();
        oWriter3.close();

        System.out.println(MyUtil.getTimeFormatted() + " " +
                "StatsDistributionByBin: finished");
    }

    public static double calculateAverage(Long[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组为空或长度为零");
        }

        int sum = 0;
        for (long num : array) {
            sum += num;
        }

        double average = (double) sum / array.length;
        return average;
    }

    public static String getTag( Integer num){
        if (num == 1){
            return "1";
        }
        if (num == 2){
            return "2";
        }
        if (num == 3){
            return "3";
        }
        if (num == 4){
            return "4";
        }
        if (num >= 5 && num <= 6){
            return "5-6";
        }
        if (num >= 7 && num <= 11){
            return "7-11";
        }
        if (num >= 12 && num <= 21){
            return "12-21";
        }
        if (num >= 22 && num <= 49){
            return "22-49";
        }
        if (num >= 50){
            return "50-";
        }
        return "None";
    }

}
