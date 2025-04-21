package process;

import process.contact.AlignTable2Contact;
import utils.MyUtil;

import java.io.IOException;
import java.util.Calendar;

public class Selection {
    private Path p;
    private String outPrefix;
    private Calendar rightNow = Calendar.getInstance();

    public Selection(Path path){
        p = path;
        outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
    }

    public Selection(Path path, String secondPre) {
        p = path;
        outPrefix = secondPre;
    }

    public void extratContact() throws IOException {
//        AlignTable2Contact.run(p,outPrefix);
        if (p.getLigation_type().equals("res")) {
            MyUtil.checkFormatAligntable(outPrefix + ".aligntable.withFrag");
            AlignTable2Contact.select(outPrefix + ".aligntable.withFrag", outPrefix, p);
        }else {
            MyUtil.checkFormatAligntable(outPrefix + ".aligntable");
            AlignTable2Contact.select(outPrefix + ".aligntable", outPrefix, p);
        }
    }
}
