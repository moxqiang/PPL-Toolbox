package utils;

import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class DetectSynergiesFromLoops {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.Contact2Pairs <loopsList> <pairs>");
            System.exit(0);
        }
        String cF=args[0];
        String pF=args[1];

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter pairWriter = new BufferedWriter(new FileWriter(pF));
        concatemer.clear();
        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
//        line="## pairs format v1.0\n" +
//                "#columns: readID chr1 position1 chr2 position2 strand1 strand2";
//        pairWriter.write(line);
//        pairWriter.newLine();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Contact2Pairs: converting start");
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Contact2Pairs: inputfile="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Contact2Pairs: outfile="+pF);
        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //只使用有效的结果
            if (!judgement.judge(one)){
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
                            line=
//                                    mm1.chr+"\t"
//                                    +mm1.start+"\t"
//                                    +mm1.end+"\t"
//                                    +mm2.chr+"\t"
//                                    +mm2.start+"\t"
//                                    +mm2.end+"\t"
//                                    +mm1.readName+"\t"
//                                    +(mm1.strand)+(mm2.strand)
                                    mm1.readName+"\t"
                                    +mm1.chr+"\t"
                                    + ((mm1.start + mm1.end) / 2) + "\t"
                                    +mm2.chr+"\t"
                                    + ((mm2.start + mm2.end) / 2) + "\t"
                                    +(mm1.strand)+"\t"
                                    +(mm2.strand)
                            ;
                            pairWriter.write(line);
                            pairWriter.newLine();
                            if (flag == 1){
                                mm1 = mm2;
                            }
                        }
                    }
                }
                countReads++;
                if (countReads % 1000000 == 0){
                    System.out.println(MyUtil.getTimeFormatted()+" " +
                            "Contact2Pairs: count of finished concatemer" + countReads);
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
                        line=
//                                    mm1.chr+"\t"
//                                    +mm1.start+"\t"
//                                    +mm1.end+"\t"
//                                    +mm2.chr+"\t"
//                                    +mm2.start+"\t"
//                                    +mm2.end+"\t"
//                                    +mm1.readName+"\t"
//                                    +(mm1.strand)+(mm2.strand)
                                mm1.readName+"\t"
                                        +mm1.chr+"\t"
                                        + ((mm1.start + mm1.end) / 2) + "\t"
                                        +mm2.chr+"\t"
                                        + ((mm2.start + mm2.end) / 2) + "\t"
                                        +(mm1.strand)+"\t"
                                        +(mm2.strand)
                        ;
                        pairWriter.write(line);
                        pairWriter.newLine();
                        if (flag == 1){
                            mm1 = mm2;
                        }
                    }
                }
            }
            countReads++;
            if (countReads % 1000000 == 0){
                System.out.println(MyUtil.getTimeFormatted()+" " +
                        "Contact2Pairs: count of finished concatemer" + countReads);
            }
            concatemer.clear();
//            concatemer.add(one);
        }

        mmReader.close();
        pairWriter.close();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Contact2Pairs: count of all converted reads="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Contact2Pairs: finished");
    }
}
