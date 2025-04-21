package utils.entity;

import com.sun.xml.internal.bind.v2.model.core.ID;
import utils.Region;

import java.util.*;

public class PCluster {
    private String ID = "";
    private Region[] anchors = null;
    private String annoations = "";

    public PCluster() {
        this.ID="";
        this.anchors = new Region[]{};
    }

    public PCluster(String line) {
        String[] fields = line.trim().split("\t");
        this.ID = fields[0];
        anchors = new Region[fields.length-1];
        for (int i = 1; i < fields.length; i++) {
            Region anchor = new Region(fields[i]);
            anchors[i-1] = anchor;
        }
    }

    public Set<Region> anchorSet(){
        Set<Region> hashSet = new HashSet();
        for (Region anchor :
                anchors) {
            hashSet.add(anchor);
        }
        return hashSet;
    }

    public String getID() {
        return ID;
    }

    public Region[] getAnchors() {
        return anchors;
    }

    public void setAnnoations(String annoations) {
        this.annoations = annoations;
    }

    @Override
    public String toString() {
        String line="";
        for (Region anchor :
                this.anchors) {
            line+=anchor.toStringNoTAB() + "\t";
        }
        line = this.ID + "\t" + line + this.annoations;
        return line;
    }
}
