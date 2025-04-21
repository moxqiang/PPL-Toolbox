package utils;

import org.checkerframework.checker.units.qual.mm2;
import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class StatIntraInter2 {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.StatIntraInter2 <pairs> <stats.out>");
            System.exit(0);
        }
        String cF=args[0];
        String oF =args[1];

        long numIntraS=0;
        long numIntraL=0;
        long numInter=0;

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(oF));
        concatemer.clear();
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
        long numLine = 0;
        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            String chr1 = fields[1];
            String chr2 = fields[3];
            long pos1 = Long.parseLong(fields[2]);
            long pos2 = Long.parseLong(fields[4]);
            if(chr1.equalsIgnoreCase(chr2)){
                if (Math.abs(
                        pos1 - pos2
                ) > 20000){
                    numIntraL++;
                }else
                    numIntraS++;
            }else
                numInter++;
            numLine++;
            if (numLine % 1000000 == 0){
                System.out.println(MyUtil.getTimeFormatted()+" " +
                        "StatIntraInter: count of finished pairs" + numLine);
            }
        }

        mmReader.close();
        line="\nintra short-range(<=20kb)\t"+numIntraS+
                " \nintra long-range(>20kb)\t"+numIntraL+
                " \ninter\t"+numInter;
        statsWriter.write(line);
        statsWriter.newLine();
        statsWriter.close();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: Summary"+line);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "StatIntraInter: finished");
    }
}
