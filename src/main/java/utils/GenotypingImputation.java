package utils;


import org.apache.commons.cli.*;
import process.contact.ReadAT;

import java.io.*;
import java.util.*;

public class GenotypingImputation {
    public static void main(String[] args) throws IOException {
        String iF="";
        String oF="";
        int numLine=100000;
        int distCutoff = 500000000;
        double dominantCutoff = 0.8;
        int step = 3;

        try {
            // create Options object
            Options options = new Options();
            options.addOption(new Option("i", "contacts", true, "path to tagged contacts file (.contacts.withTags)"));
            options.addOption(new Option("o", "out", true, "path to outputfile (.contacts.withTags)"));
            options.addOption(new Option("step", "numSteps", true, "1, 2 or 3"));
            options.addOption(new Option("dist", "cutoffDist", true, "Cutoff in Imputation by mapping site"));
            options.addOption(new Option("ratio", "ratioDominant", true, "Ratio Cutoff in Imputation by dominant rule"));

            // create the command line parser
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            // check the options have been set correctly
            System.out.println("INFO: inputFile: "+cmd.getOptionValue("i"));
            System.out.println("INFO: outputFile: "+cmd.getOptionValue("o"));
            System.out.println("INFO: distCutoff: "+cmd.getOptionValue("dist"));
            System.out.println("INFO: ratioCutoff: "+cmd.getOptionValue("ratio"));
            if (!(cmd.hasOption("i") || cmd.hasOption("o"))) {
                // print usage
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "AntOptsCommonsCLI", options );
                System.out.println();
                System.exit(0);
            }
            iF = cmd.getOptionValue("i");
            oF = cmd.getOptionValue("o");
            if (cmd.hasOption("step")) {
                step = Integer.parseInt(cmd.getOptionValue("step"));
            }
            if (cmd.hasOption("dist")) {
                distCutoff = Integer.parseInt(cmd.getOptionValue("dist"));
            }
            if (cmd.hasOption("ratio")) {
                dominantCutoff = Double.parseDouble(cmd.getOptionValue("ratio"));
            }
        } catch (Exception ex) {
            System.out.println( "Unexpected exception:" + ex.getMessage() );
            System.exit(0);
        }

        //读取contacts
        BufferedReader atReader = new BufferedReader( new FileReader(iF));
        BufferedWriter oWriter1 = new BufferedWriter(new FileWriter(oF));

        //逐个记录读取并判断
        int recordIndex = 0;
        int count = 0;
        int numImputedDist = 0;
        int numImputedBridge = 0;
        int numImputedDomiant = 0;
        String line="";
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        Judgement judgement = new Judgement();
        while ((line = atReader.readLine())!=null) {
            count++;
            if (count % numLine == 0) {
                System.out.println(count + " frags was handled");
            }

            String[] fields = line.split("\t");

            ReadAT one = new ReadAT(fields);
            //判断是否为真实比对片段
            if (!judgement.judgeRealFrags(one)){
                continue;
            }

            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                //正式的处理
                //按染色体编号划分
                Map<String, List<ReadAT>> concatemerMapByChr = splitConcatemerByChr(concatemer);
                //按染色体逐个处理
                for (List<ReadAT> concatemerByChr:
                        concatemerMapByChr.values()) {
                    Map<String, List<ReadAT>> concatemerByPhase = splitConcatemerByPhase(concatemerByChr);
                    List<ReadAT> concatemerPhase0 = concatemerByPhase.getOrDefault("0", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhase1 = concatemerByPhase.getOrDefault("1", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhase2 = concatemerByPhase.getOrDefault("2", new ArrayList<ReadAT>());
                    List<ReadAT> concatemerPhaseConflict = new ArrayList<>();
                    if (step >=1 ){ // distance rule
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            int flag = 0;
                            for (ReadAT phase1AT :
                                    concatemerPhase1) {
                                if (getDistBetweenAT(unTaggedAT, phase1AT) < distCutoff){
                                    flag+=1;
                                    break;
                                }
                            }
                            for (ReadAT phase2AT :
                                    concatemerPhase2) {
                                if (getDistBetweenAT(unTaggedAT, phase2AT) < distCutoff){
                                    flag+=2;
                                    break;
                                }
                            }
                            if (flag == 0){

                            }else if (flag == 1){
                                unTaggedAT.setPhase("1");
                            }else if (flag == 2){
                                unTaggedAT.setPhase("2");
                            }else if (flag == 3){

                            }
                        }
                        // 重新划分concatemerPhase*
                        List<ReadAT> newConcatemerPhase0 = new ArrayList<>();
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                numImputedDist++;
                                if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase1.add(unTaggedAT);
                                } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase2.add(unTaggedAT);
                                }
                            }else {
                                newConcatemerPhase0.add(unTaggedAT);
                            }
                        }
                        concatemerPhase0 = newConcatemerPhase0;
                        if (step >= 2){ // bridge rule
                            for (ReadAT unTaggedAT :
                                    concatemerPhase0) {
                                int flag=0;
                                int index = concatemerByChr.indexOf(unTaggedAT);
                                int indexFirstLeft = getindexFirstLeft(concatemerByChr, index);
                                int indexFirstRight = getindexFirstRight(concatemerByChr, index);
                                if (indexFirstLeft==-1 || indexFirstRight==-1){
                                    flag=0;
                                } else if (concatemerByChr.get(indexFirstLeft).getPhase().equalsIgnoreCase(concatemerByChr.get(indexFirstRight).getPhase())){
                                    if (concatemerByChr.get(indexFirstLeft).getPhase().equals("1")) flag = 1;
                                    if (concatemerByChr.get(indexFirstLeft).getPhase().equals("2")) flag = 2;
                                } else flag=3;
                                if (flag == 0){

                                }else if (flag == 1){
                                    unTaggedAT.setPhase("1");
                                }else if (flag == 2){
                                    unTaggedAT.setPhase("2");
                                }else if (flag == 3){

                                }
                            }
                            // 重新划分concatemerPhase*
                            newConcatemerPhase0 = new ArrayList<>();
                            for (ReadAT unTaggedAT :
                                    concatemerPhase0) {
                                if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                    numImputedBridge++;
                                    if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase1.add(unTaggedAT);
                                    } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase2.add(unTaggedAT);
                                    }
                                }else {
                                    newConcatemerPhase0.add(unTaggedAT);
                                }
                            }
                            concatemerPhase0 = newConcatemerPhase0;
                            if (step >= 3){ // dominant rule
                                if (concatemerPhase1.size()==0 || concatemerPhase2.size()==0){
                                    if (concatemerPhase1.size() == 0){
                                        if (concatemerPhase2.size()/(double) concatemerByChr.size() >= dominantCutoff){
                                            for (ReadAT unTaggedAT :
                                                    concatemerPhase0) {
                                                unTaggedAT.setPhase(2);
                                            }
                                        }
                                    }
                                    if (concatemerPhase2.size() == 0){
                                        if (concatemerPhase1.size()/(double) concatemerByChr.size() >= dominantCutoff){
                                            for (ReadAT unTaggedAT :
                                                    concatemerPhase0) {
                                                unTaggedAT.setPhase(1);
                                            }
                                        }
                                    }
                                }
                                // 重新划分concatemerPhase*
                                newConcatemerPhase0 = new ArrayList<>();
                                for (ReadAT unTaggedAT :
                                        concatemerPhase0) {
                                    if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                        numImputedDomiant++;
                                        if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                            newConcatemerPhase0.remove(unTaggedAT);
                                            concatemerPhase1.add(unTaggedAT);
                                        } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                            newConcatemerPhase0.remove(unTaggedAT);
                                            concatemerPhase2.add(unTaggedAT);
                                        }
                                    }else {
                                        newConcatemerPhase0.add(unTaggedAT);
                                    }
                                }
                                concatemerPhase0 = newConcatemerPhase0;
                            }
                        }
                    }
                }
                for (ReadAT at :
                        concatemer) {
                    line = at.toContactLineWithPhase();
                    oWriter1.write(line);
                    oWriter1.newLine();
                }
                concatemer.clear();
                concatemer.add(one);
            }

        }

        {
            //正式的处理
            //按染色体编号划分
            Map<String, List<ReadAT>> concatemerMapByChr = splitConcatemerByChr(concatemer);
            //按染色体逐个处理
            for (List<ReadAT> concatemerByChr:
                    concatemerMapByChr.values()) {
                Map<String, List<ReadAT>> concatemerByPhase = splitConcatemerByPhase(concatemerByChr);
                List<ReadAT> concatemerPhase0 = concatemerByPhase.getOrDefault("0", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhase1 = concatemerByPhase.getOrDefault("1", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhase2 = concatemerByPhase.getOrDefault("2", new ArrayList<ReadAT>());
                List<ReadAT> concatemerPhaseConflict = new ArrayList<>();
                if (step >=1 ){ // distance rule
                    for (ReadAT unTaggedAT :
                            concatemerPhase0) {
                        int flag = 0;
                        for (ReadAT phase1AT :
                                concatemerPhase1) {
                            if (getDistBetweenAT(unTaggedAT, phase1AT) < distCutoff){
                                flag+=1;
                                break;
                            }
                        }
                        for (ReadAT phase2AT :
                                concatemerPhase2) {
                            if (getDistBetweenAT(unTaggedAT, phase2AT) < distCutoff){
                                flag+=2;
                                break;
                            }
                        }
                        if (flag == 0){

                        }else if (flag == 1){
                            unTaggedAT.setPhase("1");
                        }else if (flag == 2){
                            unTaggedAT.setPhase("2");
                        }else if (flag == 3){

                        }
                    }
                    // 重新划分concatemerPhase*
                    List<ReadAT> newConcatemerPhase0 = new ArrayList<>();
                    for (ReadAT unTaggedAT :
                            concatemerPhase0) {
                        if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                            numImputedDist++;
                            if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                newConcatemerPhase0.remove(unTaggedAT);
                                concatemerPhase1.add(unTaggedAT);
                            } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                newConcatemerPhase0.remove(unTaggedAT);
                                concatemerPhase2.add(unTaggedAT);
                            }
                        }else {
                            newConcatemerPhase0.add(unTaggedAT);
                        }
                    }
                    concatemerPhase0 = newConcatemerPhase0;
                    if (step >= 2){ // bridge rule
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            int flag=0;
                            int index = concatemerByChr.indexOf(unTaggedAT);
                            int indexFirstLeft = getindexFirstLeft(concatemerByChr, index);
                            int indexFirstRight = getindexFirstRight(concatemerByChr, index);
                            if (indexFirstLeft==-1 || indexFirstRight==-1){
                                flag=0;
                            } else if (concatemerByChr.get(indexFirstLeft).getPhase().equalsIgnoreCase(concatemerByChr.get(indexFirstRight).getPhase())){
                                if (concatemerByChr.get(indexFirstLeft).getPhase().equals("1")) flag = 1;
                                if (concatemerByChr.get(indexFirstLeft).getPhase().equals("2")) flag = 2;
                            } else flag=3;
                            if (flag == 0){

                            }else if (flag == 1){
                                unTaggedAT.setPhase("1");
                            }else if (flag == 2){
                                unTaggedAT.setPhase("2");
                            }else if (flag == 3){

                            }
                        }
                        // 重新划分concatemerPhase*
                        newConcatemerPhase0 = new ArrayList<>();
                        for (ReadAT unTaggedAT :
                                concatemerPhase0) {
                            if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                numImputedBridge++;
                                if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase1.add(unTaggedAT);
                                } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                    newConcatemerPhase0.remove(unTaggedAT);
                                    concatemerPhase2.add(unTaggedAT);
                                }
                            }else {
                                newConcatemerPhase0.add(unTaggedAT);
                            }
                        }
                        concatemerPhase0 = newConcatemerPhase0;
                        if (step >= 3){ // dominant rule
                            if (concatemerPhase1.size()==0 || concatemerPhase2.size()==0){
                                if (concatemerPhase1.size() == 0){
                                    if (concatemerPhase2.size()/(double) concatemerByChr.size() >= dominantCutoff){
                                        for (ReadAT unTaggedAT :
                                                concatemerPhase0) {
                                            unTaggedAT.setPhase(2);
                                        }
                                    }
                                }
                                if (concatemerPhase2.size() == 0){
                                    if (concatemerPhase1.size()/(double) concatemerByChr.size() >= dominantCutoff){
                                        for (ReadAT unTaggedAT :
                                                concatemerPhase0) {
                                            unTaggedAT.setPhase(1);
                                        }
                                    }
                                }
                            }
                            // 重新划分concatemerPhase*
                            newConcatemerPhase0 = new ArrayList<>();
                            for (ReadAT unTaggedAT :
                                    concatemerPhase0) {
                                if (!unTaggedAT.getPhase().equalsIgnoreCase("0")){
                                    numImputedDomiant++;
                                    if (unTaggedAT.getPhase().equalsIgnoreCase("1")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase1.add(unTaggedAT);
                                    } else if (unTaggedAT.getPhase().equalsIgnoreCase("2")){
                                        newConcatemerPhase0.remove(unTaggedAT);
                                        concatemerPhase2.add(unTaggedAT);
                                    }
                                }else {
                                    newConcatemerPhase0.add(unTaggedAT);
                                }
                            }
                            concatemerPhase0 = newConcatemerPhase0;
                        }
                    }
                }
            }
            for (ReadAT at :
                    concatemer) {
                line = at.toContactLineWithPhase();
                oWriter1.write(line);
                oWriter1.newLine();
            }
            concatemer.clear();
        }

        System.out.println("numImputedDist = " + numImputedDist);
        System.out.println("numImputedBridge = " + numImputedBridge);
        System.out.println("numImputedDomiant = " + numImputedDomiant);

        oWriter1.close();
        atReader.close();
    }

    public static int getindexFirstLeft(List<ReadAT> concatemer, int index){
        int indexFirstLeft = index-1;
        while (indexFirstLeft >= 0){
            if (concatemer.get(indexFirstLeft).isPhased()) return indexFirstLeft;
            indexFirstLeft--;
        }
        return -1;
    }

    public static int getindexFirstRight(List<ReadAT> concatemer, int index){
        int indexFirstRight = index+1;
        while (indexFirstRight <= concatemer.size()-1){
            if (concatemer.get(indexFirstRight).isPhased()) return indexFirstRight;
            indexFirstRight++;
        }
        return -1;
    }

    public static int getDistBetweenAT(ReadAT at1, ReadAT at2){
        return (int) Math.abs(at1.getMid()-at2.getMid());
    }

    public static Map<String, List<ReadAT>> splitConcatemerByChr(List<ReadAT> concatemer){
        HashSet<String> chrSet  = new HashSet<>();
        HashMap<String, List<ReadAT>> concatemerMap = new HashMap<>();
        for (ReadAT one :
                concatemer) {
            chrSet.add(one.getChr());
        }
        for (String chrName :
                chrSet) {
            concatemerMap.put(chrName, new ArrayList<ReadAT>());
        }
        for (ReadAT one :
                concatemer) {
            concatemerMap.get(one.getChr()).add(one);
        }
        return concatemerMap;
    }

    public static Map<String, List<ReadAT>> splitConcatemerByPhase(List<ReadAT> concatemer){
        HashSet<String> phaseSet  = new HashSet<>();
        HashMap<String, List<ReadAT>> concatemerMap = new HashMap<>();
        for (ReadAT one :
                concatemer) {
            phaseSet.add(one.getPhase());
        }
        for (String phaseName :
                phaseSet) {
            concatemerMap.put(phaseName, new ArrayList<ReadAT>());
        }
        for (ReadAT one :
                concatemer) {
            concatemerMap.get(one.getPhase()).add(one);
        }
        return concatemerMap;
    }

}
