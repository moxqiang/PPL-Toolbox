package utils;

import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ExtractGaps {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.ExtractGaps <original.fastq> <gaps.fastq> <coverageFile> <minLength>(default 50)");
            System.exit(0);
        }

        //流准备
        String fq = args[0];
        MyUtil.checkPath(fq);
        String fqs = args[1];
        String cover = args[2];
        int cutoff = 50;
        if (args.length==4) {
            cutoff = Integer.parseInt(args[3]);
        }

        BufferedReader reader;
        BufferedReader readerCover = new BufferedReader(new FileReader(new File(cover)));
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

        //读取coverage文件
        System.out.println(MyUtil.getTimeFormatted()+" "+
                "Loading coverage file...");
        Map<String, ReadCover>  map = new HashMap<>();
        String line;
        while((line= readerCover.readLine())!=null){
            String[] fields = line.split("\t");
            if (fields.length == 4) continue;
            ReadCover rc = new ReadCover(fields);
            map.put(rc.getReadName(), rc);
        }
        System.out.println(MyUtil.getTimeFormatted()+" "+
                "loading finished");
        System.out.println();

        //提取gaps of reads
        System.out.println(MyUtil.getTimeFormatted()+" "+
                "Extracting start...");
        long num = 0;
        long numR = 0;
        long numG = 0;
        Read read = new Read();
        String aline;

        while((line= reader.readLine())!=null){
            num ++;
            //读取一条read的四行后开始处理
            if (num!=1 && num%4==1) {
                numR ++;
                if (numR%1000000==0){
                    System.out.println();
                    System.out.println(MyUtil.getTimeFormatted()+" "+
                            "INFO: "+numR+" reads was extracted...");
                }
                //开始切割
                String readName=read.getName().split("\\s")[0].substring(1);
                if (map.containsKey(readName)){
                    for (Range<Integer> range:
                         map.get(readName).getRangeSetDiff().asRanges()) {
                        //最短长度阈值判断
                        if(range.upperEndpoint()- range.lowerEndpoint() < cutoff){
                            continue;
                        }
                        aline = (read.getName().split("\\s")[0]+"_" + range.lowerEndpoint() + "-" + range.upperEndpoint()+"-"+read.getSeq().length()+ "\n"
                                + read.getSeq().substring(range.lowerEndpoint(), range.upperEndpoint()) + "\n"
                                + read.getAdded() + "\n"
                                + read.getQuality().substring(range.lowerEndpoint(), range.upperEndpoint())
                        );
                        writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                        numG++;
                    }

                }else {
                    aline = (read.getName().split("\\s")[0]+"_"+0+"-"+read.getSeq().length()+"-"+read.getSeq().length()+ "\n"
                            + read.getSeq() + "\n"
                            + read.getAdded() + "\n"
                            + read.getQuality()
                    );
                    writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                    numG++;
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
                System.out.println();
                System.out.println(MyUtil.getTimeFormatted()+" "+
                        "INFO: "+numR+" reads was extracted...");
            }
            //开始切割
            String readName=read.getName().split("\\s")[0].substring(1);
            if (map.containsKey(readName)){
                for (Range<Integer> range:
                        map.get(readName).getRangeSetDiff().asRanges()) {
                    //最短长度阈值判断
                    if(range.upperEndpoint()- range.lowerEndpoint() < cutoff){
                        continue;
                    }
                    aline = (read.getName().split("\\s")[0]+"_" + range.lowerEndpoint() + "-" + range.upperEndpoint()+"-"+read.getSeq().length()+ "\n"
                            + read.getSeq().substring(range.lowerEndpoint(), range.upperEndpoint()) + "\n"
                            + read.getAdded() + "\n"
                            + read.getQuality().substring(range.lowerEndpoint(), range.upperEndpoint())
                    );
                    writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                    numG++;
                }

            }else {
                aline = (read.getName().split("\\s")[0]+"_"+0+"-"+read.getSeq().length()+"-"+read.getSeq().length()+ "\n"
                        + read.getSeq() + "\n"
                        + read.getAdded() + "\n"
                        + read.getQuality()
                );
                writer.write((aline + "\n").getBytes(StandardCharsets.UTF_8));
                numG++;
            }
        }

        System.out.println(MyUtil.getTimeFormatted()+" "+
                "Extracting finished");
        System.out.println();
        System.out.println(MyUtil.getTimeFormatted()+" "+
                "Summary: \n" +
                "\tcount of all reads: "+numR+
                "\n\tcount of generated gaps: " +numG);

        //流释放
        reader.close();
        readerCover.close();
        writer.close();
    }
}
