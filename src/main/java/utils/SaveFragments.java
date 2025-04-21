package utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import process.contact.ReadAT;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveFragments {
    public static Judgement judgement = new Judgement();
    public static void main(String[] args) throws IOException {
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "SaveFragments: start");
        System.out.println(MyUtil.getTimeFormatted()+" " +
                "Options: "+ Arrays.toString(args));
        String cF = args[0];
        String csF = args[1];
        String statusSaved = args[2];
        BufferedReader reader = new BufferedReader(new FileReader(new File(cF)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(csF)));
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        List<ReadAT> concatemerCandidates=new ArrayList<ReadAT>();

        String line="";



        while ((line = reader.readLine())!=null) {
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                for (ReadAT at :
                        concatemer) {
                    // status == choosed
                    if (at.getStatus().equalsIgnoreCase(statusSaved)) {
                        // good boundary && good pos
                        if (isGoodBoundaries(at, 50) && isGoodPos(at, concatemer)){
                            concatemerCandidates.add(at);
                        }
                    }
                }
                if (!concatemerCandidates.isEmpty()){
                    chooseCandidates(concatemerCandidates);
                }
                for (ReadAT at :
                        concatemer) {
                    writer.write(at.toContactLine());
                    writer.newLine();
                }
                concatemerCandidates.clear();
                concatemer.clear();
                concatemer.add(one);
            }
        }

        reader.close();
        writer.close();

        System.out.println(MyUtil.getTimeFormatted()+" " +
                "SaveFragments: end");
    }

    public static boolean isGoodBoundaries(ReadAT at, int cutoff){
        Long[] refinedBoundaries = at.getRefinedBoundaries();
        if (Math.abs(at.getStart() - refinedBoundaries[0])<=cutoff &&
                Math.abs(at.getEnd() - refinedBoundaries[1])<=cutoff){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isGoodPos(ReadAT at, List<ReadAT> concatemer){
        RangeSet<Integer> rangeSet= TreeRangeSet.create();
        RangeSet<Integer> rangeSetAT= TreeRangeSet.create();
        rangeSetAT.add(Range.closedOpen(at.getrS(),at.getrE()));
        List<ReadAT> concatemerValid = new ArrayList<>();
        for (ReadAT one :
                concatemer) {
            if (judgement.judgeRealFrags(one))
                rangeSet.add(Range.closedOpen(one.rS, one.rE));
        }
        int fragLen = at.getrE()- at.getrS();
        rangeSetAT.removeAll(rangeSet);
        int usefulLen = 0;
        for (Range<Integer> range :
                rangeSetAT.asRanges()) {
            usefulLen+=range.upperEndpoint()-range.lowerEndpoint();
        }
        if (usefulLen>fragLen*0.5) return true;
        else return false;
    }

    public static boolean isOverlaped(ReadAT at1, ReadAT at2){
        if ((at1.getrS()- at2.getrE())*(at2.getrS()- at1.getrE()) > 0) return true;
        else return false;
    }

    public static void chooseCandidates(List<ReadAT> concatemer){
        List<Integer> dist = new ArrayList<Integer>();
        for (int i = 0; i < concatemer.size(); i++) {
            ReadAT readAT = concatemer.get(i);
            Long[] refinedBoundaries = readAT.getRefinedBoundaries();
            dist.add((int) (Math.abs(readAT.getStart() - refinedBoundaries[0]) + Math.abs(readAT.getEnd() - refinedBoundaries[1])));
        }
        ReadAT preAT = concatemer.get(0);
        int preI = 0;
        for (int i = 1; i < concatemer.size(); i++) {
            ReadAT readAT = concatemer.get(i);
            if(isOverlaped(preAT,readAT)){
                //若存在overlap，则判定哪个的dist更小，更小的成为新的pre
                if (dist.get(preI) < dist.get(i)){
                    continue;
                } else {
                    preAT=readAT;
                    preI=i;
                }
            } else {
                //若无overlap，则改变preAT的status
                preAT.setStatus("saved_"+preAT.getStatus());
                preAT=readAT;
                preI=i;
            }
        }
        //改变最后的preAT的status
        preAT.setStatus("saved_"+preAT.getStatus());
    }
}
