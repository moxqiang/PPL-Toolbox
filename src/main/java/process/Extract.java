package process;

import org.apache.commons.beanutils.BeanUtils;
import utils.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Calendar;

public class Extract {
    Path p;
    Path pGaps;
    private String outPrefix;
    private String fastq;
    private String cutoff;
    private String mode;
    private Calendar rightNow = Calendar.getInstance();

    public Extract(Path p) throws InvocationTargetException, IllegalAccessException {
        this.p = p;
        this.outPrefix = p.OUTPUT_DIRECTORY+"/"+p.OUTPUT_PREFIX+"/"+p.OUTPUT_PREFIX;
        this.fastq = p.Fastq_file;
        this.cutoff = "50";
        this.mode = "valid";
        //生成pGaps（替换了比对文件的参数）
        this.generateNewPath();
//        System.out.println("pGaps = " + pGaps);
    }

    public void extractGapsFromContacts() throws IOException, InvocationTargetException, IllegalAccessException {
        String targetFile="";

        if (p.refineBoundary.equalsIgnoreCase("Y")) {
            targetFile = outPrefix + ".contacts.refined";
        } else {
            targetFile = outPrefix + ".contacts";
        }

        //获得覆盖度文件
        MyUtil.checkPath(targetFile);
        CoverageReads.main(new String[]{
                targetFile,
                outPrefix+".contacts.coverage",
                mode
        });

        //覆盖度文件修改
        if (p.refineBoundary.equalsIgnoreCase("Y")) {
            GenerateResSiteOnReads.main(new String[]{
                    p.Fastq_file,
                    outPrefix+".reads.resSites",
                    p.ligation_site
            });
            CoverageReadsRefined.main(new String[]{
                    targetFile,
                    outPrefix+".contacts.coverage",
                    outPrefix+".reads.resSites",
                    mode
            });
        } else {
            CoverageReads.main(new String[]{
                    targetFile,
                    outPrefix + ".contacts.coverage",
                    mode
            });
        }

        //提取gaps
        ExtractGaps.main(new String[]{
                fastq,
                outPrefix+".gaps.fastq.gz",
                outPrefix+".contacts.coverage",
                cutoff
        });

    }

    private void generateNewPath() throws InvocationTargetException, IllegalAccessException {
        this.pGaps = new Path();
        BeanUtils.copyProperties(pGaps, p);
        pGaps.setFastq_file(outPrefix+".gaps.fastq.gz");
        pGaps.setFilter("graph");
        pGaps.setSTART_STEP("2");
        pGaps.setSkipmap("N");
        pGaps.setSplitReads("Y");
    }

    public Path getpGaps() {
        if (pGaps != null)
            return pGaps;
        System.out.println(MyUtil.getTimeFormatted()+" "+
                "ERROR: pGaps doesn't exist !!");
        return null;
    }
}
