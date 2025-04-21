package utils;

import process.contact.ComparatorReadATBySE;
import process.contact.ReadAT;
import utils.merge.MergeOne;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContactMerge {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.ContactMerge <contacts> <outputFile> <cutoff> <cutoff>");
            System.exit(0);
        }

        String cF=args[0];
        String mF =args[1];
        int cutoff1 = Integer.parseInt(args[2]);
        int cutoff2 = Integer.parseInt(args[3]);

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        List<ReadAT> concatemerP=new ArrayList<ReadAT>();
        List<ReadAT> concatemerU=new ArrayList<ReadAT>();
        List<ReadAT> concatemerFinal=new ArrayList<ReadAT>();

        MergeOne mergeConsole = new MergeOne();
        ComparatorReadATBySE comparatorReadAT = new ComparatorReadATBySE();

        //2pairs
        MyUtil.checkPath(cF);
        BufferedReader mmReader = new BufferedReader( new FileReader(cF));
        BufferedWriter mergedWriter = new BufferedWriter(new FileWriter(mF));
        concatemer.clear();

        long num = 0;
        long num1 = 0;
        long num2 = 0;
        while ((line= mmReader.readLine())!=null){
            num1++;
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                //开始处理
                //区分passed和other
                for (ReadAT at :
                        concatemer) {
                    //四种status标记被认为是可以merge的
                    if(at.status.equalsIgnoreCase("passed") ||
                            at.status.contains("close") ||
                            at.status.contains("multi") ||
                            at.status.contains("saved") ||
                            at.status.contains("null") ||
                            at.status.contains("self-interaction") ||
                            at.status.equalsIgnoreCase("merged")){
                        concatemerP.add(at);
                    }else {
                        concatemerU.add(at);
                    }
                }
                if (concatemerP.size()!=0) {
                    //开始合并并排序
                    concatemerFinal.addAll(mergeConsole.run(concatemerP, cutoff1, cutoff2));
                    num = num + (concatemerP.size() - concatemerFinal.size());
                    concatemerFinal.addAll(concatemerU);
                    concatemerFinal.sort(comparatorReadAT);
                } else {
                    //无需合并
                    concatemerFinal.addAll(concatemerU);
                    concatemerFinal.sort(comparatorReadAT);
                }
                for (ReadAT at :
                        concatemerFinal) {
                    mergedWriter.write(at.toContactLine());
                    mergedWriter.newLine();
                    num2++;
                }

                //处理完毕，清空容器
                concatemer.clear();
                concatemerP.clear();
                concatemerU.clear();
                concatemerFinal.clear();

                concatemer.add(one);
            }
        }

        //处理最后一个
        {
            //开始处理
            //区分passed和other
            for (ReadAT at :
                    concatemer) {
                //四种status标记被认为是可以merge的
                if(at.status.equalsIgnoreCase("passed") ||
                        at.status.contains("close") ||
                        at.status.contains("multi") ||
                        at.status.contains("saved") ||
                        at.status.contains("null") ||
                        at.status.contains("self-interaction") ||
                        at.status.equalsIgnoreCase("merged")){
                    concatemerP.add(at);
                }else {
                    concatemerU.add(at);
                }
            }
            if (concatemerP.size()!=0) {
                //开始合并并排序
                concatemerFinal.addAll(mergeConsole.run(concatemerP, cutoff1, cutoff2));
                num = num + (concatemerP.size() - concatemerFinal.size());
                concatemerFinal.addAll(concatemerU);
                concatemerFinal.sort(comparatorReadAT);
            } else {
                //无需合并
                concatemerFinal.addAll(concatemerU);
                concatemerFinal.sort(comparatorReadAT);
            }
            for (ReadAT at :
                    concatemerFinal) {
                mergedWriter.write(at.toContactLine());
                mergedWriter.newLine();
                num2++;
            }

            //处理完毕，清空容器
            concatemer.clear();
            concatemerP.clear();
            concatemerU.clear();
            concatemerFinal.clear();

        }

        //资源释放
        mmReader.close();
        mergedWriter.close();

        System.out.println("count of merged mappings = "+num);
        System.out.println("count of input mappings = "+num1);
        System.out.println("count of output mappings = "+num2);
    }
}
