package utils;

import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Contact2PCluster {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -cp PPL.jar utils.Contact2PCluster <monomers> <cluster>");
            System.exit(0);
        }

        String cF=args[0];
        String mF =args[1];

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter pairWriter = new BufferedWriter(new FileWriter(mF));
        concatemer.clear();
        Judgement judgement = new Judgement();

        long num = 0;
        while ((line= mmReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            if (!judgement.judge(one)){
                continue;
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
//                line=concatemer.get(0).readName+"\t"+concatemer.size()+"\t";
                line=concatemer.get(0).readName+"\t";
                for (ReadAT mm :
                        concatemer) {
//                    line += mm.chr + "\t" + mm.start + "\t" + mm.end + ";";
                    line += mm.chr + ":" + mm.start+ "-" + mm.end + "\t";
                }
                if (line.endsWith("\t")) line=line.substring(0,line.length()-1);
                pairWriter.write(line);
                pairWriter.newLine();
                num++;
                if (num%1000000==0){
                    System.out.println("[ "+num+" lines written ]");
                }

                concatemer.clear();
                concatemer.add(one);
            }
        }

        //处理最后一个
        {
//                line=concatemer.get(0).readName+"\t"+concatemer.size()+"\t";
            line=concatemer.get(0).readName+"\t";
            for (ReadAT mm :
                    concatemer) {
//                    line += mm.chr + "\t" + mm.start + "\t" + mm.end + ";";
                line += mm.chr + ":" + mm.start+ "-" + mm.end + "\t";
            }
            if (line.endsWith("\t")) line=line.substring(0,line.length()-1);
            pairWriter.write(line);
            pairWriter.newLine();
            num++;
            if (num%1000000==0){
                System.out.println("[ "+num+" lines written ]");
            }

            concatemer.clear();
        }

        mmReader.close();
        pairWriter.close();

    }
}
