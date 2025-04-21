package utils;


import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceIndexEntry;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.commons.cli.*;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

public class GenotypingBySNP {
    public static void main(String[] args) throws IOException {
        String vF="";
        String iF="";
        String cF="";
        String oF="";
        String referenceFilePath="";
        int numL = 100000;
        int snpCount = 1;
        String sampleName = "";
        int cutoffBaseQ = 2;
        int cutoffMapQ = 1;
        double cutoffRatioCertified = 0.75;
        int flankLen = 3;

        try {
            // create Options object
            Options options = new Options();
            options.addOption(new Option("v", "vars", true, "path to Phased SNPs file"));
            options.addOption(new Option("r", "reference", true, "path to reference (.fasta and index)"));
            options.addOption(new Option("i", "bam", true, "path to alignments file (.bam)"));
            options.addOption(new Option("c", "contacts", true, "path to contacts file (.contacts)"));
            options.addOption(new Option("n", "sampleName", true, "sample name in .vcf file"));
            options.addOption(new Option("o", "out", true, "path to outputfile (.contacts.withTags)"));
            options.addOption(new Option("baseQ", "baseQuality", true, "baseQ cutoff used to filter out low Positions"));
            options.addOption(new Option("mapQ", "mapQuality", true, "mapQ cutoff used to filter out low Positions"));
            options.addOption(new Option("ratio", "ratioSupportSNPs", true, "ratio cutoff used to filter out fragments with unsufficient snps evidence"));
            options.addOption(new Option("flankLen", "flankLen", true, "minimum flank sequence length besides with SNPs. Bases on the flank sequence must be match totally with genome"));

            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println(cmd.getOptionValue("i"));
            System.out.println(cmd.getOptionValue("v"));
            System.out.println(cmd.getOptionValue("c"));
            System.out.println(cmd.getOptionValue("o"));
            System.out.println(cmd.getOptionValue("r"));
            if (!(cmd.hasOption("i") || cmd.hasOption("v") || cmd.hasOption("o") || cmd.hasOption("c") || cmd.hasOption("r"))) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "AntOptsCommonsCLI", options );
                System.out.println();
                System.exit(0);
            }
            iF = cmd.getOptionValue("i");
            oF = cmd.getOptionValue("o");
            vF = cmd.getOptionValue("v");
            cF = cmd.getOptionValue("c");
            referenceFilePath = cmd.getOptionValue("r");
            if (cmd.hasOption("n")) {
                sampleName = cmd.getOptionValue("n");
            }
            if (cmd.hasOption("l")) {
                numL = Integer.parseInt(cmd.getOptionValue("l"));
            }
            if (cmd.hasOption("baseQ")) {
                cutoffBaseQ = Integer.parseInt(cmd.getOptionValue("baseQ"));
                System.out.println("cutoffBaseQ = " + cutoffBaseQ);
            }
            if (cmd.hasOption("mapQ")) {
                cutoffMapQ = Integer.parseInt(cmd.getOptionValue("mapQ"));
                System.out.println("cutoffMapQ = " + cutoffMapQ);
            }
            if (cmd.hasOption("ratio")) {
                cutoffRatioCertified = Double.parseDouble(cmd.getOptionValue("ratio"));
                System.out.println("cutoffRatioCertified = " + cutoffRatioCertified);
            }
            if (cmd.hasOption("flankLen")) {
                flankLen = Integer.parseInt(cmd.getOptionValue("flankLen"));
                System.out.println("flankLen = " + flankLen);
            }

        } catch (Exception ex) {
            System.out.println( "Unexpected exception:" + ex.getMessage() );
            System.exit(0);
        }

        //读取fasta
        System.out.println();
        System.out.println( "Reloading reference genome ...." );
        IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile(new File(referenceFilePath));
        HashMap<String, String> reloadedReference = reloadReference(fastaFile);

        //读取vcf和bam
        VCFFileReader variantContexts = new VCFFileReader(new File(vF));
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        BufferedReader contactsReader = new BufferedReader(new FileReader(cF));
        //如果不提供sampleName,则默认使用第一个sample
        if (sampleName.equalsIgnoreCase("")) {
            sampleName = variantContexts.getFileHeader().getGenotypeSamples().get(0);
            System.out.println("INFO: sample name is not given, using default first sample = " + sampleName);
        }
        BufferedWriter oWriter1 = new BufferedWriter(new FileWriter(oF));
        BufferedWriter oWriter2 = new BufferedWriter(new FileWriter(oF+".flankTest"));

        //逐个记录读取并判断
        int recordIndex = 0;

        SAMRecordIterator samRecordIterator = samReader.iterator();
        String preReadName = "";
        String readName = "";
        Map<Integer, Integer> indexPhaseDir = new HashMap<>(); //存储每个比对记录的分型结果
        Map<Integer, Integer[]> indexSNPsDir = new HashMap<>(); //存储每个比对记录的分型结果
        String line;            //status
        int countFragmentsCanPased =0;
        int countFragmentsPhased=0;
        int countIncluded=0;
        int countExcluded=0;
        int countErr=0;

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

        int countFlankUnconfident = 0;
        int countFlankConfident = 0;

        while (samRecordIterator.hasNext()){
            try {
                if ((recordIndex+1)%numL==0){
                    System.out.println("recordIndex = " + recordIndex);
//                    break;
                }
                SAMRecord record = samRecordIterator.next();
                int[] hardClipLengths = getHardClipLengths(record);
                //按reads统计
//                preReadName = readName;
//                readName = record.getReadName();
//                if (preReadName!="" && preReadName.equals(readName)){
//                    line=readName + "\t";
//                }
                int countUsefulSNPs = 0;
                int countUsefulSNPsFromPhase1 = 0;
                int countUsefulSNPsFromPhase2 = 0;
                int countUnPhased = 0;

                Boolean isCanPhased = false;
                if (record.getReadUnmappedFlag()) {
                }else {
                    //计算当前记录上的有效SNP数量
                    //遍历当前片段上的所有候选SNPs位点
                    for (VariantContext variantContext : variantContexts.query(record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd()).toList()) {
                        if (variantContext.isSNP()) {
                            try {
                                Genotype genotype = variantContext.getGenotype(sampleName);
                                String gtString = genotype.getGenotypeString();
                                boolean isPhased = gtString.contains("|");
                                //只有杂合且phased的snp才可备用
                                if (genotype.isHet() &&
                                    isPhased)
                                {
//                                    boolean isConfidentMapping = isConfidentMapping(fastaFile, record, variantContext.getStart(), flankLen);
                                    boolean isConfidentMapping = isConfidentMapping(reloadedReference, record, variantContext.getStart(), flankLen);
//                                    System.out.println(isConfidentMapping);
                                    if ( isConfidentMapping )
                                    {
                                        countFlankConfident++;
                                        isCanPhased = true;
                                        String base = getBasePositiveStrandByPos(record, variantContext.getStart());
                                        Byte baseQ = getBaseQualityPositiveStrandByPos(record, variantContext.getStart());
                                        int mapQ = record.getMappingQuality();
                                        //cutoff
                                        if (baseQ < cutoffBaseQ && mapQ < cutoffMapQ) continue;
                                        //获取基因型
                                        //                                List<String> genotypes = Arrays.asList(genotype.getGenotypeString().split("/")); // unphased snps
                                        List<String> genotypes = Arrays.asList(genotype.getGenotypeString().split("\\|")); // phased snps
                                        if (base.equalsIgnoreCase(genotypes.get(0))) {
                                            countUsefulSNPsFromPhase1++;
                                        } else if (base.equalsIgnoreCase(genotypes.get(1))) {
                                            countUsefulSNPsFromPhase2++;
                                        } else {
                                            countUnPhased++;
                                        }
                                        countUsefulSNPs++;
                                        boolean isIncluded = genotypes.contains(base);
                                    }else {
                                        countFlankUnconfident++;
                                    }
                                }
                            } catch (TribbleException e) {
                                e.printStackTrace();
                                countErr++;
                            }
                        }
                    }

                }
                //统计分型结果
                if (isCanPhased) countFragmentsCanPased++;
                if (countUsefulSNPs > 0) {
                    if (countUsefulSNPsFromPhase1 / (double) countUsefulSNPs >= cutoffRatioCertified) {
                        indexPhaseDir.put(recordIndex,  1);
                        indexSNPsDir.put(recordIndex, new Integer[]{countUsefulSNPs, countUsefulSNPsFromPhase1, countUsefulSNPsFromPhase2});
                        countFragmentsPhased++;
                    } else if (countUsefulSNPsFromPhase2 / (double) countUsefulSNPs >= cutoffRatioCertified) {
                        indexPhaseDir.put(recordIndex,  2);
                        indexSNPsDir.put(recordIndex, new Integer[]{countUsefulSNPs, countUsefulSNPsFromPhase1, countUsefulSNPsFromPhase2});
                        countFragmentsPhased++;
                    } else {
                        indexPhaseDir.put(recordIndex,  0);
                        indexSNPsDir.put(recordIndex, new Integer[]{countUsefulSNPs, countUsefulSNPsFromPhase1, countUsefulSNPsFromPhase2});
                    }
                }else {
                    indexPhaseDir.put(recordIndex,  0);
                    indexSNPsDir.put(recordIndex, new Integer[]{countUsefulSNPs, countUsefulSNPsFromPhase1, countUsefulSNPsFromPhase2});
                }
                recordIndex++;
                //输出
            } catch (SAMFormatException e) {
                e.printStackTrace();
            }
        }

        //输出统计信息
        System.out.println(MyUtil.getTimeFormatted());

        System.out.println("INFO: Phasing Records finished");
        System.out.println("INFO: countRecords = " + indexPhaseDir.size());
        System.out.println("INFO: countErrorRecords = " + countErr);
        System.out.println("INFO: countFragmentsCanPhased = " + countFragmentsCanPased);
        System.out.println("INFO: countFragmentsPhased = " + countFragmentsPhased);
        System.out.println();
        System.out.println("INFO: Starting output phased contacts");

        int registerRecord = 0;
        while ((line=contactsReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            one.setPhase(
                    String.valueOf(indexPhaseDir.getOrDefault((int) one.getIndex(), 0))
//                    String.valueOf(indexPhaseDir.get((int) one.getIndex()))
            );
            line = one.toContactLineWithPhase();
            String tail = toTabString(indexSNPsDir.getOrDefault((int) one.getIndex(), new Integer[]{-1,-1,-1}));
            oWriter1.write(line + "\t" + tail);
            oWriter1.newLine();
            registerRecord++;
            if ((registerRecord + 1) % numL == 0){
                System.out.println("INFO: OUTPUT_NUM_LINE " + registerRecord + "......");
//                break;
            };
        }
        System.out.println(MyUtil.getTimeFormatted());
        System.out.println("INFO: Finished");
        System.out.println();

        line="countFlankConfident = " + countFlankConfident;
        System.out.println(line);
        oWriter2.write(line);
        line="countFlankUnconfident = " + countFlankUnconfident;
        System.out.println(line);
        oWriter2.write(line);


        oWriter1.close();
        oWriter2.close();
        variantContexts.close();
    }

    // 提前加载全部基因组加快读写速度
    public static HashMap<String, String> reloadReference(String referenceFilePath) throws FileNotFoundException {
        IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile(new File(referenceFilePath));
        Iterator<FastaSequenceIndexEntry> iterator = fastaFile.getIndex().iterator();
        HashMap<String, String> referenceSeqs = new HashMap<>();
        while (iterator.hasNext()){
            FastaSequenceIndexEntry next = iterator.next();
            String contig = next.getContig();
            referenceSeqs.put(contig, fastaFile.getSequence(contig).getBaseString());
        }
        return referenceSeqs;
    }

    public static HashMap<String, String> reloadReference(IndexedFastaSequenceFile fastaFile) throws FileNotFoundException {
        Iterator<FastaSequenceIndexEntry> iterator = fastaFile.getIndex().iterator();
        HashMap<String, String> referenceSeqs = new HashMap<>();
        while (iterator.hasNext()){
            FastaSequenceIndexEntry next = iterator.next();
            String contig = next.getContig();
            referenceSeqs.put(contig, fastaFile.getSequence(contig).getBaseString());
        }
        return referenceSeqs;
    }

    //判断侧翼序列是否全部match
    public static Boolean isConfidentMapping(IndexedFastaSequenceFile fastaFile, SAMRecord record, int genomePos, int flankLen) throws FileNotFoundException {
        String UpstreamOnRead="";
        String DownstreamOnRead="";
        String UpstreamOnReference="";
        String DownstreamOnReference="";

        boolean isConfident=true;
        String operatorSeq = getOperatorSeq(record);
        for (int i = 1; i <= flankLen; i++) {
            int newGenomePos = genomePos - i;
            String operatorByPos = getOperatorByPos(record, newGenomePos,operatorSeq);
            String baseFromFastaByPos = getBaseFromFastaByPos(fastaFile, record.getReferenceName(), newGenomePos);
            String basePositiveStrandByPos = getBasePositiveStrandByPos(record, newGenomePos);
            if (
                    !(operatorByPos.equalsIgnoreCase("M")
                && baseFromFastaByPos.equalsIgnoreCase(basePositiveStrandByPos))
            ) {
//                System.out.println("operatorByPos = " + operatorByPos);
//                System.out.println("baseFromFastaByPos = " + baseFromFastaByPos);
//                System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);
                isConfident=false;
                break;
            }
            newGenomePos = genomePos + i;
            operatorByPos = getOperatorByPos(record, newGenomePos,operatorSeq);
            baseFromFastaByPos = getBaseFromFastaByPos(fastaFile, record.getReferenceName(), newGenomePos);
            basePositiveStrandByPos = getBasePositiveStrandByPos(record, newGenomePos);
            if (
                    !(operatorByPos.equalsIgnoreCase("M")
                            && baseFromFastaByPos.equalsIgnoreCase(basePositiveStrandByPos))
            ) {
//                System.out.println("operatorByPos = " + operatorByPos);
//                System.out.println("baseFromFastaByPos = " + baseFromFastaByPos);
//                System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);
                isConfident=false;
                break;
            }
        }
        return isConfident;
    }

    public static Boolean isConfidentMapping(HashMap<String,String> reloadedReference, SAMRecord record, int genomePos, int flankLen) throws FileNotFoundException {
        String UpstreamOnRead="";
        String DownstreamOnRead="";
        String UpstreamOnReference="";
        String DownstreamOnReference="";

        boolean isConfident=true;
        String operatorSeq = getOperatorSeq(record);
        for (int i = 1; i <= flankLen; i++) {
            int newGenomePos = genomePos - i;
            String operatorByPos = getOperatorByPos(record, newGenomePos, operatorSeq);
            String baseFromFastaByPos = getBaseFromFastaByPos(reloadedReference, record.getReferenceName(), newGenomePos);
            String basePositiveStrandByPos = getBasePositiveStrandByPos(record, newGenomePos);

            // false when any exception happen
            if (operatorByPos==null||
                    baseFromFastaByPos==null||
                    basePositiveStrandByPos==null
            ){
                return false;
            }

            if (
                    !(operatorByPos.equalsIgnoreCase("M")
                            && baseFromFastaByPos.equalsIgnoreCase(basePositiveStrandByPos))
            ) {
//                System.out.println("operatorByPos = " + operatorByPos);
//                System.out.println("baseFromFastaByPos = " + baseFromFastaByPos);
//                System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);
                isConfident=false;
                break;
            }
            newGenomePos = genomePos + i;
            operatorByPos = getOperatorByPos(record, newGenomePos, operatorSeq);
            baseFromFastaByPos = getBaseFromFastaByPos(reloadedReference, record.getReferenceName(), newGenomePos);
            basePositiveStrandByPos = getBasePositiveStrandByPos(record, newGenomePos);

            // false when any exception happen
            if (operatorByPos==null||
                    baseFromFastaByPos==null||
                    basePositiveStrandByPos==null
            ){
                return false;
            }

            if (
                    !(operatorByPos.equalsIgnoreCase("M")
                            && baseFromFastaByPos.equalsIgnoreCase(basePositiveStrandByPos))
            ) {
//                System.out.println("operatorByPos = " + operatorByPos);
//                System.out.println("baseFromFastaByPos = " + baseFromFastaByPos);
//                System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);
                isConfident=false;
                break;
            }
        }
        return isConfident;
    }

    public static String getOperatorSeq(SAMRecord record){
        // get cigar operator String
        String operatorString = "";
        Cigar cigar = record.getCigar();
        List<CigarElement> cigarElements = cigar.getCigarElements();
        int anchor = 0;
        for (CigarElement element : cigarElements) {
            int elementLength = element.getLength();
            CigarOperator operator = element.getOperator();
            // deletion不计入长度
            if (operator.equals(CigarOperator.DELETION)||
                operator.equals(CigarOperator.HARD_CLIP)
            ){
                continue;
            }
            // 处理每个碱基的比对情况
            for (int i = 0; i < elementLength; i++, anchor++) {
                operatorString+=operator;
            }
        }
        if (operatorString.length() != record.getReadString().length()){
            System.out.println("WARNING: operatorString length(" + operatorString.length() +") != read length(" + record.getReadString().length() +"): "+record.getReadName());

            // 获取比对记录的Flag
            int flag = record.getFlags();

            // 判断Flag中的各个位
            boolean isUnmapped = record.getReadUnmappedFlag();


            // 打印Flag和相关信息
            System.out.println("Read Name: " + record.getReadName());
            System.out.println("Flag: " + flag);
            System.out.println("Is Unmapped: " + isUnmapped);
            System.out.println("----------");
        }
        return operatorString;
    }

    public static String getOperatorByPos(SAMRecord record, int genomePos, String operatorString){
        int readPos = record.getReadPositionAtReferencePosition(genomePos);
        if (readPos == 0){
            return null;
        }
        if (readPos > record.getReadLength()){
            return null ;
        }

        String operator = operatorString.charAt(readPos - 1) +"";

        return operator;
    }

    public static String getBaseFromFastaByPos(IndexedFastaSequenceFile fastaFile, String chrName, int genomePos) throws FileNotFoundException {
        String base = fastaFile.getSubsequenceAt(chrName, genomePos, genomePos).getBaseString();
        return base;
    }

    public static String getBaseFromFastaByPos(HashMap<String,String> reloadedReference, String chrName, int genomePos) throws FileNotFoundException {
        String base = reloadedReference.get(chrName).charAt(genomePos-1)+"";
        return base;
    }

    public static String toTabString(Integer[] arr){
        String line = "";
        for (Integer a :
                arr) {
            line += a + "\t";
        }
        return line.trim();
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
