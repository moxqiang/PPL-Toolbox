package utils;

import utils.entity.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SplitByLinker {
    private static final int MIN_LEN = 50;
    private static final double RATIO_LEN = 0.6;


    private static List<Pair> findAllLinkerSite(String read, String linker){
        int[][] scoreMatrix = SmithWaterman.computeScoreMatrix(read, linker);
        int cuttoff = (int) (SmithWaterman.getMatchScore() * linker.length() * RATIO_LEN);

        List<Pair> allPos = SmithWaterman.findAllLinker(read, linker, scoreMatrix, cuttoff);

//        for (Pair p :
//                allPos) {
//            String[] traceSeqs = null;
//            try{
//                traceSeqs = SmithWaterman.traceback(
//                        read,
//                        linker,
//                        scoreMatrix,
//                        new int[]{p.getEnd(), p.getEndLinker()}
//                );
//            } catch(Exception e){
//                e.printStackTrace();
//                System.out.println(p);
//            }
//            System.out.println(p);
//            System.out.println(traceSeqs[0]);
//            System.out.println(traceSeqs[1]);
//        }

        return allPos;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.SplitByLinker <fastq> <fastq.splited> <Linker>");
            System.exit(0);
        }
        String fq = args[0];
        MyUtil.checkPath(fq);
        String fqs = args[1];
        String linker = args[2];
        System.out.println(
                "[input fastq file] "+ fq);
        System.out.println(
                "[output splited fastq file] "+ fqs);
        System.out.println(
                "[linker sequence info] " + linker);

        BufferedReader reader;
        BufferedOutputStream writer;

        if (MyUtil.isGZipped(new File(fq))){
            reader  = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(fq))));
        }else{
            reader = new BufferedReader((
                    new FileReader(
                            new File(fq)
                    )
                    ));
        }

        writer = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fqs)));

        String line;
        String aline;
        long num=0;
        long numS=0;
        long numR=0;
        Read read = new Read();

        //读取fq
        while((line= reader.readLine())!=null){
            num ++;
            //读取一条read的四行后开始处理
            if (num!=1 && num%4==1) {
                numR ++;
                if (numR%1000000==0){
                    System.out.println(MyUtil.getTimeFormatted()+" " +
                            "INFO: "+numR+" reads splited...");
                }
                //初始化的分割的起始位置
                //模拟酶切并输出模拟酶切片段，直到无法找到新的酶切位点
                List<Pair> allLinkerSite = findAllLinkerSite(read.getSeq(), linker);
                Iterator<Pair> pairIterator = allLinkerSite.iterator();
                int partStart = 0;
                int partEnd = 0;
                Pair pair=null;
                if (pairIterator.hasNext()) {
                    //循环切割
                    while (pairIterator.hasNext()) {
                        pair = pairIterator.next();
                        partEnd = pair.getStart();
                        if (partEnd - partStart >= MIN_LEN) {
                            aline = (read.getName().split("\\s")[0] + "_" + partStart + "-" + partEnd + "-" + read.getSeq().length() + "\n"
                                    + read.getSeq().substring(partStart, partEnd) + "\n"
                                    + read.getAdded() + "\n"
                                    + read.getQuality().substring(partStart, partEnd)
                            );
                            writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                            numS++;
                        }
                        partStart = pair.getEnd();
                    }
                } else {
                    //不需要切割
                    aline = (read.getName().split("\\s")[0]+"_"+0+"-"+read.getSeq().length()+"-"+read.getSeq().length()+"\n"
                            +read.getSeq()+"\n"
                            +read.getAdded()+"\n"
                            +read.getQuality()
                    );
                    writer.write((aline+"\n").getBytes(StandardCharsets.UTF_8));
                    numS++;
                }

                //输出最后一个酶切位点到序列结尾的片段
                if (pair!=null && pair.getEnd() < read.getSeq().length()) {
                    if (read.getSeq().length() - partStart >= 50) {
                        aline = (read.getName().split("\\s")[0] + "_" + partStart + "-" + read.getSeq().length() + "-" + read.getSeq().length() + "\n"
                                + read.getSeq().substring(partStart, read.getSeq().length()) + "\n"
                                + read.getAdded() + "\n"
                                + read.getQuality().substring(partStart, read.getSeq().length())
                        );
                        writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                        numS++;
                    }
                }
            }
            //读取reads的四个信息
            switch ((int) (num%4)){
                case 1:
                    read.setName(line);
                    break;
                case 2:
                    read.setSeq(line);
                    break;
                case 0:
                    read.setQuality(line);
                    break;
                case 3:
                    read.setAdded(line);
                    break;
            }
        }


        //处理最后一个
        {
            numR ++;
            if (numR%1000000==0){
                System.out.println(MyUtil.getTimeFormatted()+" " +
                        "INFO: "+numR+" reads splited...");
            }
            //初始化的分割的起始位置
            //模拟酶切并输出模拟酶切片段，直到无法找到新的酶切位点
            List<Pair> allLinkerSite = findAllLinkerSite(read.getSeq(), linker);
            Iterator<Pair> pairIterator = allLinkerSite.iterator();
            int partStart = 0;
            int partEnd = 0;
            Pair pair=null;
            if (pairIterator.hasNext()) {
                //循环切割
                while (pairIterator.hasNext()) {
                    pair = pairIterator.next();
                    partEnd = pair.getStart();
                    if (partEnd - partStart >= MIN_LEN) {
                        aline = (read.getName().split("\\s")[0] + "_" + partStart + "-" + partEnd + "-" + read.getSeq().length() + "\n"
                                + read.getSeq().substring(partStart, partEnd) + "\n"
                                + read.getAdded() + "\n"
                                + read.getQuality().substring(partStart, partEnd)
                        );
                        writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                        numS++;
                    }
                    partStart = pair.getEnd();
                }
            } else {
                //不需要切割
                aline = (read.getName().split("\\s")[0]+"_"+0+"-"+read.getSeq().length()+"-"+read.getSeq().length()+"\n"
                        +read.getSeq()+"\n"
                        +read.getAdded()+"\n"
                        +read.getQuality()
                );
                writer.write((aline+"\n").getBytes(StandardCharsets.UTF_8));
                numS++;
            }

            //输出最后一个酶切位点到序列结尾的片段
            if (pair!=null && pair.getEnd() < read.getSeq().length()) {
                if (read.getSeq().length() - partStart >= 50) {
                    aline = (read.getName().split("\\s")[0] + "_" + partStart + "-" + read.getSeq().length() + "-" + read.getSeq().length() + "\n"
                            + read.getSeq().substring(partStart, read.getSeq().length()) + "\n"
                            + read.getAdded() + "\n"
                            + read.getQuality().substring(partStart, read.getSeq().length())
                    );
                    writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                    numS++;
                }
            }
        }

        System.out.println();
        System.out.println("[result stats] num of reads: "+numR);
        System.out.println("[result stats] num of splited reads: "+numS);

        reader.close();
        writer.close();
    }
}
