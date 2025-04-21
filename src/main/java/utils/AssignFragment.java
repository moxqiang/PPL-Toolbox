package utils;

import com.google.common.collect.*;
import errors.MyError;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.K;
import process.contact.ReadAT;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AssignFragment {
    static Long getRangeLength(Range<Long> range){
        return range.upperEndpoint()-range.lowerEndpoint();
    }

    public static void loadResSiteFile(
            String restrictionFile,
            Map<String, RangeMap<Long,Long>> rangeMapMap,
            Map<Long,String> mapRange
    ) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(new File(restrictionFile)));
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "loadResSiteFile: loading virtual restriction file");
        long count = 0;
        String line;
        while ((line=reader.readLine())!=null){
            count ++;
            String[] fields = line.split("\t");
            if (rangeMapMap.containsKey(fields[0])) {
                rangeMapMap.get(fields[0]).put(Range.closed(Long.parseLong(fields[1]), Long.parseLong(fields[2])-1),Long.parseLong(fields[4]));
            } else {
                System.out.println("\t"+fields[0]+" part was constructed ...");
                rangeMapMap.put(fields[0], TreeRangeMap.create());
                rangeMapMap.get(fields[0]).put(Range.closed(Long.parseLong(fields[1]), Long.parseLong(fields[2])-1),Long.parseLong(fields[4]));
            }
            mapRange.put(Long.parseLong(fields[4]), fields[0]+"-"+fields[1]+"-"+fields[2] );
        }
        reader.close();
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "loadResSiteFile: " + count + " of virtual restriction fragments was loaded");
        System.out.println();
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

    public static String assignFrag(
            String line,
            Map<String, RangeMap<Long,Long>> rangeMapMap,
            Map<Long,String> mapRange
    ) throws IOException {
        double cutoff=0.5;
        String flag = "ratio";
        String flag2 = "a";

        return assignFrag(
                line,
                rangeMapMap,
                mapRange,
                cutoff,
                flag,
                flag2
        );

    }

    public static void main(String[] args) throws IOException {

        if (args.length < 3){
            System.out.println(
                    "Usage: java -cp pore-c_tool.jar utils.AssignFragment <monomers> <RestrictionSite> <outputFile> \\ " +
                    "   <cutoff> (optional, " +
                    "       eg. 20 or 0.5, default 0.5) \\" +
                    "   <a/b> (optional (when <cutoff> used), " +
                    "       a: bp mode, cutoff>=1 needed; " +
                    "       b: ratio mode, 0>=cutoff<=1 needed; " +
                    "       default a;" +
                    "   ) ");
            System.exit(0);
        }

        String mF = args[0];
        String restrictionFile = args[1];
        String oF = args[2];
        String flag = "ratio";
        String flag2 = "a";
        double cutoff=0.5;
        if (args.length>=4) {
            MyUtil.checkNum(args[3]);
            cutoff = Double.parseDouble(args[3]);
            if (cutoff >= 1){
                flag = "bp";
            }
        }
        if (args.length>=5) {
            flag2 = args[4];
            if (!(flag2.equalsIgnoreCase("a")||flag2.equalsIgnoreCase("b"))) {
                System.out.println(
                        "Usage: java -cp pore-c_tool.jar utils.AssignFragment <monomers> <RestrictionSite> <outputFile> \\ " +
                                "   <cutoff> (optional, " +
                                "       eg. 20 or 0.5, default 0.5) \\" +
                                "   <a/b> (optional (when <cutoff> used), " +
                                "       a: bp mode, cutoff>=1 needed; " +
                                "       b: ratio mode, 0>=cutoff<=1 needed; " +
                                "       default a;" +
                                "   ) ");
                System.exit(0);
            }
        }
        if (flag.equalsIgnoreCase("ratio")){
            System.out.println("INFO: ratio mode running, cutoff = "+cutoff);
            System.out.println();
        } else {
            System.out.println("INFO: bp mode running, cutoff = "+cutoff);
            System.out.println();
        }
        if (flag2.equalsIgnoreCase("a")) {
            System.out.println("NOTE: one mapping will be assigned one fragment at least !!\n" +
                               "      if you don't want that, add 'b' argument at the end of command.");
            System.out.println();
        }

        System.out.println("INFO: input file is, " + mF);
        System.out.println("INFO: virtual fragments file is, " + restrictionFile);
        System.out.println("INFO: output file is, " + oF);


        BufferedReader reader;
        //按染色体存储酶切区间
//            Map<String, RangeSet<Long>> rangeSetMap = new HashMap<String,RangeSet<Long>>();
        Map<String, RangeMap<Long,Long>> rangeMapMap = new HashMap<String,RangeMap<Long,Long>>();
//            RangeSet<Long> rangeSet ;
        Map<Long,String> mapRange = new HashMap<Long,String>();
        RangeMap<Long,Long> rangeMap ;
        Long dis;
        long count = 0;
        String line;

        //读取酶切文件
        loadResSiteFile(
                restrictionFile,
                rangeMapMap,
                mapRange
        );
//        System.out.println("1. Loading virtual restriction file");
//        String line;
//        while ((line=reader.readLine())!=null){
//            count ++;
//            String[] fields = line.split("\t");
//            if (rangeMapMap.containsKey(fields[0])) {
//                rangeMapMap.get(fields[0]).put(Range.closed(Long.parseLong(fields[1]), Long.parseLong(fields[2])-1),Long.parseLong(fields[4]));
//            } else {
//                System.out.println("\t"+fields[0]+" part was constructed ...");
//                rangeMapMap.put(fields[0], TreeRangeMap.create());
//                rangeMapMap.get(fields[0]).put(Range.closed(Long.parseLong(fields[1]), Long.parseLong(fields[2])-1),Long.parseLong(fields[4]));
//            }
//            mapRange.put(Long.parseLong(fields[4]), fields[0]+"-"+fields[1]+"-"+fields[2] );
//        }
//        reader.close();
//        System.out.println("1. " + count + " of virtual restriction fragments was loaded");
//        System.out.println();

        //匹配酶切末端
        count=0;
        System.out.println("2. Assigning fragments to mappings");
        Map<Long,Long> statsNum = new HashMap<Long,Long>();
        reader = new BufferedReader(new FileReader(mF));
        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF));
        int infoFlag = 0;
        while ((line=reader.readLine())!=null){
            String[] fields = line.split("\t");
            if(infoFlag == 0 && fields.length > 11){
                System.out.println("WARNING: fragment information existed, this process will igore it!!!");
                infoFlag++;
            }
            ReadAT at = new ReadAT(fields);
//                rangeSet = rangeSetMap.get(at.chr);
            Range<Long> range = Range.closed(at.start, at.end-1);
            rangeMap = rangeMapMap.get(at.chr);

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
            if (statsNum.containsKey(countF)) {
                statsNum.put(countF, statsNum.get(countF) + 1);
            } else {
                statsNum.put(countF, (long) 1);
            }

            String outLine;
            if (infoFlag == 0){
                outLine = line + "\t";
            }else {
                //已有fragInfo
                outLine = StringUtils.substringBeforeLast(line, "\t") + "\t";
            }
            for (long i = frgL; i <= frgR; i++) {
                outLine += i + ":" + mapRange.get(i) + ",";
            }
            outLine = outLine.substring(0, outLine.length() - 1);
            //写入输出文件
            oWriter.write(outLine);
            oWriter.newLine();
            count++;
            if (count%1000000==0)
                System.out.println("\t"+count+" mappings was handling ...");
        }
        oWriter.close();
        System.out.println("2. Assigning finished");
        System.out.println();


        //打印基本统计信息
        System.out.println("FragNum\tMapCount");
        for (Map.Entry<Long, Long> entry:
            statsNum.entrySet()) {
            System.out.println(entry.getKey()+"\t"+ entry.getValue());
        }

    }
}
