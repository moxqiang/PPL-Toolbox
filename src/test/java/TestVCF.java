import htsjdk.samtools.*;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.commons.cli.*;
import org.junit.Test;
import process.contact.ReadAT;
import utils.Judgement;
import utils.MyUtil;

import java.io.*;
import java.util.*;

public class TestVCF {
    public static int tmpCount=0;

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

    public static int getindexFirstLeft(List<ReadAT> concatemer, int index){
        int indexFirstLeft = index-1;
        while (indexFirstLeft >= 0){
            if (concatemer.get(indexFirstLeft).isPhased()) return indexFirstLeft;
            indexFirstLeft--;
        }
        return -1;
    }

    public static int getindexFirstRight(List<ReadAT> concatemer, int index){
        int indexFirstRight = index+1;
        while (indexFirstRight <= concatemer.size()-1){
            if (concatemer.get(indexFirstRight).isPhased()) return indexFirstRight;
            indexFirstRight++;
        }
        return -1;
    }

    public static int getDistBetweenAT(ReadAT at1, ReadAT at2){
        return (int) Math.abs(at1.getMid()-at2.getMid());
    }

    public static Map<String, List<ReadAT>> splitConcatemerByChr(List<ReadAT> concatemer){
        HashSet<String> chrSet  = new HashSet<>();
        HashMap<String, List<ReadAT>> concatemerMap = new HashMap<>();
        for (ReadAT one :
                concatemer) {
            chrSet.add(one.getChr());
        }
        for (String chrName :
                chrSet) {
            concatemerMap.put(chrName, new ArrayList<ReadAT>());
        }
        for (ReadAT one :
                concatemer) {
            concatemerMap.get(one.getChr()).add(one);
        }
        return concatemerMap;
    }

    public static Map<String, List<ReadAT>> splitConcatemerByPhase(List<ReadAT> concatemer){
        HashSet<String> phaseSet  = new HashSet<>();
        HashMap<String, List<ReadAT>> concatemerMap = new HashMap<>();
        for (ReadAT one :
                concatemer) {
            phaseSet.add(one.getPhase());
        }
        for (String phaseName :
                phaseSet) {
            concatemerMap.put(phaseName, new ArrayList<ReadAT>());
        }
        for (ReadAT one :
                concatemer) {
            concatemerMap.get(one.getPhase()).add(one);
        }
        return concatemerMap;
    }

    @Test
    public void test3() throws IOException {
        String iF="E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.contacts.withTags";
        String oF="E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.contacts.withTags.imputed.allChr";
        int numLine=400000;
        int distCutoff = 500000000;
        double bridgeCutoff = 0.8;
        int step = 2;


        //读取contacts
        BufferedReader atReader = new BufferedReader( new FileReader(iF));
        BufferedWriter oWriter1 = new BufferedWriter(new FileWriter(oF));

        //逐个记录读取并判断
        int recordIndex = 0;
        int count = 0;
        String line="";
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        Judgement judgement = new Judgement();
        while ((line = atReader.readLine())!=null) {
            count++;
            if (count % numLine == 0) {
                System.out.println(count + " frags was handled");
            }

            String[] fields = line.split("\t");

            ReadAT one = new ReadAT(fields);
            //判断是否为真实比对片段
            if (!judgement.judgeRealFrags(one)){
                continue;
            }

            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                //正式的处理
                //按染色体编号划分
                Map<String, List<ReadAT>> concatemerMapByChr = splitConcatemerByChr(concatemer);
                //按染色体逐个处理
                for (List<ReadAT> concatemerByChr:
                        concatemerMapByChr.values()) {
                    Map<String, List<ReadAT>> concatemerByPhase = splitConcatemerByPhase(concatemer);
                    List<ReadAT> concatemerPhase0 = concatemerByPhase.getOrDefault("0", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhase1 = concatemerByPhase.getOrDefault("1", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhase2 = concatemerByPhase.getOrDefault("2", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhaseConflict = new ArrayList<>();
                    if (step >=1 ){ // distance rule
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            int flag = 0;
                            for (ReadAT phase1AT :
                                    concatemerPhase1) {
                                if (getDistBetweenAT(unTaggedAT, phase1AT) < distCutoff){
                                    flag+=1;
                                    break;
                                }
                            }
                            for (ReadAT phase2AT :
                                    concatemerPhase2) {
                                if (getDistBetweenAT(unTaggedAT, phase2AT) < distCutoff){
                                    flag+=2;
                                    break;
                                }
                            }
                            if (flag == 0){

                            }else if (flag == 1){
                                unTaggedAT.setPhase("1");
                            }else if (flag == 2){
                                unTaggedAT.setPhase("2");
                            }else if (flag == 3){

                            }
                        }
                        // 重新划分concatemerPhase*
                        List<ReadAT> newConcatemerPhase0 = new ArrayList<>();
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase1.add(unTaggedAT);
                                } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase2.add(unTaggedAT);
                                }
                            }else {
                                newConcatemerPhase0.add(unTaggedAT);
                            }
                        }
                        concatemerPhase0 = newConcatemerPhase0;
                        if (step >= 2){ // bridge rule
                            for (ReadAT unTaggedAT :
                                    concatemerPhase0) {
                                int flag=0;
                                int index = concatemer.indexOf(unTaggedAT);
                                int indexFirstLeft = getindexFirstLeft(concatemer, index);
                                int indexFirstRight = getindexFirstRight(concatemer, index);
                                if (indexFirstLeft==-1 || indexFirstRight==-1){
                                    flag=0;
                                } else if (concatemer.get(indexFirstLeft).getPhase().equalsIgnoreCase(concatemer.get(indexFirstRight).getPhase())){
                                    if (concatemer.get(indexFirstLeft).getPhase().equals("1")) flag = 1;
                                    if (concatemer.get(indexFirstLeft).getPhase().equals("2")) flag = 2;
                                } else flag=3;
                                if (flag == 0){

                                }else if (flag == 1){
                                    unTaggedAT.setPhase("1");
                                }else if (flag == 2){
                                    unTaggedAT.setPhase("2");
                                }else if (flag == 3){

                                }
                            }
                            // 重新划分concatemerPhase*
                            newConcatemerPhase0 = new ArrayList<>();
                            for (ReadAT unTaggedAT :
                                    concatemerPhase0) {
                                if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                    if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase1.add(unTaggedAT);
                                    } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase2.add(unTaggedAT);
                                    }
                                }else {
                                    newConcatemerPhase0.add(unTaggedAT);
                                }
                            }
                            concatemerPhase0 = newConcatemerPhase0;
                            if (step >= 3){ // dominant rule
                                if (concatemerPhase1.size()==0 || concatemerPhase2.size()==0){
                                    if (concatemerPhase1.size() == 0){
                                        if (concatemerPhase2.size()/(double) concatemer.size() >= bridgeCutoff){
                                            for (ReadAT unTaggedAT :
                                                    concatemerPhase0) {
                                                unTaggedAT.setPhase(2);
                                            }
                                        }
                                    }
                                    if (concatemerPhase2.size() == 0){
                                        if (concatemerPhase1.size()/(double) concatemer.size() >= bridgeCutoff){
                                            for (ReadAT unTaggedAT :
                                                    concatemerPhase0) {
                                                unTaggedAT.setPhase(1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (ReadAT at :
                        concatemer) {
                    line = at.toContactLineWithPhase();
                    oWriter1.write(line);
                    oWriter1.newLine();
                }
                concatemer.clear();
                concatemer.add(one);
            }

        }

        {
            //正式的处理
            //按染色体编号划分
            Map<String, List<ReadAT>> concatemerMapByChr = splitConcatemerByChr(concatemer);
            //按染色体逐个处理
            for (List<ReadAT> concatemerByChr:
                    concatemerMapByChr.values()) {
                Map<String, List<ReadAT>> concatemerByPhase = splitConcatemerByPhase(concatemer);
                List<ReadAT> concatemerPhase0 = concatemerByPhase.getOrDefault("0", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhase1 = concatemerByPhase.getOrDefault("1", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhase2 = concatemerByPhase.getOrDefault("2", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhaseConflict = new ArrayList<>();
                if (step >=1 ){ // distance rule
                    for (ReadAT unTaggedAT :
                            concatemerPhase0) {
                        int flag = 0;
                        for (ReadAT phase1AT :
                                concatemerPhase1) {
                            if (getDistBetweenAT(unTaggedAT, phase1AT) < distCutoff){
                                flag+=1;
                                break;
                            }
                        }
                        for (ReadAT phase2AT :
                                concatemerPhase2) {
                            if (getDistBetweenAT(unTaggedAT, phase2AT) < distCutoff){
                                flag+=2;
                                break;
                            }
                        }
                        if (flag == 0){

                        }else if (flag == 1){
                            unTaggedAT.setPhase("1");
                        }else if (flag == 2){
                            unTaggedAT.setPhase("2");
                        }else if (flag == 3){

                        }
                    }
                    // 重新划分concatemerPhase*
                    List<ReadAT> newConcatemerPhase0 = new ArrayList<>();
                    for (ReadAT unTaggedAT :
                            concatemerPhase0) {
                        if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                            if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                newConcatemerPhase0.remove(unTaggedAT);
                                concatemerPhase1.add(unTaggedAT);
                            } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                newConcatemerPhase0.remove(unTaggedAT);
                                concatemerPhase2.add(unTaggedAT);
                            }
                        }else {
                            newConcatemerPhase0.add(unTaggedAT);
                        }
                    }
                    concatemerPhase0 = newConcatemerPhase0;
                    if (step >= 2){ // bridge rule
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            int flag=0;
                            int index = concatemer.indexOf(unTaggedAT);
                            int indexFirstLeft = getindexFirstLeft(concatemer, index);
                            int indexFirstRight = getindexFirstRight(concatemer, index);
                            if (indexFirstLeft==-1 || indexFirstRight==-1){
                                flag=0;
                            } else if (concatemer.get(indexFirstLeft).getPhase().equalsIgnoreCase(concatemer.get(indexFirstRight).getPhase())){
                                if (concatemer.get(indexFirstLeft).getPhase().equals("1")) flag = 1;
                                if (concatemer.get(indexFirstLeft).getPhase().equals("2")) flag = 2;
                            } else flag=3;
                            if (flag == 0){

                            }else if (flag == 1){
                                unTaggedAT.setPhase("1");
                            }else if (flag == 2){
                                unTaggedAT.setPhase("2");
                            }else if (flag == 3){

                            }
                        }
                        // 重新划分concatemerPhase*
                        newConcatemerPhase0 = new ArrayList<>();
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase1.add(unTaggedAT);
                                } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase2.add(unTaggedAT);
                                }
                            }else {
                                newConcatemerPhase0.add(unTaggedAT);
                            }
                        }
                        concatemerPhase0 = newConcatemerPhase0;
                        if (step >= 3){ // dominant rule
                            if (concatemerPhase1.size()==0 || concatemerPhase2.size()==0){
                                if (concatemerPhase1.size() == 0){
                                    if (concatemerPhase2.size()/(double) concatemer.size() >= bridgeCutoff){
                                        for (ReadAT unTaggedAT :
                                                concatemerPhase0) {
                                            unTaggedAT.setPhase(2);
                                        }
                                    }
                                }
                                if (concatemerPhase2.size() == 0){
                                    if (concatemerPhase1.size()/(double) concatemer.size() >= bridgeCutoff){
                                        for (ReadAT unTaggedAT :
                                                concatemerPhase0) {
                                            unTaggedAT.setPhase(1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (ReadAT at :
                    concatemer) {
                line = at.toContactLineWithPhase();
                oWriter1.write(line);
                oWriter1.newLine();
            }
            concatemer.clear();
        }

        oWriter1.close();
        atReader.close();
    }

    @Test
    public void test2() throws IOException {
        String vF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\HG001_GRCh38_1_22_v4.2.1_benchmark.vcf.gz";
        String iF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.bam";
        String oF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.contacts.withTags.7";
        String cF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.contacts";
        int numL = 100000;
        String sampleName = "";
        int cutoffBaseQ = 0;
        int cutoffMapQ = 0;
        double cutoffRatioCertified = 0.7;

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

        //逐个记录读取并判断
        int recordIndex = 0;

        SAMRecordIterator samRecordIterator = samReader.iterator();
        String preReadName = "";
        String readName = "";
        Map<Integer, Integer> indexPaseDir = new HashMap<>(); //存储每个比对记录的分型结果
        String line;            //status
        int countFragmentsCanPased =0;
        int countFragmentsPhased=0;
        int countIncluded=0;
        int countExcluded=0;

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
                            Genotype genotype = variantContext.getGenotype(sampleName);
                            //只有杂合的snp才可备用
                            if (genotype.isHet()) {
                                isCanPhased = true;
                                String base = getBasePositiveStrandByPos(record, variantContext.getStart());
                                Byte baseQ = getBaseQualityPositiveStrandByPos(record, variantContext.getStart());
                                int mapQ = record.getMappingQuality();
                                //cutoff
                                if (baseQ < cutoffBaseQ && mapQ < cutoffMapQ) continue;
                                //获取基因型
                                List<String> genotypes = Arrays.asList(genotype.getGenotypeString().split("/"));
                                if (base.equalsIgnoreCase(genotypes.get(0))){
                                    countUsefulSNPsFromPhase1++;
                                }else if (base.equalsIgnoreCase(genotypes.get(1))){
                                    countUsefulSNPsFromPhase2++;
                                }else {
                                    countUnPhased++;
                                }
                                countUsefulSNPs++;
                                boolean isIncluded = genotypes.contains(base);
                            }
                        }
                    }

                }
                //统计分型结果
                if (isCanPhased) countFragmentsCanPased++;
                if (countUsefulSNPs > 0) {
                    if (countUsefulSNPsFromPhase1 / (double) countUsefulSNPs >= cutoffRatioCertified) {
                        indexPaseDir.put(recordIndex,  1);
                        countFragmentsPhased++;
                    } else if (countUsefulSNPsFromPhase2 / (double) countUsefulSNPs >= cutoffRatioCertified) {
                        indexPaseDir.put(recordIndex,  2);
                        countFragmentsPhased++;
                    } else {
                        indexPaseDir.put(recordIndex,  0);
                    }
                }else {
                    indexPaseDir.put(recordIndex,  0);
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
        System.out.println("INFO: countFragmentsCanPased = " + countFragmentsCanPased);
        System.out.println("INFO: countFragmentsPhased = " + countFragmentsPhased);
        System.out.println();
        System.out.println("INFO: Starting output phased contacts");

        int registerRecord = 0;
        while ((line=contactsReader.readLine())!=null){
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            one.setPhase(
                    String.valueOf(indexPaseDir.getOrDefault((int) one.getIndex(), 0))
//                    String.valueOf(indexPaseDir.get((int) one.getIndex()))
            );
            line = one.toContactLineWithPhase();
            oWriter1.write(line);
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

        oWriter1.close();
        variantContexts.close();
    }

    @Test
    public void test1() throws IOException {
        String vF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\HG001_GRCh38_1_22_v4.2.1_benchmark.vcf.gz";
        String iF = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.bam";
        String oF1 = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.snapQC.byFrag.txt";
        String oF2 = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.snapQC.byRead.txt";
        String oF3 = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.snapQC.ieBybaseQ.txt";
        String oF4 = "E:\\OneDrive\\work\\pore-C\\haplo-tagging\\GSM6284587_800k.fq.snapQC.ieBymapQ.txt";
        //读取vcf和bam
        VCFFileReader variantContexts = new VCFFileReader(new File(vF));
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        String sampleName;
        sampleName = variantContexts.getFileHeader().getGenotypeSamples().get(0);
        System.out.println("sampleName = " + sampleName);
//        sampleName = "HG001";
        BufferedWriter oWriter1 = new BufferedWriter(new FileWriter(oF1));
        BufferedWriter oWriter2 = new BufferedWriter(new FileWriter(oF2));
        BufferedWriter oWriter3 = new BufferedWriter(new FileWriter(oF3));
        BufferedWriter oWriter4 = new BufferedWriter(new FileWriter(oF4));

        //逐个记录读取并判断
        int recordIndex = 0;

        SAMRecordIterator samRecordIterator = samReader.iterator();
        String preReadName = "";
        String readName = "";
        String line;            //status
        int countUsefulSNPsByRead = 0;
        int countUsefulSNPs=0;

        int countIncluded=0;
        int countExcluded=0;

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
                if ((recordIndex+1)%30000==0){
                    System.out.println("recordIndex = " + recordIndex);
//                    break;
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
                            Genotype genotype = variantContext.getGenotype(sampleName);
                            if (genotype.isHet()) {
                                String base = getBasePositiveStrandByPos(record, variantContext.getStart());
                                Byte baseQ = getBaseQualityPositiveStrandByPos(record, variantContext.getStart());
                                int mapQ = record.getMappingQuality();
//                                System.out.println("readName = " + readName);
//                                System.out.println("variantContext = " + record.getReferenceName() + ":" + variantContext.getStart() +" "+ getStrand(record));
//                                System.out.println(genotype.getGenotypeString(true));
//                                System.out.println(base);
                                boolean isIncluded = Arrays.asList(genotype.getGenotypeString().split("/")).contains(base);
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
        variantContexts.close();
    }
}
