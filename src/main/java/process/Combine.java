package process;

import utils.MergeContacts;

import java.io.IOException;
import java.util.Calendar;

public class Combine {
    private Path p;
    private String outPrefix;
    private Calendar rightNow = Calendar.getInstance();

    public Combine(Path path){
        p = path;
        outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
    }

    public void mergeContacts() throws IOException {
        String targetFile = "";
//        if (p.refineBoundary.equalsIgnoreCase("Y")){
//            targetFile = outPrefix + ".contacts.refined";
//        }else {
//            targetFile = outPrefix + ".contacts";
//        }
        targetFile = outPrefix + ".contacts";
        MergeContacts.main(new String[]{
                targetFile,
//                outPrefix + ".gaps.contacts",
                outPrefix + ".gaps.aligntable.withFrag",
//                outPrefix + ".merged.contacts"
                outPrefix + ".merged.aligntable.withFrag"
        });
    }
}
