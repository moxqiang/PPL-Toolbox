package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import process.contact.ReadAT;
import utils.MyUtil;

import java.io.*;
import java.util.*;

public class AnnotateContacts {
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

    public static String annotateRegion(
            String line,
            Map<String, RangeMap<Long,Long>> rangeMapMap,
            Map<Long,String> mapRange
    ) throws IOException {
        double cutoff=1;
        String flag = "bp"; //使用bp模式
        String flag2 = "b"; // 可以不注释到任何东西

        return assignFrag(
                line,
                rangeMapMap,
                mapRange,
                cutoff,
                flag,
                flag2
        );

    }


    public static String assignFrag(
            String line,
            Map<String, RangeMap<Long,Long>> rangeMapMap,
            Map<Long,String> mapRange,
            double cutoff,
            String flag,
            String flag2
    ) throws IOException {
        RangeMap<Long,Long> rangeMap ;

        String[] fields = line.split("\t");
        ReadAT at = new ReadAT(fields);
        Range<Long> range = Range.closed(at.start, at.end-1);
        rangeMap = rangeMapMap.get(at.chr);
        String outLine = "";

        Map.Entry<Range<Long>,Long> eL = rangeMap.getEntry(range.lowerEndpoint() );
        Map.Entry<Range<Long>,Long> eR = rangeMap.getEntry(range.upperEndpoint() );
        long frgL=-1;
        long frgR=-1;
        if (eL==null || eR==null){
            //没有与之overlap的酶切区间，报警
            System.out.println("WARNING: No overlap,[[" + line + "]] can't be assigned to any fragments!!!!");
        } else {
            frgL = eL.getValue();
            frgR = eR.getValue();

            if (flag.equalsIgnoreCase("bp")) {
                //bp模式
                if (getRangeLength(eL.getKey().intersection(range)) < cutoff) {
                    frgL++;
                }
                if (getRangeLength(eR.getKey().intersection(range)) < cutoff) {
                    frgR--;
                }
            } else if (flag.equalsIgnoreCase("ratio")) {
                //ratio模式
                if (getRangeLength(eL.getKey().intersection(range)) < cutoff * getRangeLength(eL.getKey())) {
                    frgL++;
                }
                if (getRangeLength(eR.getKey().intersection(range)) < cutoff * getRangeLength(eR.getKey())) {
                    frgR--;
                }
            }
        }
        //a mode，修饰左右片段编号，防止出现0值
        //如果将该段代码注释掉，可能有的mappings将不会被注释到任何的酶切片段
        if (flag2.equalsIgnoreCase("a")) {
            if ( frgR < frgL ){
                if (frgL-frgR==2){
                    frgL=(frgL+frgR)/2;
                    frgR=frgL;
                }else if (frgL-frgR==1){
                    if (getRangeLength(eL.getKey().intersection(range)) / (double) getRangeLength(eL.getKey())
                            >
                            getRangeLength(eR.getKey().intersection(range)) / (double) getRangeLength(eR.getKey())){
                        frgL=frgR= eL.getValue();
                    }else {
                        frgL=frgR= eR.getValue();
                    }
                }else{
                    System.out.println("WARNING: not possible ["+line+"]");
                }
            }
        }
        long countF = frgR - frgL + 1 > 0 ? frgR - frgL + 1 : 0;
        for (long i = frgL; i <= frgR; i++) {
            outLine += i + ":" + mapRange.get(i) + ",";
        }
        outLine = line +"\t"+ outLine;
        outLine = outLine.substring(0, outLine.length() - 1);
        return outLine;
    }


    public static void main(String[] args) throws IOException {

        String cF="";
        String oF="";
        String[] annoFiles = new String[]{};
        String[] annoPrefixes = new String[]{};
        Boolean overlapFlag = false;

        try {
            // create Options object
            Options options = new Options();
            options.addOption(new Option("c", "contacts", true, "path to multi-way contacts file"));
            options.addOption(new Option("o", "outputFile", true, "path to multi-way contacts file with annotation"));
            options.addOption(new Option("a", "annotations", true, "annotaion1,annoation2,...,annoationN"));
            options.addOption(new Option("at", "columnPrefix", true, "columnNumOfPrefix1,columnNumOfPrefix2,...,columnNumOfPrefixN"));
            options.addOption(new Option("overlaps", "outOverlapsLen", false, "whether the length of overlap region need to be outputted"));

            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println(cmd.getOptionValue("c"));
            System.out.println(cmd.getOptionValue("o"));
            System.out.println(cmd.getOptionValue("a"));
            if (!(cmd.hasOption("c") || cmd.hasOption("o") || cmd.hasOption("a"))) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "AntOptsCommonsCLI", options );
                System.out.println();
                System.exit(0);
            }
            cF = cmd.getOptionValue("c");
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
            System.out.println("allAnnoMapMap = " + allAnnoMapMap.get(i).get("chr1").asMapOfRanges().keySet().toArray()[0]);
        }

        //开始注释
        count=0;
        System.out.println("2. Annotating");
        Map<String,Long> statsNumByAnno = new HashMap<>();
        reader = new BufferedReader(new FileReader(cF));
        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF));
        Judgement judgement = new Judgement();
        TreeRangeMap<Long, String> nullMap = TreeRangeMap.create();

        while ((line=reader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT at = new ReadAT(fields);
            //收集所有的注释
            String annoInfo="";

            if(!judgement.judgeUnmapped(at)){
                Range<Long> range = Range.closed(at.start, at.end-1);
//                Range<Long> range = Range.closed(at.getMid(), at.getMid());


                for (Map<String, RangeMap<Long,String>> rangeMapMap:
                        allAnnoMapMap ) {
                    String annoInfoTmp="";
                    rangeMap = rangeMapMap.get(at.chr);

                    RangeMap<Long, String> overlappingRangeMap = nullMap;
                    if (rangeMap != null)
                        overlappingRangeMap = rangeMap.subRangeMap(range);


//                    if (overlappingRangeMap.asMapOfRanges().size()>0)
//                        System.out.println(overlappingRangeMap);

                    for (Map.Entry<Range<Long>, String> entry:
                            overlappingRangeMap.asMapOfRanges().entrySet()) {
                        annoInfoTmp+=","+entry.getValue();
                        if (overlapFlag){
                            annoInfoTmp+=":"+getRangeLength(entry.getKey());
                        }
                    }
                    annoInfoTmp=annoInfoTmp.replaceAll("^,+", "").replaceAll(",+$", "");
                    annoInfoTmp=annoInfoTmp.trim();
                    annoInfoTmp = annoInfoTmp.isEmpty()?"None":annoInfoTmp;
                    annoInfo = annoInfo+"\t"+annoInfoTmp;
//                    System.out.println(annoInfo);

//                    statsNumByAnno = ""
                }
            }
            annoInfo=annoInfo.trim();
            String outLine = at.toContactLine()+"\t"+annoInfo;
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
