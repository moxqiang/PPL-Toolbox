package utils;

import htsjdk.samtools.*;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.commons.cli.*;
import process.mapping.ReadBed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static utils.AssignFragment.assignFrag;
import static utils.AssignFragment.loadResSiteFile;

public class GenotypingQC {
    public static void main(String[] args) throws IOException {
        String vF="";
        String iF="";
        String oF="";
        int numL = 10000;
        int numLP = 10000;
        String sampleName = "";

        try {
            // create Options object
            Options options = new Options();
            options.addOption(new Option("v", "vars", true, "path to Phased SNPs file"));
            options.addOption(new Option("i", "bam", true, "path to alignments file (.bam)"));
            options.addOption(new Option("o", "out", true, "prefix to distribution (.txt)"));
            options.addOption(new Option("n", "sampleName", true, "sample name in .vcf file"));
            options.addOption(new Option("l", "num_line", true, "num of line used to QC"));

            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println(cmd.getOptionValue("i"));
            System.out.println(cmd.getOptionValue("v"));
            System.out.println(cmd.getOptionValue("o"));
            System.out.println(cmd.getOptionValue("l"));
            if (!(cmd.hasOption("i") || cmd.hasOption("v") || cmd.hasOption("o"))) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "AntOptsCommonsCLI", options );
                System.out.println();
                System.exit(0);
            }
            iF = cmd.getOptionValue("i");
            oF = cmd.getOptionValue("o");
            vF = cmd.getOptionValue("v");
            if (cmd.hasOption("n")) {
                sampleName = cmd.getOptionValue("n");
            }
            if (cmd.hasOption("l")) {
                numL = Integer.parseInt(cmd.getOptionValue("l"));
            }
        } catch (Exception ex) {
            System.out.println( "Unexpected exception:" + ex.getMessage() );
            System.exit(0);
        }

        //读取vcf和bam
        VCFFileReader variantContexts = new VCFFileReader(new File(vF));
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        //如果不提供sampleName,则默认使用第一个sample
        if (sampleName.equalsIgnoreCase("")) {
            sampleName = variantContexts.getFileHeader().getGenotypeSamples().get(0);
            System.out.println("INFO: sample name is not given, using default first sample = " + sampleName);
            System.out.println("INFO: all sample names listed: ");
            int index=0;
            for (String name :
                    variantContexts.getFileHeader().getGenotypeSamples()) {
                System.out.println("\t"+index+": " + name);
            }

        }
        BufferedWriter oWriter1 = new BufferedWriter(new FileWriter(oF+"1.txt"));
        BufferedWriter oWriter2 = new BufferedWriter(new FileWriter(oF+"2.txt"));
        BufferedWriter oWriter3 = new BufferedWriter(new FileWriter(oF+"byBaseQ.txt"));
        BufferedWriter oWriter4 = new BufferedWriter(new FileWriter(oF+"byMapQ.txt"));
        BufferedWriter oWriterErr = new BufferedWriter(new FileWriter(oF+"err.vcf"));

        //逐个记录读取并判断
        int recordIndex = 0;

        SAMRecordIterator samRecordIterator = samReader.iterator();
        String preReadName = "";
        String readName = "";
        String line;            //status
        int countUsefulSNPsByRead = 0;
        int countUsefulSNPs=0;
        int countUsefulSNPsFromPhase1 = 0;
        int countUsefulSNPsFromPhase2 = 0;
        int countUnPhased = 0;

        int countIncluded=0;
        int countExcluded=0;
        int countError=0;

        Map<Byte, Integer> countIncludedByBaseQ = new HashMap<>();
        Map<Byte, Integer> countExcludedByBaseQ = new HashMap<>();
        Map<Integer, Integer> countIncludedByMapQ = new HashMap<>();
        Map<Integer, Integer> countExcludedByMapQ = new HashMap<>();
        for (byte i = -1; i <= 93; i++) {
            countIncludedByBaseQ.put(i,0);
            countExcludedByBaseQ.put(i,0);
        }
        for (Integer i = 0; i <= 60; i++) {
            countIncludedByMapQ.put(i,0);
            countExcludedByMapQ.put(i,0);
        }

        while (samRecordIterator.hasNext()){
            try {
                if ((recordIndex+1)%numLP==0){
                    System.out.println("recordIndex = " + recordIndex);
                }
                if ((recordIndex+1)%numL==0){
                    System.out.println("recordIndex = " + recordIndex);
                    break;
                }
                SAMRecord record = samRecordIterator.next();
                int[] hardClipLengths = getHardClipLengths(record);
                preReadName = readName;
                readName = record.getReadName();
                if (preReadName!="" && preReadName.equals(readName)){
                    line=readName + "\t"
                            + countUsefulSNPsByRead;
                    oWriter2.write(line);
                    oWriter2.newLine();
                    countUsefulSNPsByRead=0;
                }
                if (record.getReadUnmappedFlag()) {
                    line=recordIndex + "\tUNMAPPED";
                    oWriter1.write(line);
                    oWriter1.newLine();
                    recordIndex++;
                }else {
                    //计算当前记录上的有效SNP数量
                    countUsefulSNPs = 0;
                    for (VariantContext variantContext : variantContexts.query(record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd()).toList()) {
                        if (variantContext.isSNP()) {
                            Genotype genotype = null;
                            try {
                                genotype = variantContext.getGenotype(sampleName);
                                String gtString = genotype.getGenotypeString();
                                boolean isPhased = gtString.contains("|");
                                if (genotype.isHet()&&
                                        isPhased) {
//                                    System.out.println(variantContext.getCalledChrCount()+ "\t"+ variantContext.getStart() +"\t"+ gtString);
                                    String base = getBasePositiveStrandByPos(record, variantContext.getStart());
                                    Byte baseQ = getBaseQualityPositiveStrandByPos(record, variantContext.getStart());
                                    int mapQ = record.getMappingQuality();
//                                System.out.println("readName = " + readName);
//                                System.out.println("variantContext = " + record.getReferenceName() + ":" + variantContext.getStart() +" "+ getStrand(record));
//                                System.out.println(genotype.getGenotypeString(true));
//                                System.out.println(base);
//                                boolean isIncluded = Arrays.asList(genotype.getGenotypeString().split("/")).contains(base);
                                    boolean isIncluded = Arrays.asList(genotype.getGenotypeString().split("\\|")).contains(base);
                                    List<String> genotypes = Arrays.asList(genotype.getGenotypeString().split("\\|")); // phased snps
//                                    System.out.println(gtString);
//                                    System.out.println(genotypes);
                                    if (base.equalsIgnoreCase(genotypes.get(0))){
                                        countUsefulSNPsFromPhase1++;
                                    }else if (base.equalsIgnoreCase(genotypes.get(1))){
                                        countUsefulSNPsFromPhase2++;
                                    }else {
                                        countUnPhased++;
                                    }
//                                System.out.println(isIncluded);

                                    try {
                                        if (isIncluded) {
                                            countIncluded++;
                                            countIncludedByBaseQ.put(baseQ, countIncludedByBaseQ.get(baseQ)+1);
                                            countIncludedByMapQ.put(mapQ, countIncludedByMapQ.get(mapQ)+1);
                                        }
                                        else {
                                            countExcluded++;
                                            countExcludedByBaseQ.put(baseQ, countExcludedByBaseQ.get(baseQ)+1);
                                            countExcludedByMapQ.put(mapQ, countExcludedByMapQ.get(mapQ)+1);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.out.println(baseQ);
                                    }

                                    countUsefulSNPs++;
                                    countUsefulSNPsByRead++;
                                }
                            } catch (htsjdk.tribble.TribbleException e) {
//                                e.printStackTrace();
                                countError++;
                                oWriterErr.write(variantContext.toString());
                                oWriterErr.newLine();
                            }
                        }
                    }


                    line=recordIndex + "\t"
                            + record.getReferenceName() + "\t"
                            + getStrand(record) + '\t'
                            + record.getStart() + "\t"
                            + record.getEnd() + "\t"
                            + record.getReadName() + "\t"
                            + calculateReadStart(record) + "\t"
                            + calculateReadEnd(record) + "\t"
                            + calculateOriginalReadLength(record) + "\t"
                            + record.getMappingQuality() + "\t"
                            + countUsefulSNPs;
                    oWriter1.write(line);
                    oWriter1.newLine();
                    recordIndex++;
                }
                //输出
            } catch (SAMFormatException e) {
                e.printStackTrace();
            }
        }

        System.out.println("countIncluded = " + countIncluded);
        System.out.println("countExcluded = " + countExcluded);
        System.out.println("countError = " + countError);
        System.out.println();
        System.out.println("countUsefulSNPsFromPhase1 = " + countUsefulSNPsFromPhase1);
        System.out.println("countUsefulSNPsFromPhase2 = " + countUsefulSNPsFromPhase2);
        System.out.println("countError = " + countError);

//        printSortedMap(countIncludedByBaseQ);
        System.out.println();
//        printSortedMap(countExcludedByBaseQ);
//        printSortedMapByPair(countIncludedByBaseQ, countExcludedByBaseQ);
        printSortedMapToFileByPair(countIncludedByBaseQ, countExcludedByBaseQ, oWriter3);
        printSortedMapToFileByPair(countIncludedByMapQ, countExcludedByMapQ, oWriter4);


        oWriter1.close();
        oWriter2.close();
        oWriter3.close();
        oWriter4.close();
        oWriterErr.close();
        variantContexts.close();
    }

    public static void printSortedMap(Map<Byte, Integer> map) {
        // 使用 TreeMap 对 Map 的键进行排序
        TreeMap<Byte, Integer> sortedMap = new TreeMap<>(map);

        // 循环打印排序后的键值对
        for (Map.Entry<Byte, Integer> entry : sortedMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    public static void printSortedMapByPair(Map<Byte, Integer> map1, Map<Byte, Integer> map2) {
        // 使用 TreeMap 对 Map 的键进行排序
        TreeMap<Byte, Integer> sortedMap1 = new TreeMap<>(map1);
        TreeMap<Byte, Integer> sortedMap2 = new TreeMap<>(map2);

        // 循环打印排序后的键值对
        for (Byte key : sortedMap1.keySet()) {
            String line = (key + "\t" + sortedMap1.get(key) + "\t" +  sortedMap2.get(key));
            System.out.println(line);
        }
    }

    public static <T> void printSortedMapToFileByPair (Map<T, Integer> map1, Map<T, Integer> map2, BufferedWriter writer) throws IOException {
        // 使用 TreeMap 对 Map 的键进行排序
        TreeMap<T, Integer> sortedMap1 = new TreeMap<>(map1);
        TreeMap<T, Integer> sortedMap2 = new TreeMap<>(map2);

        // 循环打印排序后的键值对
        for (T key : sortedMap1.keySet()) {
            if (sortedMap1.get(key) ==0 && sortedMap2.get(key) ==0) continue;
            String line = (key + "\t" + sortedMap1.get(key) + "\t" +  sortedMap2.get(key) + "\t" + ((double) sortedMap1.get(key)/(sortedMap1.get(key)+sortedMap2.get(key))));
            writer.write(line);
            writer.newLine();
        }
    }

    private static int calculateReadStart(SAMRecord record){
        int[] hardClipLengths = getHardClipLengths(record);
        if (record.getReadNegativeStrandFlag()){
            int startPos = record.getReadPositionAtReferencePosition(record.getEnd()) + hardClipLengths[0];
            return calculateOriginalReadLength(record) - startPos;
        }
        int startPos = record.getReadPositionAtReferencePosition(record.getStart()) + hardClipLengths[0];
        return startPos;
    }

    private static int calculateReadEnd(SAMRecord record){
        int[] hardClipLengths = getHardClipLengths(record);
        if (record.getReadNegativeStrandFlag()){
            int endPos = record.getReadPositionAtReferencePosition(record.getStart()) + hardClipLengths[0];
            return calculateOriginalReadLength(record) - endPos;
        }
        int endPos = record.getReadPositionAtReferencePosition(record.getEnd()) + hardClipLengths[0];
        return endPos;
    }

    private static int calculateOriginalReadLength(SAMRecord record){
        return (record.getReadLength() + calculateSum(getHardClipLengths(record)));
    }

    private static String toOppositeBase(String base){
        switch (base){
            case "A" :
                return "T";
            case "a" :
                return "t";
            case "T" :
                return "A";
            case "t" :
                return "a";
            case "G" :
                return "C";
            case "g" :
                return "c";
            case "C" :
                return "G";
            case "c" :
                return "g";
            default:
                return base;
        }
    }

    //获取指定为genome位置的碱基
    private static String getBasePositiveStrandByPos(SAMRecord record, int genomePos) {
        int readPos = record.getReadPositionAtReferencePosition(genomePos);
        if (readPos == 0){
            return "*";
        }
        if (readPos > record.getReadLength()){
            return "*";
        }
        String base = (char) record.getReadBases()[readPos - 1] +"";
        return base;
    }

    //获取指定为genome位置的碱基质量
    private static Byte getBaseQualityPositiveStrandByPos(SAMRecord record, int genomePos) {
        int readPos = record.getReadPositionAtReferencePosition(genomePos);
        if (readPos == 0){
            return -1;
        }
        if (readPos > record.getReadLength()){
            return -1;
        }
        Byte baseQ = record.getBaseQualities()[readPos - 1];
        return baseQ;
    }

    private static String getStrand(SAMRecord record) {
        if (record.getReadNegativeStrandFlag()) {
            return "-";
        } else {
            return "+";
        }
    }

    public static int calculateSum(int[] numbers) {
        int sum = 0;

        for (int number : numbers) {
            sum += number;
        }

        return sum;
    }

    public static int[] getHardClipLengths(SAMRecord samRecord) {
        String cigarString = samRecord.getCigarString();
        Cigar cigar = samRecord.getCigar();
        int softClipStart = 0;
        int softClipEnd = 0;

        //当且仅当 hard clipped 才需要计算左右clip长度
        if (cigar.isLeftClipped() && cigar.getFirstCigarElement().getOperator().equals(CigarOperator.HARD_CLIP)){
            CigarElement firstCigarElement = cigar.getFirstCigarElement();
            softClipStart = firstCigarElement.getLength();
        }

        if (cigar.isRightClipped() && cigar.getLastCigarElement().getOperator().equals(CigarOperator.HARD_CLIP)){
            CigarElement lastCigarElement = cigar.getLastCigarElement();
            softClipEnd = lastCigarElement.getLength();
        }

        int[] softClipLengths = { softClipStart, softClipEnd };

//        System.out.println(Arrays.toString(softClipLengths));

        return softClipLengths;
    }
}
