package process;

import utils.AssignFragment;
import utils.ContactMerge;

import java.io.IOException;
import java.util.Calendar;

public class Assign {
    Path p;
    private String outPrefix;
    private String restrictionFile;
    private String cutoff;
    private String mode;
    private Calendar rightNow = Calendar.getInstance();

    public Assign(Path p) {
        this.p = p;
        this.outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
        this.restrictionFile = p.restrictionsiteFile;
        this.cutoff = "20";
        this.mode = "a";
    }

    public void AssignFragmentToContacts() throws IOException {
        AssignFragment.main(new String[]{
                outPrefix+".contacts.merged",
                restrictionFile,
                outPrefix+".contacts.merged.withFrag",
                cutoff,
                mode
        });
    }
}
