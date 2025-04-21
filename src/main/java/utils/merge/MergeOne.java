package utils.merge;

import process.contact.ReadAT;

import java.util.ArrayList;
import java.util.List;


public class MergeOne {

    public List<ReadAT> concatemerMerged = new ArrayList<ReadAT>();


    public List<ReadAT> run(List<ReadAT> concatemer, int cutoff1 , int cutoff2){
        this.concatemerMerged.clear();

        ReadAT pre = null;

        for (ReadAT at:
             concatemer) {
            if (pre == null){
                pre = at;
                continue;
            }
            //当两个reads的距离满足阈值条件 且 位于同一条链上， 则合并
            if (Math.abs(at.rS-pre.rE) < cutoff1
                    && Math.abs(at.start-pre.end) < cutoff2
                    && (Math.abs(at.start-pre.end) < (cutoff1+cutoff2)*0.4)
                    && pre.strand==at.strand
                    && pre.getChr().equalsIgnoreCase(at.getChr())
            ){
                //合并
                pre = pre.merge(at);
            }else {
                //直到不满足合并需求，将上一个加入到合并后的队列
                concatemerMerged.add(pre);
                pre = at;
            }
        }
        //将最后一个加入合并后队列
        concatemerMerged.add(pre);
        return concatemerMerged;
    }
}
