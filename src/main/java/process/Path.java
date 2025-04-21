//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package process;

import errors.MyError;
import utils.MyUtil;

import static errors.MyError.*;
import static utils.MyUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Path {
    //获取帮助
    public String help = "N";
    //共同必要参数（5）
    public String ligation_type = "";
    public String genomefile = "";
    //    参数（linker） (necessary=5)
    public String linker = "";
    public String AutoLinker = "true";
    //    参数（酶切）   (necessary=6)
    public String ligation_site = "-";
    public String[] ligation_sites;
    //灵活必要参数
    public String GENOME_INDEX = "";
    public String Fastq_file = "";
    public String skipmap = "N";
    public String restrictionsiteFile = "None";
    //非必要参数
    public String CHROM_SIZE_INFO = "";
    public String GENOME_LENGTH = "";
    public int Ngenome = 0;
    public HashMap<String, Integer> chrMAP = new HashMap();
    public HashMap<Integer, String> chrMAP_r = new HashMap();
    public String START_STEP = "1";
    public String STOP_STEP = "100";
    public String OUTPUT_DIRECTORY = "./";
    public String OUTPUT_PREFIX = "out";
    public String fastp = "";
    public String NTHREADS = "1";
    public String removeResblock = "N";
    public String removeDis = "N";
    public long distanceCutoff = 1000;
    public String removeIsolated = "N";
    public long distanceCutoff2 = 1000000;
    public String bamout = "Y";
    public String cutoffMapq = "20";
    public int minfragsize = 20;
    public int maxfragsize = 1000000;
    public String splitReads = "N";
    public String aligner="minimap2";
    public String weigthtAS="1";
    public String ALLMAP = "false";
    public String MAP2Linker = "false";
    public String search_all_linker = "N";
    public String refineBoundary="N";
        //read dis
    public int mergeM = 1000;
        //chr dis
    public int mergeL = 1000;
    public int confidentDistCutoff = 50;
    public String cutoffFrag = "0.5";
    public int printallreads = 0;
    public int shortestAnchor = 0;
    public int shortestPeak = 1500;
    public String kmerlen = "9";
    //tmp
    public String filter = "res";
    //isolated params
    public int MinfragNum = 5;
    public double MinDomRatio = 0.8;


    //old
    String Fastq_file_1 = "";
    String Fastq_file_2 = "";
    String MODE = "1";
    String minimum_linker_alignment_score = "14";
    String CYTOBAND_DATA;
    String SPECIES;
    String PROGRAM_DIRECTORY;
    String minimum_tag_length = "18";
    String maximum_tag_length = "1000";
    String minSecondBestScoreDiff = "3";
    String output_data_with_ambiguous_linker_info = "1";
    String MERGE_DISTANCE = "2";
    String SELF_LIGATION_CUFOFF = "8000";
    String provide_slc = "N";
    String EXTENSION_LENGTH = "500";
    String EXTENSION_MODE = "1";
    String MIN_COVERAGE_FOR_PEAK = "5";
    String PEAK_MODE = "2";
    String MIN_DISTANCE_BETWEEN_PEAK = "500";
    String GENOME_COVERAGE_RATIO = "0.8";
    String PVALUE_CUTOFF_PEAK = "0.00001";
    String INPUT_ANCHOR_FILE = "null";
    String macs2 = "N";
    String nomodel = "N";
    String broadpeak = "";
    String PVALUE_CUTOFF_INTERACTION = "0.05";
    String FQMODE = "paired-end";
    String XOR_cluster = "N";
    String MAPMEM = "false";
    String printreadID = "N";
    String skipheader = "1000000";
    String linkerreads = "100000";
    String addcluster = "N";
    String hichipM = "N";
    String keeptemp = "N";
    int peakcutoff = 10000;
    int minInsertsize = 1;
    int maxInsertsize = 1000;
    String zipbedpe = "N";
    String zipsam = "N";
    String deletesam = "N";
    String MAP_ambiguous = "N";

    public Path() {
    }

    //仅用于模拟酶切 VirDigestTool
    public Path(String genomefile,  String ligation_site, String restrictionsiteFile) {
        this.ligation_site = ligation_site;
        this.restrictionsiteFile = restrictionsiteFile;
        this.genomefile = genomefile;
        MyUtil.checkPath(genomefile);

        String[] ligations = this.ligation_site.split(",");
        String[] ligation_sites1 = this.ligation_site.split(",");
        for(int k = 0; k < ligations.length; ++k) {
            ligation_sites1[k] = this.getLigationSite(ligations[k]);
        }
        this.ligation_sites = this.replaceN(ligation_sites1);
        System.out.println("[Enzyme site number] " + this.ligation_sites.length);
    }

    @Override
    public String toString() {
        return "Path{" +
                "ligation_type='" + ligation_type + '\'' +
                ", genomefile='" + genomefile + '\'' +
                ", linker='" + linker + '\'' +
                ", AutoLinker='" + AutoLinker + '\'' +
                ", ligation_site='" + ligation_site + '\'' +
                ", ligation_sites=" + Arrays.toString(ligation_sites) +
                ", GENOME_INDEX='" + GENOME_INDEX + '\'' +
                ", Fastq_file='" + Fastq_file + '\'' +
                ", skipmap='" + skipmap + '\'' +
                ", restrictionsiteFile='" + restrictionsiteFile + '\'' +
                ", CHROM_SIZE_INFO='" + CHROM_SIZE_INFO + '\'' +
                ", GENOME_LENGTH='" + GENOME_LENGTH + '\'' +
                ", Ngenome=" + Ngenome +
                ", chrMAP=" + chrMAP +
                ", chrMAP_r=" + chrMAP_r +
                ", START_STEP='" + START_STEP + '\'' +
                ", STOP_STEP='" + STOP_STEP + '\'' +
                ", OUTPUT_DIRECTORY='" + OUTPUT_DIRECTORY + '\'' +
                ", OUTPUT_PREFIX='" + OUTPUT_PREFIX + '\'' +
                ", fastp='" + fastp + '\'' +
                ", NTHREADS='" + NTHREADS + '\'' +
                ", removeResblock='" + removeResblock + '\'' +
                ", removeDis='" + removeDis + '\'' +
                ", distanceCutoff=" + distanceCutoff +
                ", bamout='" + bamout + '\'' +
                ", cutoffMapq='" + cutoffMapq + '\'' +
                ", minfragsize=" + minfragsize +
                ", maxfragsize=" + maxfragsize +
                ", splitReads='" + splitReads + '\'' +
                ", aligner='" + aligner + '\'' +
                ", weigthtAS='" + weigthtAS + '\'' +
                ", ALLMAP='" + ALLMAP + '\'' +
                ", MAP2Linker='" + MAP2Linker + '\'' +
                ", search_all_linker='" + search_all_linker + '\'' +
                ", mergeM=" + mergeM +
                ", mergeL=" + mergeL +
                ", cutoffFrag='" + cutoffFrag + '\'' +
                ", printallreads=" + printallreads +
                ", shortestAnchor=" + shortestAnchor +
                ", shortestPeak=" + shortestPeak +
                ", kmerlen='" + kmerlen + '\'' +
                ", filter='" + filter + '\'' +
                ", Fastq_file_1='" + Fastq_file_1 + '\'' +
                ", Fastq_file_2='" + Fastq_file_2 + '\'' +
                ", MODE='" + MODE + '\'' +
                ", minimum_linker_alignment_score='" + minimum_linker_alignment_score + '\'' +
                ", CYTOBAND_DATA='" + CYTOBAND_DATA + '\'' +
                ", SPECIES='" + SPECIES + '\'' +
                ", PROGRAM_DIRECTORY='" + PROGRAM_DIRECTORY + '\'' +
                ", minimum_tag_length='" + minimum_tag_length + '\'' +
                ", maximum_tag_length='" + maximum_tag_length + '\'' +
                ", minSecondBestScoreDiff='" + minSecondBestScoreDiff + '\'' +
                ", output_data_with_ambiguous_linker_info='" + output_data_with_ambiguous_linker_info + '\'' +
                ", MERGE_DISTANCE='" + MERGE_DISTANCE + '\'' +
                ", SELF_LIGATION_CUFOFF='" + SELF_LIGATION_CUFOFF + '\'' +
                ", provide_slc='" + provide_slc + '\'' +
                ", EXTENSION_LENGTH='" + EXTENSION_LENGTH + '\'' +
                ", EXTENSION_MODE='" + EXTENSION_MODE + '\'' +
                ", MIN_COVERAGE_FOR_PEAK='" + MIN_COVERAGE_FOR_PEAK + '\'' +
                ", PEAK_MODE='" + PEAK_MODE + '\'' +
                ", MIN_DISTANCE_BETWEEN_PEAK='" + MIN_DISTANCE_BETWEEN_PEAK + '\'' +
                ", GENOME_COVERAGE_RATIO='" + GENOME_COVERAGE_RATIO + '\'' +
                ", PVALUE_CUTOFF_PEAK='" + PVALUE_CUTOFF_PEAK + '\'' +
                ", INPUT_ANCHOR_FILE='" + INPUT_ANCHOR_FILE + '\'' +
                ", macs2='" + macs2 + '\'' +
                ", nomodel='" + nomodel + '\'' +
                ", broadpeak='" + broadpeak + '\'' +
                ", PVALUE_CUTOFF_INTERACTION='" + PVALUE_CUTOFF_INTERACTION + '\'' +
                ", FQMODE='" + FQMODE + '\'' +
                ", XOR_cluster='" + XOR_cluster + '\'' +
                ", MAPMEM='" + MAPMEM + '\'' +
                ", printreadID='" + printreadID + '\'' +
                ", skipheader='" + skipheader + '\'' +
                ", linkerreads='" + linkerreads + '\'' +
                ", addcluster='" + addcluster + '\'' +
                ", hichipM='" + hichipM + '\'' +
                ", keeptemp='" + keeptemp + '\'' +
                ", peakcutoff=" + peakcutoff +
                ", minInsertsize=" + minInsertsize +
                ", maxInsertsize=" + maxInsertsize +
                ", zipbedpe='" + zipbedpe + '\'' +
                ", zipsam='" + zipsam + '\'' +
                ", deletesam='" + deletesam + '\'' +
                ", MAP_ambiguous='" + MAP_ambiguous + '\'' +
                '}';
    }

    //检查参数设置的合法性
    public void checkParams(){
        //寻求帮助
        if (this.help.equalsIgnoreCase("Y")){
            needHelp();
        }
        //检查运行步骤设置的合法性
        if (Integer.parseInt(this.STOP_STEP)< Integer.parseInt(this.START_STEP) ||
        Integer.parseInt(this.START_STEP) <= 0){
            stepError();
        }
        if (Integer.parseInt(this.START_STEP) >=3 ){
            this.skipmap="Y";
        }
        //必须提供fastq文件
        if (this.Fastq_file.equals("")) {
            System.out.println("fastq needed !!!");
            MyError.notEnoughParamError();
        }
        //必须提供genome文件
        if (this.genomefile.equals("")){
            System.out.println("genome file needed !!!");
            MyError.notEnoughParamError();
        }
        //使用了bwasw，没有跳过了比对，也没给定索引文件
        if (this.skipmap.equals("N") && this.aligner.equals("bwasw")) {
            if (this.GENOME_INDEX.equals(""))
                MyError.notEnoughParamError();
        }
        //当选择跳过比对时，检查是否存在bam文件
        if (this.skipmap.equals("Y") && Integer.parseInt(this.START_STEP) <=2 ){
//            for (int i=0; i<this.Fastq_file.split(",").length; i++)
//                if (!MyUtil.checkPathV(this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+"."+i+".bam")){
//                    System.out.println("!!!If you want to skip mapping step, "+this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+"."+i+".bam "+"is needed!!!");
//                    MyUtil.checkPath(this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+"."+i+".bam");
//                }
            if (!(MyUtil.checkPathV(this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+".bam"))
            ){
                System.out.println("!!!If you want to skip mapping step, "+this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+".bam"+" is needed!!!");
                MyUtil.checkPath(this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+".bam");
            }
        }
        //没有跳过比对，但是没有提供索引文件和fq文件
        if (this.skipmap.equals("N")){
            if (this.aligner.contains("bwa")) {
                if (this.GENOME_INDEX.equals("") || this.Fastq_file.equals("")) {
                    System.out.println("fastq and bwa index needed !!!");
                    MyError.notEnoughParamError();
                }
            }else{
                if (this.Fastq_file.equals("")) {
                    System.out.println("fastq needed !!!");
                    MyError.notEnoughParamError();
                }
            }
        }
        //运行模式没有选择
        if (this.ligation_type.equals("")){
            MyError.notEnoughParamError();
            System.out.println("ligation type need to be selected !!!");
        }
        //没有给定酶切片段，也没有跳过比对，则需要给定基因组
        if ((this.restrictionsiteFile.equals("None") || this.skipmap.equals("N")) && this.genomefile.equals("")){
            System.out.println("genome file needed !!!");
            MyError.notEnoughParamError();
        }
        //选择了res模式，但是没有给定酶切位点或酶切文件
        if (this.ligation_type.equals("res") && (this.restrictionsiteFile.equals("None")&&this.ligation_site.equals("-"))){
            System.out.println("Restriction site or restriction site file needed");
            MyError.notEnoughParamError();
        }
        //从第三部开始执行，检查aligntable文件
        if (this.START_STEP.equals("3")){
            MyUtil.checkPath(this.OUTPUT_DIRECTORY+'/'+this.OUTPUT_PREFIX+'/'+this.OUTPUT_PREFIX+".aligntable.withFrag");
        }
    }

    public String getLigationSite(String ligation_site) {
        String site = "";
        if (ligation_site.equalsIgnoreCase("HindIII")) {
            site = "A^AGCTT";
        } else if (ligation_site.equalsIgnoreCase("MboI")) {
            site = "^GATC";
        } else if (ligation_site.equalsIgnoreCase("BglII")) {
            site = "A^GATCT";
        } else if (ligation_site.equalsIgnoreCase("DpnII")) {
            site = "^GATC";
        } else if (ligation_site.equalsIgnoreCase("Sau3AI")) {
            site = "^GATC";
        } else if (ligation_site.equalsIgnoreCase("NlaIII")) {
            site = "CATG^";
        } else if (ligation_site.equalsIgnoreCase("Hinf1")) {
            site = "G^ANTC";
        } else if (ligation_site.equalsIgnoreCase("AluI")) {
            site = "AG^CT";
        } else if (! ligation_site.contains("^")) {
            System.out.println("Error: the restriction position has to be specified using '^'\nPlease, use '^' to specify the cutting position\ni.e A^GATCT for HindIII digestion.\n");
            System.exit(0);
        } else {
            site = ligation_site;
        }
//        错误字符排除
        for(int j = 0; j < site.length(); ++j) {
            if (site.charAt(j) != 'C' && site.charAt(j) != 'c' && site.charAt(j) != 'T' && site.charAt(j) != 't' && site.charAt(j) != 'G' && site.charAt(j) != 'g' && site.charAt(j) != 'A' && site.charAt(j) != 'a' && site.charAt(j) != '^' && site.charAt(j) != 'N' && site.charAt(j) != 'n') {
                System.out.println("Error: \nPlease print HindIII/MboI/BglII/DpnII or restriction site with '^' and contains 'ATCG' without other character!!! " + site + " : " + site.charAt(j));
                System.exit(0);
            }
        }

        return site;
    }

    public String[] replaceN(String[] ligs) {
        int Nlig = 0;

        for(int i = 0; i < ligs.length; ++i) {
            if (ligs[i].contains("N") || ligs[i].contains("n")) {
                Nlig += 3;
            }
        }

        String[] ligs2 = new String[Nlig + ligs.length];
        int i;
        if (Nlig > 0) {
            int k = 0;

            for(i = 0; i < ligs.length; ++i) {
                if (ligs[i].contains("N")) {
                    ligs2[k] = ligs[i].replace('N', 'A');
                    ligs2[k + 1] = ligs[i].replace('N', 'T');
                    ligs2[k + 2] = ligs[i].replace('N', 'C');
                    ligs2[k + 3] = ligs[i].replace('N', 'G');
                    k += 4;
                } else if (ligs[i].contains("n")) {
                    ligs2[k] = ligs[i].replace('n', 'A');
                    ligs2[k + 1] = ligs[i].replace('n', 'T');
                    ligs2[k + 2] = ligs[i].replace('n', 'C');
                    ligs2[k + 3] = ligs[i].replace('n', 'G');
                    k += 4;
                } else {
                    ligs2[k] = ligs[i];
                    ++k;
                }
            }
        } else {
            ligs2 = ligs;
        }

        String[] var7 = ligs2;
        int var6 = ligs2.length;

        for(i = 0; i < var6; ++i) {
            String str = var7[i];
//            System.out.println("[Enzyme] " + str);
        }

        List mylist = Arrays.asList(ligs2);
        Set myset = new HashSet(mylist);
        String[] ligs3 = (String[])myset.toArray(new String[0]);
        return ligs3;
    }

    public void setParameter(String[] args) throws IOException {
        this.PROGRAM_DIRECTORY = (new Path()).getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (this.PROGRAM_DIRECTORY.endsWith(".jar")) {
            this.PROGRAM_DIRECTORY = this.PROGRAM_DIRECTORY.substring(0, this.PROGRAM_DIRECTORY.lastIndexOf("/") + 1);
        }

        this.OUTPUT_DIRECTORY = this.PROGRAM_DIRECTORY;
        int necessary = 0;
        //读取参数设置
        for(int i = 0; i < args.length; i += 2) {
            //获取帮助
            if (args[i].equals("--help")){
                this.help = args[i+1].toLowerCase();
            }
            //连接模式设置
            if (args[i].equals("--ligation_type")){
                this.ligation_type = args[i+1].toLowerCase();
                if (!this.ligation_type.equalsIgnoreCase("res" )&& !this.ligation_type.equalsIgnoreCase("linker")){
                    System.out.println("Error: ligation_type " + args[i + 1] + " is incorrect");
                    System.exit(0);
                }
                ++necessary;
            }
            //必要参数设置
            else if (args[i].equals("--fastq")) {
                this.Fastq_file = args[i + 1];
                String[] fqs = this.Fastq_file.split(",");
                System.out.println(Arrays.toString(fqs));
                for (String fq :
                        fqs) {
                    this.checkPath(fq);
                }
                ++necessary;
            } else if (args[i].equals("--CHROM_SIZE_INFO")) {
                this.CHROM_SIZE_INFO = args[i + 1];
                this.checkPath(args[i + 1]);
                ++necessary;
                BufferedReader reader = new BufferedReader(new FileReader(this.CHROM_SIZE_INFO));
                long genomeLength=0;
                for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                    this.chrMAP.put(line.split("[ \t]+")[0], this.Ngenome);
                    this.chrMAP_r.put(this.Ngenome, line.split("[ \t]+")[0]);
                    genomeLength+=Integer.parseInt(line.split("[ \t]+")[1]);
//                    System.out.println(Integer.parseInt(line.split("[ \t]+")[1]));
//                    System.out.println("genomeLength = " + genomeLength);
                    ++this.Ngenome;
                }
                this.GENOME_LENGTH=""+genomeLength;
            } else if (args[i].equals("--GENOME_INDEX")){
                this.GENOME_INDEX = args[i + 1];
                //检查bwa索引文件是否完整
                String prefix = (new File(args[i + 1])).getName();
                File parentDir = (new File(args[i + 1])).getParentFile();
                File[] files = parentDir.listFiles();
                int n = 0;
                File[] var11 = files;
                int var10 = files.length;
                for(int var9 = 0; var9 < var10; ++var9) {
                    File f = var11[var9];
                    if (f.getName().equals(prefix + ".amb") || f.getName().equals(prefix + ".ann") || f.getName().equals(prefix + ".bwt") || f.getName().equals(prefix + ".pac") || f.getName().equals(prefix + ".sa")) {
                        ++n;
                    }
                }

                if (n != 5) {
                    System.out.println("Error: please check genome index (*.amb, *.ann, *.bwt, *pac and *.sa, these files are necessary)");
                    System.exit(0);
                }

                ++necessary;
            } else if (args[i].equals("--genomefile")) {
                this.genomefile = args[i + 1];
                this.checkPath(this.genomefile);
                ++necessary;
            }
            //酶切连接模式相关设置
            else if (args[i].equals("--ligation_site")) {
                this.ligation_site = args[i + 1];
                String[] ligations = this.ligation_site.split(",");
                String[] ligation_sites1 = this.ligation_site.split(",");
                for(int k = 0; k < ligations.length; ++k) {
                    ligation_sites1[k] = this.getLigationSite(ligations[k]);
                }
                this.ligation_sites = this.replaceN(ligation_sites1);
                System.out.println("[Enzyme site number] " + this.ligation_sites.length);
            } else if (args[i].equals("--restrictionsiteFile")) {
                this.restrictionsiteFile = args[i + 1];
                checkPath(this.restrictionsiteFile);
                if (this.ligation_site.equalsIgnoreCase("-")){
                    necessary++;
                }
            }
            //linker连接模式相关设置
            else if (args[i].equals("--linker")) {
                this.linker = args[i + 1];
                checkPath(linker);
                this.AutoLinker = "false";
//                ++necessary;
            }
            //非必要设置
            else if (args[i].equals("--start_step")) {
                this.START_STEP = args[i + 1];
                checkDigit(this.START_STEP);
            } else if (args[i].equals("--stop_step")) {
                this.STOP_STEP = args[i + 1];
                checkDigit(this.STOP_STEP);
            } else if (args[i].equals("--thread")) {
                this.NTHREADS = args[i + 1];
                checkDigit(this.NTHREADS);
            } else if (args[i].equals("--fastp")) {
                this.fastp = args[i + 1];
                if (!this.fastp.equals("fastp")) {
                    this.checkPath(args[i + 1]);
                }
            } else if (args[i].equals("--skipmap")) {
                this.skipmap = args[i + 1].toUpperCase();
                checkNY(this.skipmap);
            } else if (args[i].equals("--bamout")) {
                this.bamout = args[i + 1].toUpperCase();
                checkNY(this.bamout);
            } else if (args[i].equals("--cutoffMapq")) {
                this.cutoffMapq = args[i + 1];
                checkDigit(this.cutoffMapq);
            }else if (args[i].equals("--mergeL")) {
                checkDigit(args[i+1]);
                this.mergeL = Integer.parseInt(args[i + 1]);
            }else if (args[i].equals("--mergeM")) {
                checkDigit(args[i+1]);
                this.mergeM = Integer.parseInt(args[i + 1]);
            }else if (args[i].equals("--minfragsize")) {
                this.minfragsize = Integer.parseInt(args[i + 1]);
            }else if (args[i].equals("--maxfragsize")) {
                this.maxfragsize = Integer.parseInt(args[i + 1]);
            }else if (args[i].equals("--resRemove")) {
                this.removeResblock = args[i + 1].toUpperCase();
                checkNY(this.removeResblock);
            }else if (args[i].equals("--disRemove")) {
                this.removeDis = args[i + 1].toUpperCase();
                checkNY(this.removeDis);
            }else if (args[i].equals("--distanceCutoff")) {
                MyUtil.checkDigit(args[i + 1]);
                this.distanceCutoff = Long.parseLong(args[i + 1]);
            }else if (args[i].equals("--isolatedRemove")) {
                this.removeIsolated = args[i + 1].toUpperCase();
                checkNY(this.removeDis);
            }else if (args[i].equals("--distanceCutoff2")) {
                MyUtil.checkDigit(args[i + 1]);
                this.distanceCutoff2 = Long.parseLong(args[i + 1]);
            }else if (args[i].equals("--splitReads")) {
                this.splitReads = args[i + 1].toUpperCase();
                checkNY(this.skipmap);
            }else if (args[i].equals("--aligner")) {
                this.aligner = args[i + 1].toLowerCase();
                if (this.aligner.equals("bwasw")||this.aligner.equals("minimap2")){
                    continue;
                }else {
                    MyError.UnknownAlignerError(this.aligner);
                }
            }else if (args[i].equals("--weightAS")) {
                MyUtil.checkNum(args[i+1]);
                this.weigthtAS = args[i+1];
            }else if (args[i].equals("--cutoffFrag")) {
                MyUtil.checkNum(args[i+1]);
                this.cutoffFrag = args[i+1];
            }
            else if (args[i].equals("--filter")) {
                this.filter = args[i+1];
            }
            else if (args[i].equals("--refineBoundary")) {
                this.refineBoundary = args[i+1];
                MyUtil.checkNY(this.refineBoundary);
            }
            //输出设置
            else if (args[i].equals("--output")) {
                this.OUTPUT_DIRECTORY = args[i + 1];
                checkPath(this.OUTPUT_DIRECTORY);
            } else if (args[i].equals("--prefix")) {
                this.OUTPUT_PREFIX = args[i + 1];
            }
            // 孤立片段相关参数
            else if (args[i].equals("--isolated_min_frag_num")){
                MyUtil.checkNum(args[i+1]);
                this.MinfragNum = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("--isolated_dom_ratio")){
                MyUtil.checkNum(args[i+1]);
                this.MinDomRatio = Double.parseDouble(args[i + 1]);
            }

            //未知参数
            else {
//                System.out.println("Error: unexpected paramater: " + args[i]);
//                System.exit(0);
                unexpectedParamError(args[i]);
            }
        }
//        //缺少必要参数
//        if (necessary < 5 || this.ligation_type.isEmpty()){
//            notEnoughParamError();
//        } else if (this.ligation_type.equals("res") && necessary < 6){
//            notEnoughParamError();
//        } else if (this.ligation_type.equals("linker") && necessary < 5){
//            notEnoughParamError();
//
//        }
    }

    public void checkPath(String str) {
        MyUtil.checkPath(str);
    }

    public String getFastq_file() {
        return Fastq_file;
    }

    public void setFastq_file(String fastq_file) {
        Fastq_file = fastq_file;

    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getPROGRAM_DIRECTORY() {
        return this.PROGRAM_DIRECTORY;
    }

    public String getOUTPUT_DIRECTORY() {
        return this.OUTPUT_DIRECTORY;
    }

    public String getOUTPUT_PREFIX() {
        return this.OUTPUT_PREFIX;
    }

    public String getFastq_file_1() {
        return this.Fastq_file_1;
    }

    public String getFastq_file_2() {
        return this.Fastq_file_2;
    }

    public String getLinker() {
        return this.linker;
    }

    public String getMinimum_linker_alignment_score() {
        return this.minimum_linker_alignment_score;
    }

    public String getMinimum_tag_length() {
        return this.minimum_tag_length;
    }

    public String getMaximum_tag_length() {
        return this.maximum_tag_length;
    }

    public String getMinSecondBestScoreDiff() {
        return this.minSecondBestScoreDiff;
    }

    public String getOutput_data_with_ambiguous_linker_info() {
        return this.output_data_with_ambiguous_linker_info;
    }

    public String getNTHREADS() {
        return this.NTHREADS;
    }

    public String getCutoffMapq() {
        return this.cutoffMapq;
    }

    public String getMERGE_DISTANCE() {
        return this.MERGE_DISTANCE;
    }

    public void setCutoffMapq(String cutoffMapq) {
        this.cutoffMapq = cutoffMapq;
    }

    public String getLigation_type() {
        return ligation_type;
    }

    public void setLigation_type(String ligation_type) {
        this.ligation_type = ligation_type;
    }

    public String getGenomefile() {
        return genomefile;
    }

    public void setGenomefile(String genomefile) {
        this.genomefile = genomefile;
    }

    public void setLinker(String linker) {
        this.linker = linker;
    }

    public String getAutoLinker() {
        return AutoLinker;
    }

    public void setAutoLinker(String autoLinker) {
        AutoLinker = autoLinker;
    }

    public String getLigation_site() {
        return ligation_site;
    }

    public void setLigation_site(String ligation_site) {
        this.ligation_site = ligation_site;
    }

    public String[] getLigation_sites() {
        return ligation_sites;
    }

    public void setLigation_sites(String[] ligation_sites) {
        this.ligation_sites = ligation_sites;
    }

    public String getGENOME_INDEX() {
        return GENOME_INDEX;
    }

    public void setGENOME_INDEX(String GENOME_INDEX) {
        this.GENOME_INDEX = GENOME_INDEX;
    }

    public String getSkipmap() {
        return skipmap;
    }

    public void setSkipmap(String skipmap) {
        this.skipmap = skipmap;
    }

    public String getRestrictionsiteFile() {
        return restrictionsiteFile;
    }

    public void setRestrictionsiteFile(String restrictionsiteFile) {
        this.restrictionsiteFile = restrictionsiteFile;
    }

    public String getCHROM_SIZE_INFO() {
        return CHROM_SIZE_INFO;
    }

    public void setCHROM_SIZE_INFO(String CHROM_SIZE_INFO) {
        this.CHROM_SIZE_INFO = CHROM_SIZE_INFO;
    }

    public String getGENOME_LENGTH() {
        return GENOME_LENGTH;
    }

    public void setGENOME_LENGTH(String GENOME_LENGTH) {
        this.GENOME_LENGTH = GENOME_LENGTH;
    }

    public int getNgenome() {
        return Ngenome;
    }

    public void setNgenome(int ngenome) {
        Ngenome = ngenome;
    }

    public HashMap<String, Integer> getChrMAP() {
        return chrMAP;
    }

    public void setChrMAP(HashMap<String, Integer> chrMAP) {
        this.chrMAP = chrMAP;
    }

    public HashMap<Integer, String> getChrMAP_r() {
        return chrMAP_r;
    }

    public void setChrMAP_r(HashMap<Integer, String> chrMAP_r) {
        this.chrMAP_r = chrMAP_r;
    }

    public String getSTART_STEP() {
        return START_STEP;
    }

    public void setSTART_STEP(String START_STEP) {
        this.START_STEP = START_STEP;
    }

    public String getSTOP_STEP() {
        return STOP_STEP;
    }

    public void setSTOP_STEP(String STOP_STEP) {
        this.STOP_STEP = STOP_STEP;
    }

    public void setOUTPUT_DIRECTORY(String OUTPUT_DIRECTORY) {
        this.OUTPUT_DIRECTORY = OUTPUT_DIRECTORY;
    }

    public void setOUTPUT_PREFIX(String OUTPUT_PREFIX) {
        this.OUTPUT_PREFIX = OUTPUT_PREFIX;
    }

    public String getFastp() {
        return fastp;
    }

    public void setFastp(String fastp) {
        this.fastp = fastp;
    }

    public void setNTHREADS(String NTHREADS) {
        this.NTHREADS = NTHREADS;
    }

    public String getRemoveResblock() {
        return removeResblock;
    }

    public void setRemoveResblock(String removeResblock) {
        this.removeResblock = removeResblock;
    }

    public String getRemoveDis() {
        return removeDis;
    }

    public void setRemoveDis(String removeDis) {
        this.removeDis = removeDis;
    }

    public long getDistanceCutoff() {
        return distanceCutoff;
    }

    public void setDistanceCutoff(long distanceCutoff) {
        this.distanceCutoff = distanceCutoff;
    }

    public String getBamout() {
        return bamout;
    }

    public void setBamout(String bamout) {
        this.bamout = bamout;
    }

    public int getMinfragsize() {
        return minfragsize;
    }

    public void setMinfragsize(int minfragsize) {
        this.minfragsize = minfragsize;
    }

    public int getMaxfragsize() {
        return maxfragsize;
    }

    public void setMaxfragsize(int maxfragsize) {
        this.maxfragsize = maxfragsize;
    }

    public String getSplitReads() {
        return splitReads;
    }

    public void setSplitReads(String splitReads) {
        this.splitReads = splitReads;
    }

    public String getAligner() {
        return aligner;
    }

    public void setAligner(String aligner) {
        this.aligner = aligner;
    }

    public String getWeigthtAS() {
        return weigthtAS;
    }

    public void setWeigthtAS(String weigthtAS) {
        this.weigthtAS = weigthtAS;
    }

    public String getALLMAP() {
        return ALLMAP;
    }

    public void setALLMAP(String ALLMAP) {
        this.ALLMAP = ALLMAP;
    }

    public String getMAP2Linker() {
        return MAP2Linker;
    }

    public void setMAP2Linker(String MAP2Linker) {
        this.MAP2Linker = MAP2Linker;
    }

    public String getSearch_all_linker() {
        return search_all_linker;
    }

    public void setSearch_all_linker(String search_all_linker) {
        this.search_all_linker = search_all_linker;
    }

    public int getMergeM() {
        return mergeM;
    }

    public void setMergeM(int mergeM) {
        this.mergeM = mergeM;
    }

    public int getMergeL() {
        return mergeL;
    }

    public void setMergeL(int mergeL) {
        this.mergeL = mergeL;
    }

    public String getCutoffFrag() {
        return cutoffFrag;
    }

    public void setCutoffFrag(String cutoffFrag) {
        this.cutoffFrag = cutoffFrag;
    }

    public int getPrintallreads() {
        return printallreads;
    }

    public void setPrintallreads(int printallreads) {
        this.printallreads = printallreads;
    }

    public int getShortestAnchor() {
        return shortestAnchor;
    }

    public void setShortestAnchor(int shortestAnchor) {
        this.shortestAnchor = shortestAnchor;
    }

    public int getShortestPeak() {
        return shortestPeak;
    }

    public void setShortestPeak(int shortestPeak) {
        this.shortestPeak = shortestPeak;
    }

    public String getKmerlen() {
        return kmerlen;
    }

    public void setKmerlen(String kmerlen) {
        this.kmerlen = kmerlen;
    }

    public void setFastq_file_1(String fastq_file_1) {
        Fastq_file_1 = fastq_file_1;
    }

    public void setFastq_file_2(String fastq_file_2) {
        Fastq_file_2 = fastq_file_2;
    }

    public String getMODE() {
        return MODE;
    }

    public void setMODE(String MODE) {
        this.MODE = MODE;
    }

    public void setMinimum_linker_alignment_score(String minimum_linker_alignment_score) {
        this.minimum_linker_alignment_score = minimum_linker_alignment_score;
    }

    public String getCYTOBAND_DATA() {
        return CYTOBAND_DATA;
    }

    public void setCYTOBAND_DATA(String CYTOBAND_DATA) {
        this.CYTOBAND_DATA = CYTOBAND_DATA;
    }

    public String getSPECIES() {
        return SPECIES;
    }

    public void setSPECIES(String SPECIES) {
        this.SPECIES = SPECIES;
    }

    public void setPROGRAM_DIRECTORY(String PROGRAM_DIRECTORY) {
        this.PROGRAM_DIRECTORY = PROGRAM_DIRECTORY;
    }

    public void setMinimum_tag_length(String minimum_tag_length) {
        this.minimum_tag_length = minimum_tag_length;
    }

    public void setMaximum_tag_length(String maximum_tag_length) {
        this.maximum_tag_length = maximum_tag_length;
    }

    public void setMinSecondBestScoreDiff(String minSecondBestScoreDiff) {
        this.minSecondBestScoreDiff = minSecondBestScoreDiff;
    }

    public void setOutput_data_with_ambiguous_linker_info(String output_data_with_ambiguous_linker_info) {
        this.output_data_with_ambiguous_linker_info = output_data_with_ambiguous_linker_info;
    }

    public void setMERGE_DISTANCE(String MERGE_DISTANCE) {
        this.MERGE_DISTANCE = MERGE_DISTANCE;
    }

    public String getSELF_LIGATION_CUFOFF() {
        return SELF_LIGATION_CUFOFF;
    }

    public void setSELF_LIGATION_CUFOFF(String SELF_LIGATION_CUFOFF) {
        this.SELF_LIGATION_CUFOFF = SELF_LIGATION_CUFOFF;
    }

    public String getProvide_slc() {
        return provide_slc;
    }

    public void setProvide_slc(String provide_slc) {
        this.provide_slc = provide_slc;
    }

    public String getEXTENSION_LENGTH() {
        return EXTENSION_LENGTH;
    }

    public void setEXTENSION_LENGTH(String EXTENSION_LENGTH) {
        this.EXTENSION_LENGTH = EXTENSION_LENGTH;
    }

    public String getEXTENSION_MODE() {
        return EXTENSION_MODE;
    }

    public void setEXTENSION_MODE(String EXTENSION_MODE) {
        this.EXTENSION_MODE = EXTENSION_MODE;
    }

    public String getMIN_COVERAGE_FOR_PEAK() {
        return MIN_COVERAGE_FOR_PEAK;
    }

    public void setMIN_COVERAGE_FOR_PEAK(String MIN_COVERAGE_FOR_PEAK) {
        this.MIN_COVERAGE_FOR_PEAK = MIN_COVERAGE_FOR_PEAK;
    }

    public String getPEAK_MODE() {
        return PEAK_MODE;
    }

    public void setPEAK_MODE(String PEAK_MODE) {
        this.PEAK_MODE = PEAK_MODE;
    }

    public String getMIN_DISTANCE_BETWEEN_PEAK() {
        return MIN_DISTANCE_BETWEEN_PEAK;
    }

    public void setMIN_DISTANCE_BETWEEN_PEAK(String MIN_DISTANCE_BETWEEN_PEAK) {
        this.MIN_DISTANCE_BETWEEN_PEAK = MIN_DISTANCE_BETWEEN_PEAK;
    }

    public String getGENOME_COVERAGE_RATIO() {
        return GENOME_COVERAGE_RATIO;
    }

    public void setGENOME_COVERAGE_RATIO(String GENOME_COVERAGE_RATIO) {
        this.GENOME_COVERAGE_RATIO = GENOME_COVERAGE_RATIO;
    }

    public String getPVALUE_CUTOFF_PEAK() {
        return PVALUE_CUTOFF_PEAK;
    }

    public void setPVALUE_CUTOFF_PEAK(String PVALUE_CUTOFF_PEAK) {
        this.PVALUE_CUTOFF_PEAK = PVALUE_CUTOFF_PEAK;
    }

    public String getINPUT_ANCHOR_FILE() {
        return INPUT_ANCHOR_FILE;
    }

    public void setINPUT_ANCHOR_FILE(String INPUT_ANCHOR_FILE) {
        this.INPUT_ANCHOR_FILE = INPUT_ANCHOR_FILE;
    }

    public String getMacs2() {
        return macs2;
    }

    public void setMacs2(String macs2) {
        this.macs2 = macs2;
    }

    public String getNomodel() {
        return nomodel;
    }

    public void setNomodel(String nomodel) {
        this.nomodel = nomodel;
    }

    public String getBroadpeak() {
        return broadpeak;
    }

    public void setBroadpeak(String broadpeak) {
        this.broadpeak = broadpeak;
    }

    public String getPVALUE_CUTOFF_INTERACTION() {
        return PVALUE_CUTOFF_INTERACTION;
    }

    public void setPVALUE_CUTOFF_INTERACTION(String PVALUE_CUTOFF_INTERACTION) {
        this.PVALUE_CUTOFF_INTERACTION = PVALUE_CUTOFF_INTERACTION;
    }

    public String getFQMODE() {
        return FQMODE;
    }

    public void setFQMODE(String FQMODE) {
        this.FQMODE = FQMODE;
    }

    public String getXOR_cluster() {
        return XOR_cluster;
    }

    public void setXOR_cluster(String XOR_cluster) {
        this.XOR_cluster = XOR_cluster;
    }

    public String getMAPMEM() {
        return MAPMEM;
    }

    public void setMAPMEM(String MAPMEM) {
        this.MAPMEM = MAPMEM;
    }

    public String getPrintreadID() {
        return printreadID;
    }

    public void setPrintreadID(String printreadID) {
        this.printreadID = printreadID;
    }

    public String getSkipheader() {
        return skipheader;
    }

    public void setSkipheader(String skipheader) {
        this.skipheader = skipheader;
    }

    public String getLinkerreads() {
        return linkerreads;
    }

    public void setLinkerreads(String linkerreads) {
        this.linkerreads = linkerreads;
    }

    public String getAddcluster() {
        return addcluster;
    }

    public void setAddcluster(String addcluster) {
        this.addcluster = addcluster;
    }

    public String getHichipM() {
        return hichipM;
    }

    public void setHichipM(String hichipM) {
        this.hichipM = hichipM;
    }

    public String getKeeptemp() {
        return keeptemp;
    }

    public void setKeeptemp(String keeptemp) {
        this.keeptemp = keeptemp;
    }

    public int getPeakcutoff() {
        return peakcutoff;
    }

    public void setPeakcutoff(int peakcutoff) {
        this.peakcutoff = peakcutoff;
    }

    public int getMinInsertsize() {
        return minInsertsize;
    }

    public void setMinInsertsize(int minInsertsize) {
        this.minInsertsize = minInsertsize;
    }

    public int getMaxInsertsize() {
        return maxInsertsize;
    }

    public void setMaxInsertsize(int maxInsertsize) {
        this.maxInsertsize = maxInsertsize;
    }

    public String getZipbedpe() {
        return zipbedpe;
    }

    public void setZipbedpe(String zipbedpe) {
        this.zipbedpe = zipbedpe;
    }

    public String getZipsam() {
        return zipsam;
    }

    public void setZipsam(String zipsam) {
        this.zipsam = zipsam;
    }

    public String getDeletesam() {
        return deletesam;
    }

    public void setDeletesam(String deletesam) {
        this.deletesam = deletesam;
    }

    public String getMAP_ambiguous() {
        return MAP_ambiguous;
    }

    public void setMAP_ambiguous(String MAP_ambiguous) {
        this.MAP_ambiguous = MAP_ambiguous;
    }
}
