package process;

import process.contact.AlignTable2Contact;
import utils.MyUtil;
import utils.RefineBoundary;

import java.io.IOException;
import java.util.Calendar;

public class Refiner {
    private Path p;
    private String outPrefix;
    private Calendar rightNow = Calendar.getInstance();

    public Refiner(Path path){
        p = path;
        outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
    }

    public Refiner(Path path, String secondPre) {
        p = path;
        outPrefix = secondPre;
    }

    public void refineContact() throws IOException {
//        AlignTable2Contact.run(p,outPrefix);
        if (p.getLigation_type().equals("res")) {
            RefineBoundary.main(
                    new String[]{
                            outPrefix + ".contacts",
                            outPrefix + ".contacts.refined"
                    }
            );
        }else {
            return;
        }
    }
}
