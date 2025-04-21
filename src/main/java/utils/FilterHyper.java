package utils;

import process.contact.ComparatorReadATBySE;
import process.contact.ReadAT;
import utils.entity.ChrSizes;
import utils.entity.SubgraphsFinder;

import java.io.*;
import java.util.*;

//columns: readID chr1 pos1 chr2 pos2 strand1 strand2
public class FilterHyper {
    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.out.println("Usage: java -cp PPL.jar utils.FilterHyper <monomers> <monomers.filtered> <chromSizes> <binSize, 1000000> <cutoff, default:0.85> <range> <delete singleton? Y/N>");
            System.exit(0);
        }
        //读取参数
        String iF = args[0];
        String oF = args[1];
        String chrFile = args[2];
        Integer binSize = Integer.parseInt(args[3]);
        //percentile
        double cutoff = 0.85;
        if (args.length >= 5) {
            cutoff = Float.parseFloat(args[4]);
        }
        //delete singleton
        boolean deleteSingleton = true;
        if (args.length >= 6) {
            deleteSingleton = args[5].equalsIgnoreCase("N")?false:true;
        }
        //用于限制区域
        boolean isRangeLimited=false;
        String range=null;
        String rangeChr=null;
        Long rangeStart=null;
        Long rangeEnd=null;
        if (args.length >= 6) {
            isRangeLimited=true;
            range = args[5];
            rangeChr = range.split(":")[0];
            if (range.contains(":")) {
                rangeStart = Long.parseLong(range.split(":")[1].split("-")[0]);
                rangeEnd = Long.parseLong(range.split(":")[1].split("-")[1]);
            } else {
                rangeStart = (long) 0;
                rangeEnd = Long.MAX_VALUE;
            }
        }

        //读取chromSizes 文件，划分bin
        String line;
        ChrSizes chrSizes = new ChrSizes(chrFile, binSize);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: chromosome sizes info:");
        for (String chrName :
                chrSizes.getChrMap().keySet()) {
            System.out.println(
                    "Chromsomes INFO: " +
                    chrName + "\t" +
                            chrSizes.getChrMap().get(chrName) + "\t" +
                            Arrays.toString(chrSizes.getChrBinRange().get(chrName))
            );

        }
        System.out.println(
                "nbins\t" +
                chrSizes.getNumBins()
        );
        System.out.println(
                "bin-size\t" +
                chrSizes.getResolution()+
                "\n"
        );


        //map记录每个格子的count数量
        Map<Integer, Map<Integer, Float>> statMap = new TreeMap<>();

        //转换成pair后统计
        List<ReadAT> concatemer = new ArrayList<ReadAT>();
        //2pairs
        MyUtil.checkPath(iF);
        BufferedReader mmReader = new BufferedReader(new FileReader(iF));

        BufferedWriter oWriter = new BufferedWriter(new FileWriter(oF));
        concatemer.clear();
        Set<ReadAT> mms = new HashSet<>();
        Judgement judgement = new Judgement();
        long countReads = 0;
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: stating count distribution: start (percentile: "+cutoff+")");
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: inputfile=" + iF);
        while ((line = mmReader.readLine()) != null) {
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //只使用有效的结果
            if (!judgement.judgeS(one)) {
                continue;
            }
            if (!chrSizes.getChrSet().contains(one.getChr())){
                continue;
            }
            if (isRangeLimited &&
                !(rangeChr.equalsIgnoreCase(one.getChr())
                    && rangeStart < one.getMid()
                    && rangeEnd > one.getMid()
            )){
                continue;
            }
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                mms.clear();
                for (ReadAT mm1 :
                        concatemer) {
                    mms.add(mm1);
                    for (ReadAT mm2 :
                            concatemer) {
                        if (mms.contains(mm2))
                            continue;
//                            break;
                        else {
                            int flag = 0;
                            if (mm1.chr.compareTo(mm2.chr) > 0) {
                                ReadAT tmp = mm1;
                                mm1 = mm2;
                                mm2 = tmp;
                                flag = 1;
                            }

//                            #columns: readID chr1 pos1 chr2 pos2 strand1 strand2
                            alterMapV(
                                    statMap,
                                    mm1.getChr(),
                                    (int) ((mm1.start + mm1.end) / 2),
                                    mm2.getChr(),
                                    (int) ((mm2.start + mm2.end) / 2),
                                    binSize,
                                    chrSizes
                            );
                            if (flag == 1) {
                                mm1 = mm2;
                            }
                        }
                    }
                }
                countReads++;
                if (countReads % 1000000 == 0) {
                    System.out.println(MyUtil.getTimeFormatted() + " " +
                            "FilterHyper: " + countReads+" of concatemers were finished......");
                }
                concatemer.clear();
                concatemer.add(one);
            }
        }

        //处理最后一个
        {
            mms.clear();
            for (ReadAT mm1 :
                    concatemer) {
                mms.add(mm1);
                for (ReadAT mm2 :
                        concatemer) {
                    if (mms.contains(mm2))
                        continue;
//                            break;
                    else {
                        int flag = 0;
                        if (mm1.chr.compareTo(mm2.chr) > 0) {
                            ReadAT tmp = mm1;
                            mm1 = mm2;
                            mm2 = tmp;
                            flag = 1;
                        }

//                            #columns: readID chr1 pos1 chr2 pos2 strand1 strand2
                        alterMapV(
                                statMap,
                                mm1.getChr(),
                                (int) ((mm1.start + mm1.end) / 2),
                                mm2.getChr(),
                                (int) ((mm2.start + mm2.end) / 2),
                                binSize,
                                chrSizes
                        );
                        if (flag == 1) {
                            mm1 = mm2;
                        }
                    }
                }
            }
            countReads++;
            if (countReads % 1000000 == 0) {
                System.out.println(MyUtil.getTimeFormatted() + " " +
                        "FilterHyper: " + countReads+" of concatemers were finished......");
            }
            concatemer.clear();
        }
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: stating count distribution: end (percentile: "+cutoff+")\n");

        //计算阈值，分位数
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: computing cutoff count: start (percentile: "+cutoff+")");
        List<Float> statList = new ArrayList<>();
        for (Map<Integer, Float> subStatMap :
             statMap.values()) {
            statList.addAll(subStatMap.values());
        }
        System.out.println("statList.size() = " + statList.size());
        float cutoffCount = calculatePercentile(statList, cutoff);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: computing cutoff count: end (cutoff: "+cutoffCount+")");

        int countUp=0;
        int countDown=0;
        for (Float kk:
             statList) {
            if (kk < cutoffCount) countDown++;
            if (kk > cutoffCount) countUp++;
        }
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: "+"\tpassed tiles count:"+countUp+"\tunpassed tiles count:"+countDown);

        //删除<cutoffCount的tiles
        for (Map<Integer, Float> subStatMap :
                statMap.values()) {
            Iterator<Map.Entry<Integer, Float>> iterator = subStatMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Integer, Float> entry = iterator.next();
                if (entry.getValue() < cutoffCount){
                    iterator.remove();
                }
            }
        }

        statList.clear();
        for (Map<Integer, Float> subStatMap :
                statMap.values()) {
            statList.addAll(subStatMap.values());
        }
        System.out.println("statList.size() = " + statList.size());


        //再次遍历contacts文件，过滤掉低频的交互
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: filtering hypergraph: start (cutoff: "+cutoffCount+")");
        mmReader.close();
        mmReader = new BufferedReader(new FileReader(iF));
        //用于保存过筛的monomers
        Set<ReadAT> atSet = new HashSet<>();
        List<ReadAT> atList = new ArrayList<>();
        ComparatorReadATBySE comparatorReadATBySE = new ComparatorReadATBySE();
        countReads=0;
        int countAllMappings=0;
        int countFilteredMappings=0;
        while ((line = mmReader.readLine()) != null) {
            String[] fields = line.split("\t");
            ReadAT one = new ReadAT(fields);
            //只使用有效的结果
            if (!judgement.judgeS(one)) {
                continue;
            }
            if (!chrSizes.getChrSet().contains(one.getChr())){
                continue;
            }
            if (isRangeLimited &&
                    !(rangeChr.equalsIgnoreCase(one.getChr())
                            && rangeStart < one.getMid()
                            && rangeEnd > one.getMid()
                    )){
                continue;
            }
            countAllMappings++;
            //将同一个concatemer的比对在一个批次进行处理
            if (concatemer.size() == 0) {
                concatemer.add(one);
            } else if (one.readName.equals(concatemer.get(0).readName)) {
                concatemer.add(one);
            } else {
                int[][] adjMatrix = new int[concatemer.size()][concatemer.size()];
                for (int[] row :
                        adjMatrix) {
                    Arrays.fill(row, 0);
                }

                mms.clear();
                for (ReadAT mm1 :
                        concatemer) {
                    mms.add(mm1);
                    for (ReadAT mm2 :
                            concatemer) {
                        if (mms.contains(mm2))
                            continue;
//                            break;
                        else {
                            int flag = 0;
                            if (mm1.chr.compareTo(mm2.chr) > 0) {
                                ReadAT tmp = mm1;
                                mm1 = mm2;
                                mm2 = tmp;
                                flag = 1;
                            }
                            // 若过筛，则加入候选集合
                            ////1.1 建立邻接矩阵
                            if (isQualified(
                                    statMap,
                                    mm1.getChr(),
                                    (int) ((mm1.start + mm1.end) / 2),
                                    mm2.getChr(),
                                    (int) ((mm2.start + mm2.end) / 2),
                                    binSize,
                                    chrSizes)){
                                adjMatrix[concatemer.indexOf(mm1)][concatemer.indexOf(mm2)]=1;
                                adjMatrix[concatemer.indexOf(mm2)][concatemer.indexOf(mm1)]=1;
                            }
                            if (flag == 1) {
                                mm1 = mm2;
                            }
                        }
                    }
                }
                countReads++;
                if (countReads % 1000000 == 0) {
                    System.out.println(MyUtil.getTimeFormatted() + " " +
                            "FilterHyper: " + countReads+" of concatemers were finished......");
                }

                ////2.1 连通子图方法-从临界矩阵寻找完全子图
                List<List<Integer>> allSubgraphs = null;
                if (deleteSingleton){
                    allSubgraphs = SubgraphsFinder.findAllSubgraphsWithoutSingleton(adjMatrix);
                }else {
                    allSubgraphs = SubgraphsFinder.findAllSubgraphs(adjMatrix);
                }
                for (int i = 0; i <allSubgraphs.size(); i++) {
                    List<Integer> subgraph = allSubgraphs.get(i);
                    for (Integer indexMonomer :
                            subgraph) {
                        ReadAT at = concatemer.get(indexMonomer);
                        at.setReadName(at.getReadName()+"."+i);
                        atSet.add(at);
                    }
                }

                ////2.2 最大完全子图方法-从临界矩阵寻找完全子图
//                int numNodes = adjMatrix.length;
//                List<List<Integer>> fullyConnectedSubgraphs = new ArrayList<>();
//                boolean[] visited = new boolean[numNodes];
//
//                for (int i = 0; i < numNodes; i++) {
//                    if (!visited[i]) {
//                        List<Integer> subgraph = new ArrayList<>();
//                        findFullyConnectedSubgraph(adjMatrix, visited, i, subgraph);
//                        fullyConnectedSubgraphs.add(subgraph);
//                        for (Integer index :
//                                subgraph) {
//                            atSet.add(concatemer.get(index));
//                        }
//                    }
//                }
//
//                // 输出全连接子图
//                if (concatemer.size()>5) {
//                    for (List<Integer> subgraph : fullyConnectedSubgraphs) {
//                        System.out.println("Fully Connected Subgraph: " + subgraph);
//                    }
//                }

                atList.addAll(atSet);
                atList.sort(comparatorReadATBySE);
                for (ReadAT monomer :
                        atList) {
                    countFilteredMappings++;
                    oWriter.write(monomer.toContactLine());
                    oWriter.newLine();
                }
                atSet.clear();
                atList.clear();
                concatemer.clear();
                concatemer.add(one);
            }
        }
        {
            int[][] adjMatrix = new int[concatemer.size()][concatemer.size()];
            for (int[] row :
                    adjMatrix) {
                Arrays.fill(row, 0);
            }

            mms.clear();
            for (ReadAT mm1 :
                    concatemer) {
                mms.add(mm1);
                for (ReadAT mm2 :
                        concatemer) {
                    if (mms.contains(mm2))
                        continue;
//                            break;
                    else {
                        int flag = 0;
                        if (mm1.chr.compareTo(mm2.chr) > 0) {
                            ReadAT tmp = mm1;
                            mm1 = mm2;
                            mm2 = tmp;
                            flag = 1;
                        }
                        // 若过筛，则加入候选集合
                        ////1.1 建立邻接矩阵
                        if (isQualified(
                                statMap,
                                mm1.getChr(),
                                (int) ((mm1.start + mm1.end) / 2),
                                mm2.getChr(),
                                (int) ((mm2.start + mm2.end) / 2),
                                binSize,
                                chrSizes)){
                            adjMatrix[concatemer.indexOf(mm1)][concatemer.indexOf(mm2)]=1;
                            adjMatrix[concatemer.indexOf(mm2)][concatemer.indexOf(mm1)]=1;
                        }
                        if (flag == 1) {
                            mm1 = mm2;
                        }
                    }
                }
            }
            countReads++;
            if (countReads % 1000000 == 0) {
                System.out.println(MyUtil.getTimeFormatted() + " " +
                        "FilterHyper: " + countReads+" of concatemers were finished......");
            }

            ////2.1 连通子图方法-从临界矩阵寻找完全子图
            List<List<Integer>> allSubgraphs = null;
            if (deleteSingleton){
                allSubgraphs = SubgraphsFinder.findAllSubgraphsWithoutSingleton(adjMatrix);
            }else {
                allSubgraphs = SubgraphsFinder.findAllSubgraphs(adjMatrix);
            }
            for (int i = 0; i <allSubgraphs.size(); i++) {
                List<Integer> subgraph = allSubgraphs.get(i);
                for (Integer indexMonomer :
                        subgraph) {
                    ReadAT at = concatemer.get(indexMonomer);
                    at.setReadName(at.getReadName()+"."+i);
                    atSet.add(at);
                }
            }

            ////2.2 最大完全子图方法-从临界矩阵寻找完全子图
//                int numNodes = adjMatrix.length;
//                List<List<Integer>> fullyConnectedSubgraphs = new ArrayList<>();
//                boolean[] visited = new boolean[numNodes];
//
//                for (int i = 0; i < numNodes; i++) {
//                    if (!visited[i]) {
//                        List<Integer> subgraph = new ArrayList<>();
//                        findFullyConnectedSubgraph(adjMatrix, visited, i, subgraph);
//                        fullyConnectedSubgraphs.add(subgraph);
//                        for (Integer index :
//                                subgraph) {
//                            atSet.add(concatemer.get(index));
//                        }
//                    }
//                }
//
//                // 输出全连接子图
//                if (concatemer.size()>5) {
//                    for (List<Integer> subgraph : fullyConnectedSubgraphs) {
//                        System.out.println("Fully Connected Subgraph: " + subgraph);
//                    }
//                }

            atList.addAll(atSet);
            atList.sort(comparatorReadATBySE);
            for (ReadAT monomer :
                    atList) {
                countFilteredMappings++;
                oWriter.write(monomer.toContactLine());
                oWriter.newLine();
            }
            atSet.clear();
            atList.clear();
            concatemer.clear();
        }

        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: filtering hypergraph: end (cutoff: "+cutoffCount+")\n");


        mmReader.close();
        oWriter.close();
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: count of all reads=" + countReads);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: count of all mappings=" + countAllMappings);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: count of all filtered mappings=" + countFilteredMappings);
        System.out.println(MyUtil.getTimeFormatted() + " " +
                "FilterHyper: finished");
    }
    
//    static void deleteSingleton(Set<ReadAT> atSet){
//        for (ReadAT at :
//                ReadAT) {
//
//        }
//    }

    //判断当前交互是否显著
    static boolean isQualified(
            Map<Integer, Map<Integer, Float>> map,
            String chrName1, Integer key1,
            String chrName2, Integer key2,
            Integer binSize,
            ChrSizes chrSizes
    ){
        //坐标转bin id
        int offset1 = chrSizes.getOffset(chrName1);
        int offset2 = chrSizes.getOffset(chrName2);
        key1 = key1 / binSize + offset1;
        key2 = key2 / binSize + offset2;
        if (key1 > chrSizes.getMaxBinByChr(chrName1) ||
                key2 > chrSizes.getMaxBinByChr(chrName2)){
            System.out.println("error: bin id was bigger than biggest in the chr");
        }

        Map<Integer, Float> subMap = map.get(key1);

        return subMap.containsKey(key2);
    }

    //用于修改交互数目
    static void alterMapV(Map<Integer, Map<Integer, Float>> map,
                          String chrName1, Integer key1,
                          String chrName2, Integer key2,
                          Integer binSize,
                          ChrSizes chrSizes) {
        //坐标转bin id
        int offset1 = chrSizes.getOffset(chrName1);
        int offset2 = chrSizes.getOffset(chrName2);
        key1 = key1 / binSize + offset1;
        key2 = key2 / binSize + offset2;
        if (key1 > chrSizes.getMaxBinByChr(chrName1) ||
            key2 > chrSizes.getMaxBinByChr(chrName2)){
            System.out.println("error: bin id was bigger than biggest in the chr");
        }


        if (map.get(key1) == null) {
            map.put(key1, new TreeMap<>());
            map.get(key1).put(key2, (float) 1);
        } else {
            Map<Integer, Float> subMap = map.get(key1);
            if (subMap.get(key2) == null) {
                subMap.put(key2, (float) 1);
            } else {
                subMap.put(key2, 1 + subMap.get(key2));
            }
        }
    }

    // 计算百分位数的方法
    public static float calculatePercentile(List<Float> values, int percentile) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("值列表为空");
        }

        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        // 使用快速选择算法查找分位数
        return quickSelect(values, 0, values.size() - 1, index);
    }

    // 计算百分位数的方法
//    public static float calculatePercentile(List<Float> values, float percentile) {
//        if (values.isEmpty()) {
//            throw new IllegalArgumentException("值列表为空");
//        }
//
//        // 选择第index大的为cutoff
//        int index = (int) Math.ceil(percentile * values.size()) - 1;
//        System.out.println("values.size() = " + values.size());
//        System.out.println("index = " + index);
//        // 使用快速选择算法查找分位数
//        return quickSelect(values, 0, values.size() - 1, index);
//    }

    // 使用快速选择算法来查找分位数
    private static Float quickSelect(List<Float> values, int left, int right, int k) {
        if (left == right) {
            // 当左边界等于右边界时，只有一个元素，它就是分位数
            return values.get(left);
        }

        // 使用快速排序的分区方法找到中值的索引
        int pivotIndex = partition(values, left, right);
        if (k == pivotIndex) {
            // 如果分区后的中值索引等于目标k，那么这个中值就是百分位数
            return values.get(k);
        } else if (k < pivotIndex) {
            // 如果k小于中值索引，继续在左侧子数组中查找
            return quickSelect(values, left, pivotIndex - 1, k);
        } else {
            // 如果k大于中值索引，继续在右侧子数组中查找
            return quickSelect(values, pivotIndex + 1, right, k);
        }
    }

    // 使用快速排序的分区方法来确定中值的索引
    private static int partition(List<Float> values, int left, int right) {
        // 选择一个枢轴元素，这里选择最右边的元素作为枢轴
        Float pivot = values.get(right);
        int i = left;
        for (int j = left; j < right; j++) {
            // 将小于等于枢轴的元素移到左侧
            if (values.get(j) <= pivot) {
                Collections.swap(values, i, j);
                i++;
            }
        }
        // 将枢轴元素放到正确的位置
        Collections.swap(values, i, right);
        return i; // 返回枢轴元素的索引
    }

    public static float calculatePercentile(List<Float> values, double percentile) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("值列表为空");
        }

        int n = values.size();
        int index = (int) Math.ceil(percentile * n) - 1;
        return quickSelectIterative(values, index);
    }

    private static Float quickSelectIterative(List<Float> values, int k) {
        int left = 0;
        int right = values.size() - 1;

        int progressThreshold = (int) (0.02 * (right - left + 1)); // 定义每完成2%输出一次进度
        int progressCounter = 0;
        int processedElements = 0; // 追踪已处理的元素数目

        while (left < right) {
            int pivotIndex = partition(values, left, right);
            if (k == pivotIndex) {
                return values.get(k);
            } else if (k < pivotIndex) {
                right = pivotIndex - 1;
            } else {
                left = pivotIndex + 1;
            }
//            System.out.println("progressCounter = " + progressCounter);
            processedElements++;
            progressCounter++;
            if (progressCounter >= progressThreshold) {
                double progressPercentage = ((double) processedElements / (double) (values.size())) * 100;
                System.out.println("Sorting Progress: " + String.format("%.2f", progressPercentage) + "% finished (" + processedElements + "/" + values.size() + ")");
                progressCounter = 0; // 重置计数器
            }
        }

        // 输出最终进度
        double progressPercentage = ((double) processedElements / (double) (values.size())) * 100;
        System.out.println("Sorting Progress: " + String.format("%.2f", progressPercentage) + "% finished (" + processedElements + "/" + values.size() + ")");

        return values.get(k);
    }

}
