package utils;

import com.google.common.collect.Maps;
import utils.entity.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SWScoreStats {

    public static void statsOneMatrix(Map<Integer, Integer> countStats, String read, String linker){
        int[][] scoreMatrix = SmithWaterman.computeScoreMatrix(read, linker);
        for (int i = 0; i < scoreMatrix.length; i++) {
            for (int j = 0; j < scoreMatrix[0].length; j++) {
                if (countStats.containsKey(scoreMatrix[i][j])) {
                    countStats.put(scoreMatrix[i][j],
                            countStats.get(scoreMatrix[i][j])+1
                    );
                }
                else {
                    countStats.put(scoreMatrix[i][j],
                            1
                    );
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.SWScoreStats <fastq> <Linker>");
            System.exit(0);
        }
        String fq = args[0];
        MyUtil.checkPath(fq);
        String linker = args[1];
        System.out.println(
                "[input fastq file] " + fq);
        System.out.println(
                "[linker sequence info] " + linker);

        BufferedReader reader;
        BufferedOutputStream writer;

        if (MyUtil.isGZipped(new File(fq))) {
            reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(fq))));
        } else {
            reader = new BufferedReader((
                    new FileReader(
                            new File(fq)
                    )
            ));
        }
        String line;
        String aline;
        long num = 0;
        long numS = 0;
        long numR = 0;
        Read read = new Read();
        Map<Integer, Integer> countStats = new HashMap<>();

        //读取fq
        while((line=reader.readLine())!=null) {
            num++;
            //读取一条read的四行后开始处理
            if (num != 1 && num % 4 == 1) {
                numR++;
                if (numR % 1000000 == 0) {
                    System.out.println(MyUtil.getTimeFormatted() + " " +
                            "INFO: " + numR + " reads splited...");
                }
                //初始化的分割的起始位置
                //模拟酶切并输出模拟酶切片段，直到无法找到新的酶切位点
                statsOneMatrix(countStats, read.getSeq(),linker);
            }
            //读取reads的四个信息
            switch ((int) (num % 4)) {
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
        SortedSet<Integer> keys = new TreeSet<>(countStats.keySet());
        for (Integer key : keys) {
            Integer value = countStats.get(key);
            // do something
            System.out.println(key+"\t"+value);
        }
    }
}

