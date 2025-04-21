package process.contact;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.google.common.collect.*;
import jdk.nashorn.tools.Shell;
import process.contact.ReadAT;

public class FragmentPurifying {

    String restrictionsiteFile;
//    RangeMap<Long, Long> rangeMap;
    List<ReadAT> concatemerPurified;
    List<ReadAT> concatemerPurifiedRemove;
    int minfragsize;
    int maxfragsize;

    public FragmentPurifying(String restrictionFile, int minfragsize, int maxfragsize) throws IOException {
        restrictionsiteFile=restrictionsiteFile;
        concatemerPurified = new ArrayList<ReadAT>();
        concatemerPurifiedRemove = new ArrayList<ReadAT>();
        this.minfragsize=minfragsize;
        this.maxfragsize=maxfragsize;
//        this.rangeMap = TreeRangeMap.create();
        BufferedReader reader = new BufferedReader(new FileReader(new File(restrictionFile)));
        String line;
        long id = 0;

        while ((line=reader.readLine())!=null){
            String[] fields = line.split("\t");
//            rangeMap.put(Range.openClosed(Long.parseLong(fields[1]), Long.parseLong(fields[2])),id);
            id++;
        }
        reader.close();
    }


    public int removePETinsameblock(List<ReadAT> concatemer) throws IOException {
        return 0;
    }

    public List<ReadAT> purify(List<ReadAT> concatemer) throws IOException {
        String line="";
        long num1=0;
        long num2=0;
        long num3=0;
        //暂存at
        HashMap<ReadAT, List<Long>> tmpMap = new HashMap<ReadAT, List<Long>>();
        //将所有的at及片段信息存入tmpMap
        for (ReadAT at :
                concatemer) {
            tmpMap.put(at, new ArrayList<Long>());
            //循环遍历每一个frag id
            for (long[] frags:
                    at.getFrags()) {
                long id = frags[0];
                tmpMap.get(at).add(id);
            }
        }
        //再次遍历，比较at两两之间的片段邻近情况，删除齐总mapq较低的
        ArrayList<ReadAT> readATS = new ArrayList<>();
        for (ReadAT at1 :
                concatemer) {
            //每次循环值判断at1的准确性，当有一次判断其为劣于at2，则开始判断下一个
            readATS.add(at1);
            boolean isGood = true;
            for (ReadAT at2 :
                    concatemer) {
                if (at1 == at2) continue;
                //查看是否由相邻
                for (long fragID1 :
                        tmpMap.get(at1)) {
                    for (long fragID2 :
                        tmpMap.get(at2)){
                        //临近判断
                        if (Math.abs(fragID1 - fragID2) <= 1){
                            //打劣质标签
                            if (at1.getMapq() < at2.getMapq()) isGood = false;

                        }
                        if (!isGood) break;
                    }
                    if (!isGood) break;
                }
                if (!isGood) break;
            }
            if (isGood) concatemerPurified.add(at1);
            else concatemerPurifiedRemove.add(at1);
        }
//        concatemer.clear();
//        concatemer.addAll(concatemerPurified);
        return concatemerPurifiedRemove;
    }

    public void clear(){
        concatemerPurified.clear();
        concatemerPurifiedRemove.clear();
    }
}
