package utils.entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChrSizes {
    int numBins;
    int resolution;
    Map<String, Long> chrMap= new HashMap<>();
    Map<String, Integer[]> chrBinRange= new HashMap<>();

    public ChrSizes(String chrFile) {
    }

//    从chrSizes文件开始建立
    public ChrSizes(String chrSizesFile, int resolution) throws IOException {
        this.resolution = resolution;
        BufferedReader chrReader = new BufferedReader(new FileReader(chrSizesFile));
        String line;
        while ((line=chrReader.readLine())!=null){
            String[] fields = line.split("\t");
            String chrName = fields[0];
            Long chrLen = Long.parseLong(fields[1]);
            this.chrMap.put(chrName, chrLen);
            this.chrBinRange.put(chrName, new Integer[]{
                    numBins,
                    Math.toIntExact(numBins + chrLen / resolution )
            });
            this.numBins += (chrLen/resolution + 1);
        }
        chrReader.close();
    }



    public int getOffset(String chrName){
        return
                this.chrBinRange.get(chrName)[0];
    }

    public int getMaxBinByChr(String chrName){
        return
                this.chrBinRange.get(chrName)[1];
    }

    public int getNumBins() {
        return numBins;
    }

    public int getNumBinsByChr(String chrName) {
        Integer[] range = this.chrBinRange.get(chrName);
        return range[1]-range[0]+1;
    }

    public int getBinByPos(String chrName, Long pos) {
        Integer[] range = this.chrBinRange.get(chrName);
        int binID = (int) (range[0] + pos / this.resolution);
        if (binID <= range[1])
            return binID;
        else
            return -1;
    }

    public Map.Entry<String, Integer> getPosByBin(Integer bin) {
        String chrName="None";
        Integer posStart=-999;
        for ( Map.Entry<String,Integer[]> entry : this.chrBinRange.entrySet()){
            Integer[] dRange = entry.getValue();
            if (bin >= dRange[0] &&
                bin <= dRange[1]) {
                chrName = entry.getKey();
                posStart = (bin - dRange[0])*this.resolution;
            }
        }
        return new AbstractMap.SimpleEntry<String, Integer>(chrName, posStart);
    }



    public int getResolution() {
        return resolution;
    }

    public Map<String, Long> getChrMap() {
        return chrMap;
    }

    public Set<String> getChrSet() {
        return chrMap.keySet();
    }

    public Map<String, Integer[]> getChrBinRange() {
        return chrBinRange;
    }
}
