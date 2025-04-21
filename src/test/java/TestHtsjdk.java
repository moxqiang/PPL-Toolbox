import htsjdk.samtools.*;
import htsjdk.samtools.reference.FastaSequenceIndexEntry;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static utils.GenotypingBySNPOld.getBasePositiveStrandByPos;
import static utils.GenotypingBySNP.getOperatorSeq;

public class TestHtsjdk {
    @Test
    public void test1() throws IOException {
//        final SamReader reader = SamReaderFactory.makeDefault().open(new File("/my.bam"));
        String iF = "E:\\tmp\\haplo\\htrans\\GSM6284587_10w.fq.bam";
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        SAMRecordIterator iterator = samReader.iterator();
        SAMRecord firstRecord = iterator.next();
        Cigar cigar = firstRecord.getCigar();

        String readSequence = firstRecord.getReadString();
        List<CigarElement> cigarElements = cigar.getCigarElements();

        // 遍历CIGAR元素
        int cigarLength = 0;
        int anchor = 0;
        String bases = "";
        String operatorString = "";
        for (CigarElement element : cigarElements) {
            int elementLength = element.getLength();
            CigarOperator operator = element.getOperator();
            // deletion不计入长度
            if (operator.equals(CigarOperator.DELETION)){
                continue;
            }

            // 处理每个碱基的比对情况
            for (int i = 0; i < elementLength; i++, anchor++) {
                char base = readSequence.charAt(anchor);
                bases+=base;
                operatorString+=operator;
                System.out.println("Base: " + base + ", Operator: " + operator);
            }
            cigarLength += elementLength;
        }
        System.out.println("bases = " + bases);
        System.out.println("operatorString = " + operatorString);
        System.out.println("read length = " + readSequence.length());
        System.out.println("cigar length = " + cigarLength);
        System.out.println("anchor = " + anchor);


        System.out.println("firstRecord.getReadName() = " + firstRecord.getReadName());
        System.out.println("firstRecord.getReadBases().toString() = " + new String(firstRecord.getReadBases()));
        System.out.println("firstRecord.getReadString() = " + firstRecord.getReadString());

        iterator.close();
        samReader.close();
    }

    public static String getOperatorByPos(SAMRecord record, int genomePos){
        int readPos = record.getReadPositionAtReferencePosition(genomePos);
        if (readPos == 0){
            return null;
        }
        if (readPos > record.getReadLength()){
            return null ;
        }

        // get cigar operator String
        String operatorString = "";
        {
            String readSequence = record.getReadString();
            Cigar cigar = record.getCigar();
            List<CigarElement> cigarElements = cigar.getCigarElements();
            int anchor = 0;
            for (CigarElement element : cigarElements) {
                int elementLength = element.getLength();
                CigarOperator operator = element.getOperator();
                // deletion不计入长度
                if (operator.equals(CigarOperator.DELETION)){
                    continue;
                }
                // 处理每个碱基的比对情况
                for (int i = 0; i < elementLength; i++, anchor++) {
                    char base = readSequence.charAt(anchor);
                    operatorString+=operator;
                }
            }
        }

        String operator = operatorString.charAt(readPos - 1) +"";

        return operator;
    }

    public static String getBaseFromFastaByPos(IndexedFastaSequenceFile fastaFile, String chrName, int genomePos) throws FileNotFoundException {
        String base = fastaFile.getSubsequenceAt(chrName, genomePos, genomePos).getBaseString();
        return base;
    }

    @Test
    public void test2() throws IOException {
        String iF = "E:\\tmp\\haplo\\htrans\\GSM6284587_10w.fq.bam";
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        SAMRecordIterator iterator = samReader.iterator();
        SAMRecord firstRecord = iterator.next();

        String operatorByPos = getOperatorByPos(firstRecord, 93156267);
        String basePositiveStrandByPos = getBasePositiveStrandByPos(firstRecord, 93156267);

        System.out.println("operatorByPos = " + operatorByPos);
        System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);

        String referenceFilePath = "E:\\tmp\\hg38\\hg38.chr.fa";
        IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile(new File(referenceFilePath));

        String baseFromFastaByPos = getBaseFromFastaByPos(fastaFile, firstRecord.getReferenceName(), 93156267);
        System.out.println("baseFromFastaByPos = " + baseFromFastaByPos);
    }

    @Test
    public void test3() throws IOException {
        String referenceFilePath = "E:\\tmp\\hg38\\hg38.chr.fa";
        IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile(new File(referenceFilePath));
        boolean indexed = fastaFile.isIndexed();
        System.out.println("indexed = " + indexed);
        System.out.println(fastaFile.getSequence("chrM"));
        String basePositiveStrandByPos = fastaFile.getSubsequenceAt("chr8", 93156267, 93156267).getBaseString();
        System.out.println("basePositiveStrandByPos = " + basePositiveStrandByPos);

        Iterator<FastaSequenceIndexEntry> iterator = fastaFile.getIndex().iterator();
        HashMap<String, String> referenceSeqs = new HashMap<>();
        while (iterator.hasNext()){
            FastaSequenceIndexEntry next = iterator.next();
            String contig = next.getContig();
            referenceSeqs.put(contig, fastaFile.getSequence(contig).getBaseString());
        }
        System.out.println("referenceSeqs.get(\"chrM\") = " + referenceSeqs.get("chrM"));
    }

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

    @Test
    public void test4() throws IOException {
//        final SamReader reader = SamReaderFactory.makeDefault().open(new File("/my.bam"));
        String iF = "E:\\tmp\\haplo\\htrans\\GSM6284587_10w.fq.bam";
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(iF));
        SAMRecordIterator iterator = samReader.iterator();
//        SAMRecord firstRecord = iterator.next();

        while (iterator.hasNext()){
            SAMRecord samRecord = iterator.next();
            String operatorSeq = getOperatorSeq(samRecord);
        }

        iterator.close();
        samReader.close();
    }

}
