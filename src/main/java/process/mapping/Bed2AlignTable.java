package process.mapping;

import utils.MyUtil;

import java.io.*;
import java.util.Arrays;

public class Bed2AlignTable {
    public Bed2AlignTable(String inputfile, String outputFile, String splitReads) throws IOException {
        MyUtil.checkPath(inputfile);
        BufferedReader bedReader = new BufferedReader( new FileReader(inputfile));
        new File(outputFile).delete();
        BufferedWriter atWriter = new BufferedWriter( new FileWriter(outputFile));

        String line;
        long num=0;
        while ((line = bedReader.readLine())!=null){
            String[] fields = line.split("\t");
            //filter mapq < cutoff reads
//            if (Integer.parseInt(fields[4]) < cutoff) continue;
//            ReadBed one = new ReadBed(fields);
            ReadBed one = new ReadBed(fields, splitReads);
            if (one.start != one.end) {
                //写入aligntable并加入mid和status
                atWriter.write(one.toAlignTable() + "\t" + num + "\t" + "null");
                num++;
                atWriter.newLine();
            } else{
                System.out.println(" WARNING: start site == end site \n"+line);
            }
        }
        bedReader.close();
        atWriter.close();
    }


    public static void main(String[] args) throws IOException {
        if (args.length ==3 ) {
            new Bed2AlignTable(args[0], args[1]+".aligntable", args[2]);
        }else {
            System.out.println("Usage: java -cp *.jar proBam2AlignTable <inputFile> <OutputFile>");
        }

    }
}
