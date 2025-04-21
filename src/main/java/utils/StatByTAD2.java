package utils;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class StatByTAD2 {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.StatByTAD2 <pairs> <domainfile(3 colmns)> <stats.out>");
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
            String chr1 = fields[0];
            String chr2 = fields[3];
            Long mid1 = (long) ((Integer.parseInt(fields[1]) + Integer.parseInt(fields[2])) / 2);
            Long mid2 = (long) ((Integer.parseInt(fields[4]) + Integer.parseInt(fields[5])) / 2);

            try {
                String d1 = (String) domainMaps.get(chr1).get(mid1);
                String d2 = (String) domainMaps.get(chr2).get(mid2);
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
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.out.println(line);
            }

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
