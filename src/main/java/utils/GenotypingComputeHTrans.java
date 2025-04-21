package utils;

import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class GenotypingComputeHTrans {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.GenotypingComputeHTrans <monomers> <probability.txt> <num_line>");
            System.exit(0);
        }
        String cF=args[0];
        String pF=args[1];
        int numLine=Integer.MAX_VALUE;
        if (args.length==3){
            numLine = Integer.parseInt(args[2]);
        }

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter statWriter = new BufferedWriter(new FileWriter(pF));
        concatemer.clear();
        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
//        line="## pairs format v1.0\n" +
//                "#columns: readID chr1 position1 chr2 position2 strand1 strand2";
//        statWriter.write(line);
//        statWriter.newLine();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "GenotypingComputeHTrans: start");
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "GenotypingComputeHTrans: inputfile="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "GenotypingComputeHTrans: outfile="+pF);

        int countCis = 0;
        int countTrans = 0;
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
                            // when chr1 == chr2
                            if (mm1.getChr().equalsIgnoreCase(mm2.getChr())
//                                && mm1.getPhase().equalsIgnoreCase(mm2.getPhase())
                                && ! mm1.getPhase().equalsIgnoreCase("0")
                                && ! mm2.getPhase().equalsIgnoreCase("0")
                            ){
                                long dist = Math.abs(mm1.getMid() - mm2.getMid());
                                boolean isCis = mm1.getPhase().equalsIgnoreCase(mm2.getPhase());
                                line=
                                        mm1.getReadName() + "\t" +
                                        mm1.getChr() + "\t" +
                                        dist + "\t" +
                                        (isCis?"cis":"trans") + "\t" +
                                        mm1.getIndex() + "\t" + mm2.getIndex()
                                ;
                                if (isCis) countCis++;
                                else countTrans++;
                                statWriter.write(line);
                                statWriter.newLine();
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
                            "GenotypingComputeHTrans: count of finished concatemer" + countReads);
                }
                concatemer.clear();
                concatemer.add(one);
                if (countReads % numLine == 0){
                    System.out.println(MyUtil.getTimeFormatted()+" " +
                            "GenotypingComputeHTrans: count of finished concatemer" + numLine);
                    break;
                }
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
                        // when chr1 == chr2
                        if (mm1.getChr().equalsIgnoreCase(mm2.getChr())
//                                && mm1.getPhase().equalsIgnoreCase(mm2.getPhase())
                                && ! mm1.getPhase().equalsIgnoreCase("0")
                                && ! mm2.getPhase().equalsIgnoreCase("0")
                        ){
                            long dist = Math.abs(mm1.getMid() - mm2.getMid());
                            boolean isCis = mm1.getPhase().equalsIgnoreCase(mm2.getPhase());
                            line=
                                    mm1.getReadName() + "\t" +
                                            mm1.getChr() + "\t" +
                                            dist + "\t" +
                                            (isCis?"cis":"trans") + "\t" +
                                            mm1.getIndex() + "\t" + mm2.getIndex()
                            ;
                            if (isCis) countCis++;
                            else countTrans++;
                            statWriter.write(line);
                            statWriter.newLine();
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
                        "GenotypingComputeHTrans: count of finished concatemer" + countReads);
            }
            concatemer.clear();
            if (countReads % numLine == 0){
                System.out.println(MyUtil.getTimeFormatted()+" " +
                        "GenotypingComputeHTrans: count of finished concatemer" + numLine);
            }
        }

        mmReader.close();
        statWriter.close();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "GenotypingComputeHTrans: count of processed reads="+countReads);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "GenotypingComputeHTrans: finished");

        System.out.println("countCis = " + countCis);
        System.out.println("countTrans = " + countTrans);

    }
}
