package process.contact;

import process.Path;

import java.util.List;

public class RefineBoundary {
    Path p;
    int mergeL;

    public RefineBoundary(Path p) {
        this.p = p;
        this.mergeL = p.mergeL;
    }

    public void refine(List<ReadAT> concameter){
        for (ReadAT at :
                concameter) {
            at.calConfidenceLevel(this.mergeL);
            if (at.confidenceLevel == ConfidenceLevel.None){
                continue;
            } else if (at.confidenceLevel == ConfidenceLevel.LR){
                at.setStart(at.getFrags()[0][1]);
                at.setEnd(at.getFrags()[at.getFrags().length-1][2]);
            } else if (at.confidenceLevel == ConfidenceLevel.L){
                at.setStart(at.getFrags()[0][1]);
            } else if (at.confidenceLevel == ConfidenceLevel.R){
                at.setEnd(at.getFrags()[at.getFrags().length-1][2]);
            }
        }
    }
}
