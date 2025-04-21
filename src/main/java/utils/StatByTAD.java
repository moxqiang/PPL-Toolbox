package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class StatByTAD {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.StatByTAD <contacts> <domainfile(3 colmns)> <stats.out>");
            System.exit(0);
        }
        String cF=args[0];
        String dF=args[1];
        String oF =args[2];

        List<Long> numIntraSList = new ArrayList<>();
        List<Long> numIntraLList = new ArrayList<>();
        List<Long> numInterList = new ArrayList<>();

        long numIntraTAD=0;
        long numInterTAD=0;;
        long numNN=0;;
        long numTADN=0;;

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedReader domainReader = new BufferedReader( new FileReader(dF));
        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(oF));
        concatemer.clear();

//        读入domain文件
        Map<String, TreeRangeMap> domainMaps = new HashMap<>();

        while ((line= domainReader.readLine())!=null){
            String[] fields = line.trim().split("\t");
            String chr = fields[0];
            Long start = Long.parseLong(fields[1]);
            Long end = Long.parseLong(fields[2]);
            if (domainMaps.get(chr)==null){
                domainMaps.put(chr,TreeRangeMap.create());
            }
            domainMaps.get(chr).put(Range.closed(start,end), chr+":"+start+"-"+end);
        }
        domainReader.close();


        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
//        line="## pairs format v1.0\n" +
//                "#columns: readID chr1 position1 chr2 position2 strand1 strand2";
//        pairWriter.write(line);
//        pairWriter.newLine();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: converting start");
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: inputfile="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: outfile="+ oF);
        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //只使用有效的结果
            if (!judgement.judge(one)){
                continue;
            }
            if (!domainMaps.keySet().contains(one.getChr())){
                continue;
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                mms.clear();
                for (ReadAT mm1 :
                        concatemer) {
                    mms.add(mm1);
                    for (ReadAT mm2 :
                            concatemer) {
                        if (mms.contains(mm2))
                            continue;
//                            break;
                        else{
                            int flag = 0;
                            if (mm1.chr.compareTo(mm2.chr)>0 ){
                                ReadAT tmp = mm1;
                                mm1 = mm2;
                                mm2 = tmp;
                                flag = 1;
                            }
//                            #columns: readID chr1 pos1 chr2 pos2 strand1 strand2

                            String d1 = (String) domainMaps.get(mm1.getChr()).get(mm1.getMid());
                            String d2 = (String) domainMaps.get(mm2.getChr()).get(mm2.getMid());
                            if (d1==null || d2==null){
                                if (d1==d2)
                                    numTADN++;
                                else
                                    numNN++;
                            } else {
                                if (d1==d2)
                                    numIntraTAD++;
                                else
                                    numInterTAD++;
                            }

                            if (flag == 1){
                                mm1 = mm2;
                            }
                        }
                    }
                }
                countReads++;
                if (countReads % 1000000 == 0){
                    System.out.println(MyUtil.getTimeFormatted()+" " +
                            "StatIntraInter: count of finished concatemer" + countReads);
                }
                concatemer.clear();
                concatemer.add(one);
            }
        }

        //处理最后一个
        {
            mms.clear();
            for (ReadAT mm1 :
                    concatemer) {
                mms.add(mm1);
                for (ReadAT mm2 :
                        concatemer) {
                    if (mms.contains(mm2))
                        continue;
//                            break;
                    else{
                        int flag = 0;
                        if (mm1.chr.compareTo(mm2.chr)>0 ){
                            ReadAT tmp = mm1;
                            mm1 = mm2;
                            mm2 = tmp;
                            flag = 1;
                        }
//                            #columns: readID chr1 pos1 chr2 pos2 strand1 strand2

                        String d1 = (String) domainMaps.get(mm1.getChr()).get(mm1.getMid());
                        String d2 = (String) domainMaps.get(mm2.getChr()).get(mm2.getMid());
                        if (d1==null || d2==null){
                            if (d1==d2)
                                numTADN++;
                            else
                                numNN++;
                        } else {
                            if (d1==d2)
                                numIntraTAD++;
                            else
                                numInterTAD++;
                        }

                        if (flag == 1){
                            mm1 = mm2;
                        }
                    }
                }
            }
            countReads++;
            if (countReads % 1000000 == 0){
                System.out.println(MyUtil.getTimeFormatted()+" " +
                        "StatIntraInter: count of finished concatemer" + countReads);
            }
            concatemer.clear();
        }

        mmReader.close();
        line="\n\t\tintra-TAD\tinter-TAD\tTAD-N\tN-N";
        statsWriter.write(line);
        statsWriter.newLine();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: Summary"+line);
        line="\tAll\t"+numIntraTAD+
                "\t"+numInterTAD+
                "\t"+numTADN+
                "\t"+numNN;
        statsWriter.write(line);
        statsWriter.newLine();
        System.out.println(line);

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

//        for (Integer[] range :
//                ranges) {
//            numIntraS=0;
//            numIntraL=0;
//            numInter=0;
//            Integer min = range[0];
//            Integer max = range[1];
//            line= Arrays.toString(range)+"\t";
//            for (Integer dimension = 2;
//                 dimension<=numIntraSList.size()-1;
//                 dimension++) {
//                if (dimension>=min && dimension<=max)
//                    numIntraS+=numIntraSList.get(dimension);
//            }
//            for (Integer dimension = 2;
//                 dimension<=numIntraLList.size()-1;
//                 dimension++) {
//                if (dimension>=min && dimension<=max)
//                    numIntraL+=numIntraLList.get(dimension);
//            }
//            for (Integer dimension = 2;
//                 dimension<=numInterList.size()-1;
//                 dimension++) {
//                if (dimension>=min && dimension<=max)
//                    numInter+=numInterList.get(dimension);
//            }
//            line="\t"+Arrays.toString(range)+"\t"+numIntraS+
//                    "\t"+numIntraL+
//                    "\t"+numInter;
//            statsWriter.write(line);
//            statsWriter.newLine();
//            System.out.println(line);
//        }

        statsWriter.close();

        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: finished");
    }


    public static void listAddOne(List<Long> list, Integer index){
        if (index > list.size()-1){
            for (Integer i=list.size(); i<=index; i++){
                list.add(i, (long)0);
            }
        }
        if (list.get(index)==null){
            list.set(index, (long)1);
        }else {
            list.set(index, list.get(index)+1);
        }
    }
}
