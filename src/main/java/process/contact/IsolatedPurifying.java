package process.contact;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.util.*;

public class IsolatedPurifying {
    List<ReadAT> concatemerPurified;
    List<ReadAT> concatemerPurifiedRemove;
    RangeSet<Long> rangeSet = TreeRangeSet.create(); //用于存储

    public IsolatedPurifying() {
        concatemerPurified=new ArrayList<>();
        concatemerPurifiedRemove = new ArrayList<>();
    }

    public List<ReadAT> purify(List<ReadAT> concatemer, long dis, int fragNumCutoff, double dominantCutoff) {
        Map<Range<Long>, ReadAT> rangeMap;
        Map<Range<Long>, ReadAT> tmpMap;
        rangeMap = new HashMap<>();
        tmpMap = new HashMap<>();

        RangeSet<Long> rangeSetTmp;

        //判断片段个数是否达到要求数量
        if (concatemer.size() < fragNumCutoff){
            concatemerPurified.clear();
            concatemerPurifiedRemove.clear();
            concatemerPurified.addAll(concatemer);
            return concatemerPurifiedRemove;
        }

        //计算所有片段构成的区域集合
        for (ReadAT at :
                concatemer) {
            rangeSet.add(Range.closed(at.start, at.end));
        }

        //逐个判断哪些片段是与其他所有片段的距离均超过1Mb的
        for (ReadAT at :
                concatemer) {
            rangeSetTmp = TreeRangeSet.create(rangeSet);
            rangeSetTmp.remove(Range.closed(at.start, at.end));
            if (rangeSetTmp.intersects(Range.closed(at.start-dis, at.end+dis))){
                this.concatemerPurified.add(at);
            }else {
                concatemerPurifiedRemove.add(at);
            }
        }

        //判断过初筛后的片段数量是否超过比例阈值
        if (concatemerPurified.size()/(double)concatemer.size() < dominantCutoff){
            concatemerPurified.clear();
            concatemerPurifiedRemove.clear();
            concatemerPurified.addAll(concatemer);
            return concatemerPurifiedRemove;
        }

        //计算初筛片段上下游扩展指定距离后构成的区域集合
        rangeSet.clear();
        for (ReadAT at :
                concatemerPurified) {
            rangeSet.add(Range.closed(at.start-dis, at.end-dis));
        }

        //判断过初筛片段之间是否能够成一个团
        Set<Range<Long>> ranges = rangeSet.asRanges();
        if (ranges.size() >= 1){
            concatemerPurified.clear();
            concatemerPurifiedRemove.clear();
            concatemerPurified.addAll(concatemer);
            return concatemerPurifiedRemove;
        }

        return concatemerPurifiedRemove;
    }

    public void clean(){
        concatemerPurified.clear();
        concatemerPurifiedRemove.clear();
    }
}
