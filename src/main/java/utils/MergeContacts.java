package utils;

import process.Merge;
import process.contact.ComparatorReadATBySE;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

public class MergeContacts {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.MergeContacts <contacts1> <contacts2> <contactMerged>");
            System.exit(0);
        }

//        new MergeContacts().test(args);
        new MergeContacts().rudeSort(args);
//        new MergeContacts().smartSort(args);
    }

    public void test(String[] args) throws IOException {

        String cF1=args[0];
        String cF2=args[1];
        String mF =args[2];

        BufferedReader mmReader1 = new BufferedReader( new FileReader(cF1));
        BufferedReader mmReader2 = new BufferedReader( new FileReader(cF2));
        BufferedWriter mergedWriter = new BufferedWriter(new FileWriter(mF));


        //以文件二为基础合并文件1
        String line1 = null;
        String line2 = null;
        List<ReadAT> concatemer1=new ArrayList<ReadAT>();
        List<ReadAT> concatemer2=new ArrayList<ReadAT>();
        List<ReadAT> concatemerMerged=new ArrayList<ReadAT>();
        ComparatorReadATBySE comparatorReadATBySE = new ComparatorReadATBySE();
        ReadAT one;
        ReadAT one2 ;
        while ((line1=mmReader1.readLine())!=null){
            String[] fields = line1.split("\t");
            one = new ReadAT(fields);
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer1.size() == 0){
                concatemer1.add(one);
            } else if (one.readName.equals(concatemer1.get(0).readName)){
                concatemer1.add(one);
            } else {
                //如果line2没有内容，则向下读一行
                if (line2==null) {
                    line2= mmReader2.readLine();
                }
                //一直读
                do {
                    String[] fields2 = line2.split("\t");
                    one2 = new ReadAT(fields2);
                    //直到满足条件开始合并
                    if (one2.getReadName().equals(concatemer1.get(0).getReadName())){
                        //将第一个满足条件的比对加入列表2
                        concatemer2.add(one2);
                        //继续读
                        while((line2= mmReader2.readLine())!=null){
                            //判断是否一致
                            if (one2.getReadName().equals(concatemer1.get(0).getReadName())) {
                                fields2 = line1.split("\t");
                                one2 = new ReadAT(fields2);
                                concatemer2.add(one2);
                            }else {
                                //当读到的内容不再一致，则合并最终结果并写回（注意，此时line2中的内容没有被写回）
                                concatemerMerged.addAll(concatemer1);
                                concatemerMerged.addAll(concatemer2);
                                concatemerMerged.sort(comparatorReadATBySE);
                                for (ReadAT at:
                                        concatemerMerged) {
                                    mergedWriter.write(at.toContactLine());
                                    mergedWriter.newLine();
                                }
                                break;
                            }
                        }
                        //当文件二在这个时候读完，开始写回
                        if(line2==null){
                            //当读到的内容不再一致，则合并最终结果并写回
                            concatemerMerged.addAll(concatemer1);
                            concatemerMerged.addAll(concatemer2);
                            concatemerMerged.sort(comparatorReadATBySE);
                            for (ReadAT at:
                                    concatemerMerged) {
                                mergedWriter.write(at.toContactLine());
                                mergedWriter.newLine();
                            }
                            break;
                        }
                        break;
                    }else {
                        //当两个文件当前读到的名字不一致时，将文件一的内容写到新的文件
                        mergedWriter.write(one2.toContactLine());
                        mergedWriter.newLine();
                    }
                }while ((line2=mmReader2.readLine())!=null);

                //当文件2读完，也要退出循环
                if (line2==null){
                    if (concatemerMerged.isEmpty()){
                        concatemerMerged.addAll(concatemer1);
                        concatemerMerged.sort(comparatorReadATBySE);
                        for (ReadAT at:
                                concatemerMerged) {
                            mergedWriter.write(at.toContactLine());
                            mergedWriter.newLine();
                        }
                    }
                    break;
                }

                //一轮结束，清除容器
                concatemerMerged.clear();
                concatemer2.clear();
                concatemer1.clear();
                concatemer1.add(one);
            }
        }

        if (line1==null){
            //当文件一先读完，将文件二剩余部分和未写回的Line2写回
            {
                //如果line2没有内容，则向下读一行
                if (line2==null) {
                    line2= mmReader2.readLine();
                }
                //一直读
                do {
                    String[] fields2 = line2.split("\t");
                    one2 = new ReadAT(fields2);
                    //直到满足条件开始合并
                    if (one2.getReadName().equals(concatemer1.get(0).getReadName())){
                        //将第一个满足条件的比对加入列表2
                        concatemer2.add(one2);
                        //继续读
                        while((line2= mmReader2.readLine())!=null){
                            //判断是否一致
                            if (one2.getReadName().equals(concatemer1.get(0).getReadName())) {
                                fields2 = line1.split("\t");
                                one2 = new ReadAT(fields2);
                                concatemer2.add(one2);
                            }else {
                                //当读到的内容不再一致，则合并最终结果并写回（注意，此时line2中的内容没有被写回）
                                concatemerMerged.addAll(concatemer1);
                                concatemerMerged.addAll(concatemer2);
                                concatemerMerged.sort(comparatorReadATBySE);
                                for (ReadAT at:
                                        concatemerMerged) {
                                    mergedWriter.write(at.toContactLine());
                                    mergedWriter.newLine();
                                }
                                break;
                            }
                        }
                        //当文件二在这个时候读完，开始写回
                        if(line2==null){
                            //当读到的内容不再一致，则合并最终结果并写回
                            concatemerMerged.addAll(concatemer1);
                            concatemerMerged.addAll(concatemer2);
                            concatemerMerged.sort(comparatorReadATBySE);
                            for (ReadAT at:
                                    concatemerMerged) {
                                mergedWriter.write(at.toContactLine());
                                mergedWriter.newLine();
                            }
                            break;
                        }
                        break;
                    }else {
                        //当两个文件当前读到的名字不一致时，将文件一的内容写到新的文件
                        mergedWriter.write(one2.toContactLine());
                        mergedWriter.newLine();
                    }
                }while ((line2=mmReader2.readLine())!=null);

                //当文件2读完，也要退出循环
                if (line2==null){
                    if (concatemerMerged.isEmpty()){
                        concatemerMerged.addAll(concatemer1);
                        concatemerMerged.sort(comparatorReadATBySE);
                        for (ReadAT at:
                                concatemerMerged) {
                            mergedWriter.write(at.toContactLine());
                            mergedWriter.newLine();
                        }
                    }
                }

                //一轮结束，清除容器
                concatemerMerged.clear();
                concatemer2.clear();
                concatemer1.clear();
            }
            do {
                mergedWriter.write(line2);
                mergedWriter.newLine();
            } while((line2= mmReader2.readLine())!=null);
        }else if (line2==null){
            while((line1= mmReader1.readLine())!=null){
                mergedWriter.write(line1);
                mergedWriter.newLine();
            }
        }

        mmReader1.close();
        mmReader2.close();
        mergedWriter.close();
    }

    public void rudeSort(String[] args) throws IOException {
        String cF1 = args[0];
        String cF2 = args[1];
        String mF = args[2];

        BufferedReader mmReader1 = new BufferedReader(new FileReader(cF1));
        BufferedReader mmReader2 = new BufferedReader(new FileReader(cF2));
        BufferedWriter mergedWriter = new BufferedWriter(new FileWriter(mF));

        String line;
        ReadAT one;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        while ((line= mmReader1.readLine())!=null){
            String[] fields = line.split("\t");
            one = new ReadAT(fields);
            concatemer.add(one);
        }
        while ((line= mmReader2.readLine())!=null){
            String[] fields = line.split("\t");
            one = new ReadAT(fields);
            concatemer.add(one);
        }

        System.out.println(concatemer.size());
        ComparatorReadATBySE comparatorReadATBySE = new ComparatorReadATBySE();
        concatemer.sort(comparatorReadATBySE);

        for (ReadAT at:
                concatemer) {
            mergedWriter.write(at.toContactLine());
            mergedWriter.newLine();
        }

        mmReader1.close();
        mmReader2.close();
        mergedWriter.close();
    }

    public void smartSort(String[] args) throws IOException {
        String cF1 = args[0];
        String cF2 = args[1];
        String mF = args[2];

        ComparatorReadATBySE comparatorReadATBySE = new ComparatorReadATBySE();
        Map<String, Integer> nameMap = new HashMap<String, Integer>();
        BufferedReader mmReader1 = new BufferedReader(new FileReader(cF1));
        BufferedReader mmReader2 = new BufferedReader(new FileReader(cF2));
        BufferedWriter mergedWriter = new BufferedWriter(new FileWriter(mF));

        String line;
        ReadAT one;
        String readName = null;
        String tmpName = null;
        Map<String,List<String>> gapLines=new HashMap<String, List<String>>() {};
        List<ReadAT> concatemer=new ArrayList<ReadAT>();

        int countMap = 0;
        int countMap1 = 0;
        int countRead = 0;
        int countNeedMerge = 0;
        int countNotMerge = 0;

        //遍历两个contacts文件，确定哪些reads需要被合并
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "MergeContacts step1: collecting all reads and make classification");
        while ((line= mmReader1.readLine())!=null){
            countMap1++;
            readName = line.split("\t")[3];
            nameMap.put(readName, 0);
        }
        while ((line= mmReader2.readLine())!=null){
            countMap1++;
            readName = line.split("\t")[3];
            if (!nameMap.containsKey(readName)){
                nameMap.put(readName, 2);
            } else if (nameMap.get(readName) == 0) {
                nameMap.put(readName, 1);
            }
//            countNeedMerge ++;
        }
//        countMap = nameMap.size();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "MergeContacts step1: finished");
        mmReader1.close();
        mmReader2.close();


        //重新遍历，开始合并
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "MergeContacts step2: merging");
        mmReader1 = new BufferedReader(new FileReader(cF1));
        mmReader2 = new BufferedReader(new FileReader(cF2));
        while ((line= mmReader2.readLine())!=null){
            String[] fields = line.split("\t");
            if (gapLines.containsKey(fields[3])) {
                gapLines.get(fields[3]).add(line);
            }
            else {
                gapLines.put(fields[3], new ArrayList<String>());
                gapLines.get(fields[3]).add(line);
            }
        }
        while ((line= mmReader1.readLine())!=null){
            String[] fields = line.split("\t");
            one = new ReadAT(fields);
            if (nameMap.get(fields[3]) == 0){
                //直接输出
                mergedWriter.write(line);
                mergedWriter.newLine();
                countMap++;
                //如果临时空间还存储着内容，则合并后输出
                if (concatemer.size()!=0){
                    for (String aline :
                            gapLines.get(concatemer.get(0).getReadName())){
                        String[] afields = aline.split("\t");
                        ReadAT two = new ReadAT(afields);
                        concatemer.add(two);
                    }
                    gapLines.remove(concatemer.get(0).getReadName());
                    concatemer.sort(comparatorReadATBySE);
                    for (ReadAT at:
                            concatemer) {
                        mergedWriter.write(at.toContactLine());
                        mergedWriter.newLine();
                        countMap++;
                    }
                    concatemer.clear();
//                    concatemer.add(one);
                }
            }
            else if (nameMap.get(fields[3]) == 1){
                //将同一个concatemer的比对在一个批次进行处理
                if (concatemer.size() == 0) {
                    concatemer.add(one);
                } else if (one.getReadName().equals(concatemer.get(0).getReadName())) {
                    concatemer.add(one);
                } else {
                    for (String aline :
                         gapLines.get(concatemer.get(0).getReadName())){
                        String[] afields = aline.split("\t");
                        ReadAT two = new ReadAT(afields);
                        concatemer.add(two);
                    }
                    gapLines.remove(concatemer.get(0).getReadName());
                    concatemer.sort(comparatorReadATBySE);
                    for (ReadAT at:
                            concatemer) {
                        mergedWriter.write(at.toContactLine());
                        mergedWriter.newLine();
                        countMap++;
                    }
                    concatemer.clear();
                    concatemer.add(one);
                }
            }
        }
        //处理最后一个
        if (concatemer.size()!=0){
            for (String aline :
                    gapLines.get(concatemer.get(0).getReadName())){
                String[] afields = aline.split("\t");
                ReadAT two = new ReadAT(afields);
                concatemer.add(two);
            }
            gapLines.remove(concatemer.get(0).getReadName());
            concatemer.sort(comparatorReadATBySE);
            for (ReadAT at:
                    concatemer) {
                mergedWriter.write(at.toContactLine());
                mergedWriter.newLine();
                countMap++;
            }
            concatemer.clear();
//            concatemer.add(one);
        }

        //处理2中有1中没有的
        for (String readNameTmp :
                gapLines.keySet()) {
            if (nameMap.get(readNameTmp).equals(2)){
                for (String lineTmp:
                gapLines.get(readNameTmp)) {
                    mergedWriter.write(lineTmp);
                    mergedWriter.newLine();
                    countMap++;
                }
//                gapLines.remove(readNameTmp);
            }
        }

//        System.out.println("gapLines = " + gapLines.size());
//        for (String lineTmp:
//                gapLines.values()) {
//            mergedWriter.write(lineTmp);
//            mergedWriter.newLine();
//            countMap++;
//        }

        System.out.println(MyUtil.getTimeFormatted()+" " +
                "MergeContacts step2: finished");


        System.out.println(MyUtil.getTimeFormatted()+" " +
                "MergeContacts summary: " + countMap);
//        System.out.println("countMap1 = " + countMap1);

        mmReader1.close();
        mmReader2.close();
        mergedWriter.close();
    }


}
