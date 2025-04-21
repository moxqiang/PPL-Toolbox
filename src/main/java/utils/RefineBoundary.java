package utils;

import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class RefineBoundary {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.RefineBoundary <monomers> <refinedMonomers>");
            System.exit(0);
        }
        String cF=args[0];
        String rF =args[1];

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter rmWriter = new BufferedWriter(new FileWriter(rF));
        concatemer.clear();
        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
//        line="## pairs format v1.0\n" +
//                "#columns: readID chr1 position1 chr2 position2 strand1 strand2";
//        rmWriter.write(line);
//        rmWriter.newLine();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "RefineBoundary: converting start");
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "RefineBoundary: inputfile="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "RefineBoundary: outfile="+ rF);
        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            if (!judgement.judgeUnmapped(one)) {
                one.refineBoundaries();
                // 拯救low quality &

            }
            rmWriter.write(one.toContactLine());
            rmWriter.newLine();
        }


        mmReader.close();
        rmWriter.close();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "RefineBoundary: count of all converted reads="+cF);
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "RefineBoundary: finished");
    }
}
