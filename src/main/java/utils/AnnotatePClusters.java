package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.apache.commons.cli.*;
import process.contact.ReadAT;
import utils.entity.PCluster;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotatePClusters {
    static Long getRangeLength(Range<Long> range){
        return range.upperEndpoint()-range.lowerEndpoint();
    }

    public static void loadRegionFile(
            String restrictionFile,
            Map<String, RangeMap<Long,String>> rangeMapMap,
//            Map<Long,String> mapRange,
            Long columnI
    ) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(new File(restrictionFile)));

        long count = 0;
        String line;
        while ((line=reader.readLine())!=null){
            count ++;
            String[] fields = line.split("\t");
            String chrName = fields[0];
            long start = Long.parseLong(fields[1]);
            long end = Long.parseLong(fields[2])-1;
            String prefix = fields[Math.toIntExact(columnI)];
//            String id = prefix+"_"+chrName+":"+start+"-"+end;
            String id = prefix;
            if (rangeMapMap.containsKey(chrName)) {
                rangeMapMap.get(chrName).put(Range.closed(start, end),id);
            } else {
                System.out.println("\t"+chrName+" part was constructed ...");
                rangeMapMap.put(chrName, TreeRangeMap.create());
                rangeMapMap.get(chrName).put(Range.closed(start, end),id);
            }
//            mapRange.put(Long.parseLong(id), fields[0]+"-"+fields[1]+"-"+fields[2] );
        }
        reader.close();
        System.out.println();
    }



    public static void main(String[] args) throws IOException {

        String pcF="";
        String oF="";
        String[] annoFiles = new String[]{};
        String[] annoPrefixes = new String[]{};
        Boolean overlapFlag = false;

        try {
            // create Options object
            System.out.println("hello");
            Options options = new Options();
            options.addOption(new Option("pc", "pclusters", true, "path to pclusters file"));
            options.addOption(new Option("o", "outputFile", true, "path to pclusters file with annotation"));
            options.addOption(new Option("a", "annotations", true, "annotaion1,annoation2,...,annoationN"));
            options.addOption(new Option("at", "columnPrefix", true, "columnNumOfPrefix1,columnNumOfPrefix2,...,columnNumOfPrefixN"));
            options.addOption(new Option("overlaps", "outOverlapsLen", false, "whether the length of overlap region need to be outputted"));
            options.addOption(new Option("pvalue", "pvalue", false, "whether calculating pvalue"));

            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println(cmd.getOptionValue("pc"));
            System.out.println(cmd.getOptionValue("o"));
            System.out.println(cmd.getOptionValue("a"));
            if (!(cmd.hasOption("pc") || cmd.hasOption("o") || cmd.hasOption("a"))) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "AntOptsCommonsCLI", options );
                System.out.println();
                System.exit(0);
            }
            pcF = cmd.getOptionValue("pc");
            oF = cmd.getOptionValue("o");
            annoFiles = cmd.getOptionValue("a").split(",");
            if (cmd.hasOption("at")){
                annoPrefixes = cmd.getOptionValue("at").split(",");
                if (annoPrefixes.length!=annoFiles.length){
                    System.out.println("ERROR: annotations num must be same with prefixes num");
                    System.exit(0);
                }
            } else {
                annoPrefixes = new String[annoFiles.length];
                for (String annoPrefix :
                        annoPrefixes) {
                    annoPrefix = "";
                }
            }
            if (cmd.hasOption("overlaps")){
                overlapFlag = true;
            }
        } catch (Exception ex) {
            System.out.println( "Unexpected exception:" + ex.getMessage() );
            System.exit(0);
        }

//        System.out.println("INFO: inputfile is, " + mF);
//        System.out.println("INFO: outputfile is, " + oF);
//        System.out.println("INFO: virtual fragments file is, " + annoFiles.toString());


        BufferedReader reader;
        //按染色体存储注释区间
        List<Map<String, RangeMap<Long,String>>> allAnnoMapMap = new ArrayList<>();
        for (String annoFile :
                annoFiles) {
            Map<String, RangeMap<Long,String>> rangeMapMap = new HashMap<String,RangeMap<Long,String>>();
            allAnnoMapMap.add(rangeMapMap);
        }
        RangeMap<Long,String> rangeMap ;
        Long dis;
        long count = 0;
        String line;

        //读取注释文件
        for (int i=0; i<allAnnoMapMap.size(); i++) {
            System.out.println(MyUtil.getTimeFormatted()+" " +
                    "loadRegionFile: loading "+i+" file");
            loadRegionFile(
                    annoFiles[i],
                    allAnnoMapMap.get(i),
                    Long.parseLong(annoPrefixes[i]) - 1
            );
//            System.out.println("allAnnoMapMap = " + allAnnoMapMap.get(i).get("chr1").asMapOfRanges().keySet().toArray()[0]);
        }

        //开始注释
        count=0;
        System.out.println("2. Annotating");
        Map<String,Long> statsNumByAnno = new HashMap<>();
        reader = new BufferedReader(new FileReader(pcF));
        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF));
        Judgement judgement = new Judgement();
        TreeRangeMap<Long, String> nullMap = TreeRangeMap.create();

        while ((line=reader.readLine())!=null){
            PCluster onePCluster = new PCluster(line);
            //收集所有的注释
            String annoInfo="";
            //为每个anchor注释
            for (Region anchor :
                    onePCluster.getAnchors()) {
                String annoInfoOneAnchor="";
                Range<Long> range = Range.closed(anchor.getStart(), anchor.getEnd()-1);
                for (Map<String, RangeMap<Long,String>> rangeMapMap:
                        allAnnoMapMap ) {
                    String annoInfoOneAnchorOneAnno="";
                    rangeMap = rangeMapMap.get(anchor.getChr());
                    //求交集区间
                    RangeMap<Long, String> overlappingRangeMap = nullMap;
                    if (rangeMap != null)
                        overlappingRangeMap = rangeMap.subRangeMap(range);

                    for (Map.Entry<Range<Long>, String> entry:
                            overlappingRangeMap.asMapOfRanges().entrySet()) {
                        annoInfoOneAnchorOneAnno+=","+entry.getValue();
                        if (overlapFlag){
                            annoInfoOneAnchorOneAnno+=":"+getRangeLength(entry.getKey());
                        }
                    }
                    annoInfoOneAnchorOneAnno=annoInfoOneAnchorOneAnno.trim();
                    annoInfoOneAnchorOneAnno=annoInfoOneAnchorOneAnno.replaceAll("^[,;\\|]+", "").replaceAll("^[,;\\|]+$", "");
                    annoInfoOneAnchorOneAnno = annoInfoOneAnchorOneAnno.isEmpty()?"None":annoInfoOneAnchorOneAnno;
                    annoInfoOneAnchor=annoInfoOneAnchor+";"+annoInfoOneAnchorOneAnno;
                }
                annoInfoOneAnchor=annoInfoOneAnchor.trim();
                annoInfoOneAnchor=annoInfoOneAnchor.replaceAll("^[,;\\|]+", "").replaceAll("^[,;\\|]+$", "");
                annoInfo=annoInfo+"|"+annoInfoOneAnchor;
            }
            annoInfo=annoInfo.trim();
            annoInfo=annoInfo.replaceAll("^\\|+", "").replaceAll("\\|+$", "");
            onePCluster.setAnnoations(annoInfo);
            String outLine = onePCluster.toString();
            //写入输出文件
            oWriter.write(outLine);
            oWriter.newLine();
            count++;
            if (count%1000000==0)
                System.out.println("\t"+count+" mappings was handled ...");
        }
        oWriter.close();
        System.out.println("2. Annotation finished");
        System.out.println();


        //打印基本统计信息
//        System.out.println("FragNum\tMapCount");
//        for (Map.Entry<Long, Long> entry:
//                statsNum.entrySet()) {
//            System.out.println(entry.getKey()+"\t"+ entry.getValue());
//        }
    }
}
