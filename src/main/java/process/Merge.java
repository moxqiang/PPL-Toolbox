package process;

import utils.ContactMerge;

import java.io.IOException;
import java.util.Calendar;

public class Merge {
    Path p;
    private String outPrefix;
    private int cutoff1;
    private int cutoff2;
    private Calendar rightNow = Calendar.getInstance();

    public Merge(Path p) {
        this.p = p;
        this.outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
        this.cutoff1 = p.mergeM;
        this.cutoff2 = p.mergeL;
    }

    public void mergeContacts() throws IOException {
        if (p.refineBoundary.equalsIgnoreCase("Y")) {
            ContactMerge.main(new String[]{
                    outPrefix + ".merged.saved.contacts.refined",
                    outPrefix + ".final.contacts",
                    cutoff1 + "",
                    cutoff2 + ""
            });
        } else {
            ContactMerge.main(new String[]{
                    outPrefix + ".merged.saved.contacts",
                    outPrefix + ".final.contacts",
                    cutoff1 + "",
                    cutoff2 + ""
            });
        }
    }
}
