package process.contact;

import process.Path;
import utils.Judgement;
import utils.N50Calculator;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AlignTable2Contact {
    public AlignTable2Contact(String dataType, String inputFile, String outPrefix, int cutoff){

    }

    //res-split模式
    public AlignTable2Contact(String inputFile, String outPrefix, Path p) throws IOException {
        //读取mapq阈值
        int cutoff = Integer.parseInt(p.cutoffMapq);

        //输出流创建
        BufferedReader atReader = new BufferedReader( new FileReader(inputFile));
        new File(outPrefix + ".penalty").delete();
        BufferedWriter pWriter = new BufferedWriter( new FileWriter(outPrefix+".penalty"));
        new File(outPrefix + ".filter.stats").delete();
        BufferedWriter fsWriter = new BufferedWriter( new FileWriter(outPrefix+".filter.stats"));
        new File(outPrefix + ".monomers").delete();
        BufferedWriter mmWriter = new BufferedWriter( new FileWriter(outPrefix+".monomers"));
        new File(outPrefix + ".singleton").delete();
        BufferedWriter singletonWriter = new BufferedWriter( new FileWriter(outPrefix+".singleton"));
        new File(outPrefix + ".ssingleton").delete();
        BufferedWriter ssingletonWriter = new BufferedWriter( new FileWriter(outPrefix+".ssingleton"));
        new File(outPrefix + ".lowQuality").delete();
        BufferedWriter lowQualityWriter = new BufferedWriter( new FileWriter(outPrefix+".lowQuality"));
        BufferedWriter resRemoveWriter = null;
        if (p.ligation_type.equals("res")){
            new File(outPrefix + ".resRemove").delete();
            resRemoveWriter = new BufferedWriter( new FileWriter(outPrefix+".resRemove"));
        }
        new File(outPrefix + ".disRemove").delete();
        BufferedWriter disRemoveWriter = new BufferedWriter( new FileWriter(outPrefix+".disRemove"));
        BufferedWriter shortRemoveWriter = null;
//        if (p.splitReads.equals("N")) {
            new File(outPrefix + ".notOnShortestPath").delete();
            shortRemoveWriter = new BufferedWriter(new FileWriter(outPrefix + ".notOnShortestPath"));
//        }
        new File(outPrefix + ".contacts").delete();
        BufferedWriter contactsWriter = new BufferedWriter( new FileWriter(outPrefix+".contacts"));

        String line;
        List<ReadAT> concatemer=new ArrayList<ReadAT>();
        List<ReadAT> concatemer2=new ArrayList<ReadAT>();
        //用于处理overlap的比对
        GraphConcatemer graphConcatemer;
        //用于处理位于同一酶切位点的匹配
        FragmentPurifying fragmentPurifying=null;
        if (p.ligation_type.equals("res")) {
            fragmentPurifying = new FragmentPurifying(p.restrictionsiteFile, p.minfragsize, p.maxfragsize);
        }
        DistancePurifying distancePurifying = new DistancePurifying();
        OverlapPurifying overlapPurifying = new OverlapPurifying();
        ComparatorReadATBySE comparatorReadAT = new ComparatorReadATBySE();

        //开始逐一处理比对结果
        int count=0;
        int countRes=0;
        int countDis=0;
        int countOverlap=0;
        int countSingle1=0;
        int countSingle2=0;
        int countLow=0;
        int countValid=0;
        while ((line = atReader.readLine())!=null){
            count++;
            if (count % 1000000 == 0) {
                System.out.println(count + " mapping was handled");
            }

            String[] fields = line.split("\t");

            ReadAT one = new ReadAT(fields);

            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0){
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)){
                concatemer.add(one);
            } else {
                //开始处理
                concatemer2.addAll(concatemer);
                //过滤低分比对
                List<ReadAT> concatemerLowQ = new ArrayList<>();
                for (ReadAT monomer :
                        concatemer) {
                    if (monomer.mapq < cutoff){
                        monomer.setStatus("low_quality");
                        concatemerLowQ.add(monomer);
                        lowQualityWriter.write(monomer.toContactLine());
                        lowQualityWriter.newLine();
                        countLow++;
                    }
                }
                //去除
                concatemer.removeAll(concatemerLowQ);

                //过滤低分比对之后数量不为0
                //原始singleton
                if (concatemer.size() == 1){
                    for (ReadAT monomer :
                            concatemer) {
                        monomer.setStatus("singleton1");
                        singletonWriter.write(monomer.toContactLine());
                        singletonWriter.newLine();
                        countSingle1++;
                    }
                }else if (concatemer.size() > 1) {
                    //过滤掉重合比对
                    if (concatemer.size() > 1) {
//                        if (p.splitReads.equals("N")) {
                        if (true) {
                            //如果提前不分割reads，则使用图算法过滤掉重叠比对的部分
                            graphConcatemer = new GraphConcatemer(concatemer);
                            graphConcatemer.IdentifyMonomersFromConcatemer(Double.parseDouble(p.weigthtAS));
                            //将被删除的比对写入
                            concatemer.removeAll(graphConcatemer.monomers);
                            if (!concatemer.isEmpty()) {
                                for (ReadAT monomer :
                                        concatemer) {
                                    monomer.setStatus("region_multi-mappings");
                                    shortRemoveWriter.write(monomer.toContactLine());
                                    shortRemoveWriter.newLine();
                                    countOverlap++;
                                }
                            }
                            pWriter.write(graphConcatemer.penalty+"\t"+graphConcatemer.monomers.get(0).readLen+"");
                            pWriter.newLine();
                            concatemer = graphConcatemer.monomers;
                        }
//                        else {
//                            //如果分割reads，则使用overlap算法过滤掉重叠比对的部分
//                            List<ReadAT> concatemerOverlap = overlapPurifying.purify(concatemer, Integer.parseInt(p.weigthtAS));
//                            for (ReadAT monomer :
//                                    concatemerOverlap) {
//                                monomer.setStatus("region_multi-mappings");
//                                shortRemoveWriter.write(monomer.toContactLine());
//                                shortRemoveWriter.newLine();
//                                countOverlap++;
//                            }
//                            overlapPurifying.clear();
//                        }
                    }

                    //通过酶切位点过滤
                    if (concatemer.size() > 1 && p.removeResblock.equalsIgnoreCase("Y")) {
                        if (p.ligation_type.equals("res")) {
                            //filter by res sites
                            //临时存储被去除的匹配
                            List<ReadAT> concatemerResRemove = fragmentPurifying.purify(concatemer);
                            for (ReadAT monomer :
                                    concatemerResRemove) {
                                monomer.setStatus("self-interaction");
                                resRemoveWriter.write(monomer.toContactLine());
                                resRemoveWriter.newLine();
                                countRes++;
                            }
                            fragmentPurifying.clear();
                        }
                    }

                    //通过距离阈值过滤
                    if (concatemer.size() > 1 && p.removeDis.equalsIgnoreCase("Y")) {
                        List<ReadAT> concatemerDisRemove = distancePurifying.purify(concatemer, p.distanceCutoff);
                        for (ReadAT monomer :
                                concatemerDisRemove) {
                            monomer.setStatus("close(<" + p.distanceCutoff + ")");
                            disRemoveWriter.write(monomer.toContactLine());
                            disRemoveWriter.newLine();
                            countDis++;
                        }
                        distancePurifying.clean();
                    }

                    //写入最终过滤的结果
                    concatemer.sort(comparatorReadAT);
                    if (concatemer.size() > 1) {
                        for (ReadAT monomer :
                                concatemer) {
                            monomer.setStatus("passed");
                            mmWriter.write(monomer.toContactLine());
                            mmWriter.newLine();
                            countValid++;
                        }
                    } else if (concatemer.size() > 0) {
                        //singleton2
                        for (ReadAT monomer :
                                concatemer) {
                            monomer.setStatus("singleton2");
                            ssingletonWriter.write(monomer.toContactLine());
                            ssingletonWriter.newLine();
                            countSingle2++;
                        }
                    }


                }

                concatemer2.sort(comparatorReadAT);
                for (ReadAT monomer :
                        concatemer2) {
                    contactsWriter.write(monomer.toContactLine());
                    contactsWriter.newLine();
                }

                concatemer.clear();
                concatemer2.clear();
                concatemer.add(one);
            }

        }


        //处理最后一个
        {
            //开始处理
            concatemer2.addAll(concatemer);
            //过滤低分比对
            List<ReadAT> concatemerLowQ = new ArrayList<>();
            for (ReadAT monomer :
                    concatemer) {
                if (monomer.mapq < cutoff){
                    monomer.setStatus("low_quality");
                    concatemerLowQ.add(monomer);
                    lowQualityWriter.write(monomer.toContactLine());
                    lowQualityWriter.newLine();
                    countLow++;
                }
            }
            //去除
            concatemer.removeAll(concatemerLowQ);

            //过滤低分比对之后数量不为0
            //原始singleton
            if (concatemer.size() == 1){
                for (ReadAT monomer :
                        concatemer) {
                    monomer.setStatus("singleton1");
                    singletonWriter.write(monomer.toContactLine());
                    singletonWriter.newLine();
                    countSingle1++;
                }
            }else if (concatemer.size() > 1) {
                //过滤掉重合比对
                if (concatemer.size() > 1) {
//                        if (p.splitReads.equals("N")) {
                    if (true) {
                        //如果提前不分割reads，则使用图算法过滤掉重叠比对的部分
                        graphConcatemer = new GraphConcatemer(concatemer);
                        graphConcatemer.IdentifyMonomersFromConcatemer(Double.parseDouble(p.weigthtAS));
                        //将被删除的比对写入
                        concatemer.removeAll(graphConcatemer.monomers);
                        if (!concatemer.isEmpty()) {
                            for (ReadAT monomer :
                                    concatemer) {
                                monomer.setStatus("region_multi-mappings");
                                shortRemoveWriter.write(monomer.toContactLine());
                                shortRemoveWriter.newLine();
                                countOverlap++;
                            }
                        }
                        concatemer = graphConcatemer.monomers;
                    }
//                        else {
//                            //如果分割reads，则使用overlap算法过滤掉重叠比对的部分
//                            List<ReadAT> concatemerOverlap = overlapPurifying.purify(concatemer, Integer.parseInt(p.weigthtAS));
//                            for (ReadAT monomer :
//                                    concatemerOverlap) {
//                                monomer.setStatus("region_multi-mappings");
//                                shortRemoveWriter.write(monomer.toContactLine());
//                                shortRemoveWriter.newLine();
//                                countOverlap++;
//                            }
//                            overlapPurifying.clear();
//                        }
                }

                //通过酶切位点过滤
                if (concatemer.size() > 1 && p.removeResblock.equalsIgnoreCase("Y")) {
                    if (p.ligation_type.equals("res")) {
                        //filter by res sites
                        //临时存储被去除的匹配
                        List<ReadAT> concatemerResRemove = fragmentPurifying.purify(concatemer);
                        for (ReadAT monomer :
                                concatemerResRemove) {
                            monomer.setStatus("self-interaction");
                            resRemoveWriter.write(monomer.toContactLine());
                            resRemoveWriter.newLine();
                            countRes++;
                        }
                        fragmentPurifying.clear();
                    }
                }

                //通过距离阈值过滤
                if (concatemer.size() > 1 && p.removeDis.equalsIgnoreCase("Y")) {
                    List<ReadAT> concatemerDisRemove = distancePurifying.purify(concatemer, p.distanceCutoff);
                    for (ReadAT monomer :
                            concatemerDisRemove) {
                        monomer.setStatus("close(<" + p.distanceCutoff + ")");
                        disRemoveWriter.write(monomer.toContactLine());
                        disRemoveWriter.newLine();
                        countDis++;
                    }
                    distancePurifying.clean();
                }

                //写入最终过滤的结果
                concatemer.sort(comparatorReadAT);
                if (concatemer.size() > 1) {
                    for (ReadAT monomer :
                            concatemer) {
                        monomer.setStatus("passed");
                        mmWriter.write(monomer.toContactLine());
                        mmWriter.newLine();
                        countValid++;
                    }
                } else if (concatemer.size() > 0) {
                    //singleton2
                    for (ReadAT monomer :
                            concatemer) {
                        monomer.setStatus("singleton2");
                        ssingletonWriter.write(monomer.toContactLine());
                        ssingletonWriter.newLine();
                        countSingle2++;
                    }
                }


            }

            concatemer2.sort(comparatorReadAT);
            for (ReadAT monomer :
                    concatemer2) {
                contactsWriter.write(monomer.toContactLine());
                contactsWriter.newLine();
            }

            concatemer.clear();
            concatemer2.clear();
//            concatemer.add(one);
        }

        //流释放
        atReader.close();
        mmWriter.close();
        lowQualityWriter.close();
        if (p.ligation_type.equals("res")) {
            resRemoveWriter.close();
        }
        pWriter.close();
        disRemoveWriter.close();
        singletonWriter.close();
        ssingletonWriter.close();
        shortRemoveWriter.close();
        contactsWriter.close();

        //打印统计结果
        System.out.println();
        line=("count of all mappings= " + count) + "\n" +
                ("count of all valid mappings = " + countValid) + "\n" +
                ("count of low mapq(<"+cutoff+") mappings = " + countLow) + "\n" +
                ("count of multi mappings at same reads position = " + countOverlap) + "\n" +
                ("count of single mappings(original) = " + countSingle1) + "\n" +
                ("count of single mappings(generated by filtering) = " + countSingle2) + "\n" +
                ("count of all self-ligation mappings = " + (countRes+countDis)) + "\n" +
                ("count of self-interaction mappings(dis<"+p.distanceCutoff+") = " + countDis) + "\n"
        ;
        if (p.ligation_type.equals("res")) {
            line+=("count of self-interaction mappings(by restriction fragment) = " + countRes) + "\n";
        }
        System.out.println(line);
        fsWriter.write(line);
        fsWriter.close();
        System.out.println();


    }

    public static void select(String inputFile, String outPrefix, Path p) throws IOException {
        //读取mapq阈值
        int cutoff = Integer.parseInt(p.cutoffMapq);

        //流创建
        BufferedReader atReader = new BufferedReader( new FileReader(inputFile));
        new File(outPrefix + ".basic_statistics.txt").delete();
        BufferedWriter bsWriter = new BufferedWriter( new FileWriter(outPrefix+".basic_statistics.txt"));
        new File(outPrefix + ".filter.stats").delete();
        BufferedWriter fsWriter = new BufferedWriter( new FileWriter(outPrefix+".filter.stats"));
        new File(outPrefix + ".contacts").delete();
        BufferedWriter contactsWriter = new BufferedWriter( new FileWriter(outPrefix+".contacts"));
        new File(outPrefix + ".penalty.distribution").delete();
        BufferedWriter pWriter = new BufferedWriter( new FileWriter(outPrefix+".penalty.distribution"));

        String line;
        List<ReadAT> concatemerTmp =new ArrayList<ReadAT>();
        List<ReadAT> concatemerAll =new ArrayList<ReadAT>();
        //用于处理overlap的比对
        GraphConcatemer graphConcatemer;
        //用于处理位于同一酶切位点的匹配
        FragmentPurifying fragmentPurifying=null;
        if (p.ligation_type.equals("res")) {
            fragmentPurifying = new FragmentPurifying(p.restrictionsiteFile, p.minfragsize, p.maxfragsize);
        }

        DistancePurifying distancePurifying = new DistancePurifying();
        IsolatedPurifying isolatedPurifying = new IsolatedPurifying();
        OverlapPurifying overlapPurifying = new OverlapPurifying();
        ResSitePurifying resSitePurifying = new ResSitePurifying(p);
        ComparatorReadATBySE comparatorReadAT = new ComparatorReadATBySE();
        RefineBoundary refineBoundary = new RefineBoundary(p);
        Judgement judgement = new Judgement();

        //开始逐一处理比对结果
        // qiangwei Stats
        List<Integer> readLensList = new ArrayList<>();
        List<Integer> fragLensList = new ArrayList<>();
        int countReads=0;
        long avgLenReads=0;
        long N50LenReads=0;
        int countReadsWithAtLeastOneMapping=0;
        int countFragments=0;
        double countFragmentsPerRead=0;
        int avgLenFrags=0;
        int N50LenFrags=0;


        int count=0;
        int countRes=0;
        int countFrag =0;
        int countDis=0;
        int countIso=0;
        int countOverlap=0;
        int countSingle1=0;
        int countSingle2=0;
        int countLow=0;
        int countUnmapped=0;
        int countValid=0;
        while ((line = atReader.readLine())!=null){
            count++;
            if (count % 1000000 == 0) {
                System.out.println(count + " mappings were handled");
            }

            String[] fields = line.split("\t");

            ReadAT one = new ReadAT(fields);

            //将同一个concatemer的比对在一个批次进行处理
            if (concatemerTmp.size() == 0){
                concatemerTmp.add(one);
            } else if (one.readName.equals(concatemerTmp.get(0).readName)){
                concatemerTmp.add(one);
            } else {
                //开始处理
                concatemerAll.addAll(concatemerTmp);


                //过滤unmapped
                List<ReadAT> concatemerUnmapped = new ArrayList<>();
                for (ReadAT monomer :
                        concatemerTmp) {
                    if (monomer.getStatus().equalsIgnoreCase("unmapped")){
                        concatemerUnmapped.add(monomer);
                        countUnmapped++;
                    }
                }
                //去除
                concatemerTmp.removeAll(concatemerUnmapped);


                //过滤低分比对
                List<ReadAT> concatemerLowQ = new ArrayList<>();
                for (ReadAT monomer :
                        concatemerTmp) {
                    if (monomer.mapq < cutoff){
                        monomer.setStatus("low_quality");
                        concatemerLowQ.add(monomer);
                        countLow++;
                    }
                }
                //去除
                concatemerTmp.removeAll(concatemerLowQ);


                //过滤低分比对之后数量不为0
                //原始singleton
                if (concatemerTmp.size() == 1){
                    for (ReadAT monomer :
                            concatemerTmp) {
                        monomer.setStatus("singleton1");
                        countSingle1++;
                    }
                }else if (concatemerTmp.size() > 1) {
                    //选择最优的alignment组合
                    if (concatemerTmp.size() > 1
//                            && p.filter.equalsIgnoreCase("graph")
                    ) {
                        //如果提前不分割reads，则使用图算法过滤掉重叠比对的部分
                        graphConcatemer = new GraphConcatemer(concatemerTmp);
                        graphConcatemer.IdentifyMonomersFromConcatemer(Double.parseDouble(p.weigthtAS));

                        try {
                            pWriter.write(graphConcatemer.oPenalty+"\t"+graphConcatemer.gPenalty+"\t"+graphConcatemer.aPenalty+"\t"+graphConcatemer.penalty+"\t"+graphConcatemer.monomers.get(0).readLen+"");
                            pWriter.newLine();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(concatemerAll.get(0).getReadName());
                            for (ReadAT monomer :
                                    concatemerAll) {
                                System.out.println(monomer.toContactLine());
                            }
                        }
                        List<ReadAT> concatemerOverlap = new ArrayList<>();
                        concatemerOverlap.addAll(concatemerTmp);
                        concatemerOverlap.removeAll(graphConcatemer.monomers);
                        //将被删除的比对写入
                        if (!concatemerOverlap.isEmpty()) {
                            for (ReadAT monomer :
                                    concatemerOverlap) {
                                monomer.setStatus("region_multi-mappings");
                                countOverlap++;
                            }
                        }
                        concatemerTmp.removeAll(concatemerOverlap);

                    }

                    //使用酶切位点过滤（边缘置信判断）
                    if (concatemerTmp.size() > 0
                            && p.ligation_type.equals("res")
                            && p.filter.equalsIgnoreCase("res")
                    ) {
//                        List<ReadAT> concatemerResRemove = resSitePurifying.purify(concatemerTmp);
                        List<ReadAT> concatemerResRemove = resSitePurifying.purifyStrict(concatemerTmp);
                        for (ReadAT monomer :
                                concatemerResRemove) {
                            monomer.setStatus("not_confident");
                            countRes++;
                        }
                        concatemerTmp.removeAll(concatemerResRemove);
                        resSitePurifying.clear();

                    }

                    //通过酶切片段过滤(临近交互)
                    if (concatemerTmp.size() > 1 && p.removeResblock.equalsIgnoreCase("Y")) {
                        if (p.ligation_type.equals("res")) {
                            //filter by res sites
                            //临时存储被去除的匹配
                            List<ReadAT> concatemerFragRemove = fragmentPurifying.purify(concatemerTmp);
                            for (ReadAT monomer :
                                    concatemerFragRemove) {
                                monomer.setStatus("adjacent_contacts");
                                countFrag++;
                            }
                            fragmentPurifying.clear();
                            concatemerTmp.removeAll(concatemerFragRemove);

                        }
                    }

                    //通过距离阈值过滤
                    if (concatemerTmp.size() > 1 && p.removeDis.equalsIgnoreCase("Y")) {
                        List<ReadAT> concatemerDisRemove = distancePurifying.purify(concatemerTmp, p.distanceCutoff);
                        for (ReadAT monomer :
                                concatemerDisRemove) {
//                            monomer.setStatus("close contacts(<" + p.distanceCutoff + ")");
                            monomer.setStatus("close_contacts");
                            countDis++;
                        }
                        concatemerTmp.removeAll(concatemerDisRemove);
                        distancePurifying.clean();

                    }

                    //过滤孤立的交互
                    if (concatemerTmp.size() > 1 && p.removeIsolated.equalsIgnoreCase("Y")) {
                        List<ReadAT> concatemerDisRemove = isolatedPurifying.purify(concatemerTmp, p.distanceCutoff2, p.MinfragNum, p.MinDomRatio);
                        for (ReadAT monomer :
                                concatemerDisRemove) {
//                            monomer.setStatus("isolated contacts(>" + p.distanceCutoff2 + ")");
                            monomer.setStatus("isolated_contacts");
                            countIso++;
                        }
                        concatemerTmp.removeAll(concatemerDisRemove);
                        isolatedPurifying.clean();

                    }

                    if (concatemerTmp.size() > 1) {
                        for (ReadAT monomer :
                                concatemerTmp) {
                            monomer.setStatus("passed");
                            countValid++;
                        }
                    }
                    else if (concatemerTmp.size() == 0) {
                        //singleton2
                        for (ReadAT monomer :
                                concatemerTmp) {
                            monomer.setStatus("singleton2");
                            countSingle2++;
                        }
                    }
                }

                //精修边缘
//                refineBoundary.refine(concatemerAll);

                //写入最终过滤的结果
                concatemerAll.sort(comparatorReadAT);
                for (ReadAT monomer :
                        concatemerAll) {
                    contactsWriter.write(monomer.toContactLine());
                    contactsWriter.newLine();
                    //qiangwei
                    if (judgement.judgeRealFrags(monomer)){
                        fragLensList.add(monomer.getMappingLen());
                    }
                }
                //qiangwei
                readLensList.add(concatemerAll.get(0).getReadLen());

                concatemerTmp.clear();
                concatemerAll.clear();
                concatemerTmp.add(one);
            }
        }


        //处理最后一个
        {
            //开始处理
            concatemerAll.addAll(concatemerTmp);


            //过滤unmapped
            List<ReadAT> concatemerUnmapped = new ArrayList<>();
            for (ReadAT monomer :
                    concatemerTmp) {
                if (monomer.getStatus().equalsIgnoreCase("unmapped")){
                    concatemerUnmapped.add(monomer);
                    countUnmapped++;
                }
            }
            //去除
            concatemerTmp.removeAll(concatemerUnmapped);


            //过滤低分比对
            List<ReadAT> concatemerLowQ = new ArrayList<>();
            for (ReadAT monomer :
                    concatemerTmp) {
                if (monomer.mapq < cutoff){
                    monomer.setStatus("low_quality");
                    concatemerLowQ.add(monomer);
                    countLow++;
                }
            }
            //去除
            concatemerTmp.removeAll(concatemerLowQ);


            //过滤低分比对之后数量不为0
            //原始singleton
            if (concatemerTmp.size() == 1){
                for (ReadAT monomer :
                        concatemerTmp) {
                    monomer.setStatus("singleton1");
                    countSingle1++;
                }
            }else if (concatemerTmp.size() > 1) {
                //选择最优的alignment组合
                if (concatemerTmp.size() > 1
//                            && p.filter.equalsIgnoreCase("graph")
                ) {
                    //如果提前不分割reads，则使用图算法过滤掉重叠比对的部分
                    graphConcatemer = new GraphConcatemer(concatemerTmp);
                    graphConcatemer.IdentifyMonomersFromConcatemer(Double.parseDouble(p.weigthtAS));

                    try {
                        pWriter.write(graphConcatemer.oPenalty+"\t"+graphConcatemer.gPenalty+"\t"+graphConcatemer.aPenalty+"\t"+graphConcatemer.penalty+"\t"+graphConcatemer.monomers.get(0).readLen+"");
                        pWriter.newLine();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(concatemerAll.get(0).getReadName());
                        for (ReadAT monomer :
                                concatemerAll) {
                            System.out.println(monomer.toContactLine());
                        }
                    }
                    List<ReadAT> concatemerOverlap = new ArrayList<>();
                    concatemerOverlap.addAll(concatemerTmp);
                    concatemerOverlap.removeAll(graphConcatemer.monomers);
                    //将被删除的比对写入
                    if (!concatemerOverlap.isEmpty()) {
                        for (ReadAT monomer :
                                concatemerOverlap) {
                            monomer.setStatus("region_multi-mappings");
                            countOverlap++;
                        }
                    }
                    concatemerTmp.removeAll(concatemerOverlap);

                }

                //使用酶切位点过滤（边缘置信判断）
                if (concatemerTmp.size() > 0
                        && p.ligation_type.equals("res")
                        && p.filter.equalsIgnoreCase("res")
                ) {
//                        List<ReadAT> concatemerResRemove = resSitePurifying.purify(concatemerTmp);
                    List<ReadAT> concatemerResRemove = resSitePurifying.purifyStrict(concatemerTmp);
                    for (ReadAT monomer :
                            concatemerResRemove) {
                        monomer.setStatus("not_confident");
                        countRes++;
                    }
                    concatemerTmp.removeAll(concatemerResRemove);
                    resSitePurifying.clear();

                }

                //通过酶切片段过滤(临近交互)
                if (concatemerTmp.size() > 1 && p.removeResblock.equalsIgnoreCase("Y")) {
                    if (p.ligation_type.equals("res")) {
                        //filter by res sites
                        //临时存储被去除的匹配
                        List<ReadAT> concatemerFragRemove = fragmentPurifying.purify(concatemerTmp);
                        for (ReadAT monomer :
                                concatemerFragRemove) {
                            monomer.setStatus("adjacent_contacts");
                            countFrag++;
                        }
                        fragmentPurifying.clear();
                        concatemerTmp.removeAll(concatemerFragRemove);

                    }
                }

                //通过距离阈值过滤
                if (concatemerTmp.size() > 1 && p.removeDis.equalsIgnoreCase("Y")) {
                    List<ReadAT> concatemerDisRemove = distancePurifying.purify(concatemerTmp, p.distanceCutoff);
                    for (ReadAT monomer :
                            concatemerDisRemove) {
//                            monomer.setStatus("close contacts(<" + p.distanceCutoff + ")");
                        monomer.setStatus("close_contacts");
                        countDis++;
                    }
                    concatemerTmp.removeAll(concatemerDisRemove);
                    distancePurifying.clean();

                }

                //过滤孤立的交互
                if (concatemerTmp.size() > 1 && p.removeIsolated.equalsIgnoreCase("Y")) {
                    List<ReadAT> concatemerDisRemove = isolatedPurifying.purify(concatemerTmp, p.distanceCutoff2, p.MinfragNum, p.MinDomRatio);
                    for (ReadAT monomer :
                            concatemerDisRemove) {
//                            monomer.setStatus("isolated contacts(>" + p.distanceCutoff2 + ")");
                        monomer.setStatus("isolated_contacts");
                        countIso++;
                    }
                    concatemerTmp.removeAll(concatemerDisRemove);
                    isolatedPurifying.clean();

                }

                if (concatemerTmp.size() > 1) {
                    for (ReadAT monomer :
                            concatemerTmp) {
                        monomer.setStatus("passed");
                        countValid++;
                    }
                }
                else if (concatemerTmp.size() == 0) {
                    //singleton2
                    for (ReadAT monomer :
                            concatemerTmp) {
                        monomer.setStatus("singleton2");
                        countSingle2++;
                    }
                }
            }

            //精修边缘
//                refineBoundary.refine(concatemerAll);

            //写入最终过滤的结果
            concatemerAll.sort(comparatorReadAT);
            for (ReadAT monomer :
                    concatemerAll) {
                contactsWriter.write(monomer.toContactLine());
                contactsWriter.newLine();
                //qiangwei
                if (judgement.judgeRealFrags(monomer)){
                    fragLensList.add(monomer.getMappingLen());
                }
            }
            //qiangwei
            readLensList.add(concatemerAll.get(0).getReadLen());

            concatemerTmp.clear();
            concatemerAll.clear();
        }

        //流释放
        atReader.close();
        contactsWriter.close();
        pWriter.close();
        //打印统计结果
        System.out.println();

        //开始逐一处理比对结果
        countReads=readLensList.size();
        avgLenReads=calculateSum(readLensList)/countReads;
        N50LenReads= N50Calculator.calculateN50(readLensList);
        countReadsWithAtLeastOneMapping=countReads-countUnmapped;
        countFragments=fragLensList.size();
        countFragmentsPerRead= (double) countFragments/(double) countReadsWithAtLeastOneMapping;
        avgLenFrags=(int) (calculateSum(fragLensList)/countFragments);
        N50LenFrags= N50Calculator.calculateN50(fragLensList);

        line="Basic Stats:\n" +
                "Reads count\t" +countReads+"\n"+
                "Average length of reads\t" +avgLenReads+"\n"+
                "N50 length of reads\t" +N50LenReads+"\n"+
                "Mapped Reads count\t" +countReadsWithAtLeastOneMapping+"\n"+
                "Unmapped Reads count\t" +countUnmapped+"\n"+
                "Fragments count\t" +countFragments+"\n"+
                "Average fragments count by read\t" +countFragmentsPerRead+"\n"+
                "Average length of fragments\t" +avgLenFrags+"\n"+
                "N50 length of fragments\t"+N50LenFrags;
        System.out.println(line);
        bsWriter.write(line);
        bsWriter.close();


        if (p.ligation_type.equals("res")) {
            line=("count of all records= " + count) + "\n" +
                    ("count of all valid mappings = " + countValid + "\t("+100/((double)count/countValid)+"%)") + "\n" +
                    ("count of unmapped reads = " + countUnmapped + "\t("+100/((double)count/countUnmapped)+"%)") + "\n" +
                    ("count of low mapq(<"+cutoff+") mappings = " + countLow+ "\t("+100/((double)count/countLow)+"%)") + "\n" +
                    ("count of not confident boundary mappings = " + countRes+ "\t("+100/((double)count/countRes)+"%)") + "\n" +
                    ("count of multi mappings at same reads position = " + countOverlap+ "\t("+100/((double)count/countOverlap)+"%)") + "\n" +
                    ("count of single mappings (original) = " + countSingle1+ "\t("+100/((double)count/countSingle1)+"%)") + "\n" +
                    ("count of single mappings (after filtering) = " + countSingle2+ "\t("+100/((double)count/countSingle2)+"%)") + "\n" +
                    ("count of all self-ligation mappings (close + adjacent) = " + (countFrag +countDis)+ "\t("+100/((double)count/(countFrag +countDis))+"%)") + "\n" +
                    ("count of close contacts (dis<"+p.distanceCutoff+") = " + countDis+ "\t("+100/((double)count/countDis)+"%)") + "\n" +
                    ("count of adjacent contacts (by restriction fragment) = " + countFrag+ "\t("+100/((double)count/countFrag)+"%)") + "\n" +
                    ("count of isolated contacts (dis>"+p.distanceCutoff2+") = " + countIso+ "\t("+100/((double)count/countIso)+"%)") + "\n";
        } else {
            line=("count of all mappings= " + count) + "\n" +
                    ("count of all valid mappings = " + countValid) + "\n" +
                    ("count of low mapq(<"+cutoff+") mappings = " + countLow) + "\n" +
                    ("count of multi mappings at same reads position = " + countOverlap) + "\n" +
                    ("count of single mappings(original) = " + countSingle1) + "\n" +
                    ("count of single mappings(generated after filtering) = " + countSingle2) + "\n" +
                    ("count of all self-ligation mappings = " + (countFrag +countDis)) + "\n";
        }
        System.out.println(line);
        fsWriter.write(line);
        fsWriter.close();
        System.out.println();
    }

    public static void run(Path p, String outPrefix) throws IOException {
        new AlignTable2Contact(outPrefix + ".aligntable",outPrefix,p);
    }

    public static long calculateSum(List<Integer> numbers) {
        long sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        return sum;
    }

}
