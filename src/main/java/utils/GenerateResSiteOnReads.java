package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GenerateResSiteOnReads {
    private static final int MIN_LEN = 50;

    public static String[] replaceN(String[] ligs) {
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

    public static String getLigationSite(String ligation_site) {
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

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.GenerateResSiteOnReads <fastq> <ResSites> <ResMotif>");
            System.exit(0);
        }
        String fq = args[0];
        MyUtil.checkPath(fq);
        String fqs = args[1];
        String ligation_site = args[2];
        String[] ligations = ligation_site.split(",");
        String[] ligation_sites1 = ligation_site.split(",");
        for(int k = 0; k < ligations.length; ++k) {
            ligation_sites1[k] = getLigationSite(ligations[k]);
        }
        String[] ligation_sites = replaceN(ligation_sites1);
        System.out.println("[Enzyme site number] " + ligation_sites.length);
        System.out.println(Arrays.toString(ligation_sites));

        BufferedReader reader;
        BufferedWriter writer;

        if (MyUtil.isGZipped(new File(fq))){
            reader  = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(fq))));
        }else{
            reader = new BufferedReader((
                    new FileReader(
                            new File(fq)
                    )
                    ));
        }

        writer = new BufferedWriter(new FileWriter(fqs));

        String line;
        String aline;
        long num=0;
        long numS=0;
        long numR=0;
        Read read = new Read();

        //读取fq
        while((line= reader.readLine())!=null){
            num ++;
            //读取一条read的四行后开始处理
            if (num!=1 && num%4==1) {
                numR ++;
                if (numR%1000000==0){
                    System.out.println();
                    System.out.println("INFO: "+numR+" reads splited...");
                }
                //初始化的分割的起始位置
                int start=0;
                //处理多种酶（待定）
                aline = (read.getName().split("\\s")[0].substring(1) + "\t" + start);
                writer.write(aline + "\n");
                for (String site :
                        ligation_sites) {
                    int resSite = site.indexOf("^");
                    site=site.replace("^","");
                    //模拟酶切并输出模拟酶切片段，直到无法找到新的酶切位点
                    while (read.getSeq().indexOf(site,start+site.length())!=-1) {
                        int preStart =start;
//                        start = read.getSeq().indexOf(site,preStart)+resSite;
                        start = read.getSeq().indexOf(site,preStart+site.length())+resSite;
                        if (start - preStart >= MIN_LEN) {
                            aline = (read.getName().split("\\s")[0].substring(1) + "\t" + start);
                            writer.write(aline + "\n");
                            numS++;
                        }
                    }
                    //输出最后一个酶切位点到序列结尾的片段
                    if (start < read.getSeq().length()) {
                        if(read.getSeq().length() - start >= MIN_LEN) {
                            aline = (read.getName().split("\\s")[0].substring(1) + "\t" + read.getSeq().length());
                            writer.write(aline + "\n");
                            numS++;
                        }
                    }
                }
            }
            //读取reads的四个信息
            switch ((int) (num%4)){
                case 1:
                    read.setName(line);
                    break;
                case 2:
                    read.setSeq(line);
                    break;
                case 0:
                    read.setQuality(line);
                    break;
                case 3:
                    read.setAdded(line);
                    break;
            }
        }

        //处理最后一个
        {
            numR ++;
            if (numR%1000000==0){
                System.out.println();
                System.out.println("INFO: "+numR+" reads splited...");
            }
            //初始化的分割的起始位置
            int start=0;
            //处理多种酶（待定）
            aline = (read.getName().split("\\s")[0].substring(1) + "\t" + start);
            writer.write(aline + "\n");
            for (String site :
                    ligation_sites) {
                int resSite = site.indexOf("^");
                site=site.replace("^","");
                //模拟酶切并输出模拟酶切片段，直到无法找到新的酶切位点
                while (read.getSeq().indexOf(site,start+site.length())!=-1) {
                    int preStart =start;
//                        start = read.getSeq().indexOf(site,preStart)+resSite;
                    start = read.getSeq().indexOf(site,preStart+site.length())+resSite;
                    if (start - preStart >= MIN_LEN) {
                        aline = (read.getName().split("\\s")[0].substring(1) + "\t" + start);
                        writer.write(aline + "\n");
                        numS++;
                    }
                }
                //输出最后一个酶切位点到序列结尾的片段
                if (start < read.getSeq().length()) {
                    if(read.getSeq().length() - start >= MIN_LEN) {
                        aline = (read.getName().split("\\s")[0].substring(1) + "\t" + read.getSeq().length());
                        writer.write(aline + "\n");
                        numS++;
                    }
                }
            }
        }

        System.out.println();
        System.out.println("num of reads: "+numR);
        System.out.println("num of splited reads: "+numS);

        reader.close();
        writer.close();
    }
}
