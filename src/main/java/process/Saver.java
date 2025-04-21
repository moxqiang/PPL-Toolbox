package process;

import utils.SaveFragments;

import java.io.IOException;
import java.util.Calendar;

public class Saver {
    private Path p;
    private String outPrefix;
    private Calendar rightNow = Calendar.getInstance();

    public Saver(Path path){
        p = path;
        outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
    }

    public Saver(Path path, String secondPre) {
        p = path;
        outPrefix = secondPre;
    }

    public void save(String status) throws IOException {
        SaveFragments.main( new String[]{
                outPrefix + ".contacts",
                outPrefix + ".saved.contacts",
                status
        });
    }
}
