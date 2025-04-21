package process.contact;

import process.Path;

import java.util.ArrayList;
import java.util.List;

public class ResSitePurifying {
    private Path p = null;
    private List<ReadAT> concatemerPurified = new ArrayList<ReadAT>();
    private List<ReadAT> concatemerRemove = new ArrayList<ReadAT>();

    public ResSitePurifying(Path p){
        this.p = p;
    }

    //宽松版
    public List<ReadAT> purifyStrict(List<ReadAT> concameter){
        for (ReadAT at :
                concameter) {
            if (at.getConfidenceLevel(p.confidentDistCutoff) == ConfidenceLevel.LR){
                this.concatemerPurified.add(at);
            } else {
                this.concatemerRemove.add(at);
            }
        }
        concameter.clear();
        concameter.addAll(this.concatemerPurified);
        return concatemerRemove;
    }

    //严格版
    public List<ReadAT> purify(List<ReadAT> concameter){
        for (ReadAT at :
                concameter) {
            if (at.getConfidenceLevel(p.mergeL) != ConfidenceLevel.None){
                this.concatemerPurified.add(at);
            } else {
                this.concatemerRemove.add(at);
            }
        }
        concameter.clear();
        concameter.addAll(this.concatemerPurified);
        return concatemerRemove;
    }

    public void clear(){
        concatemerRemove.clear();
        concatemerPurified.clear();
    }
}
