package utils.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AllMaxCliques {

    private int[][] graph;
    private List<List<Integer>> allCliques;

    public AllMaxCliques(int[][] adjacencyMatrix) {
        this.graph = adjacencyMatrix;
        allCliques = new ArrayList<>();
    }

    public static List<List<Integer>> filterSublists(List<List<Integer>> lists) {
        List<List<Integer>> filteredLists = new ArrayList<>();

        for (int i = 0; i < lists.size(); i++) {
            List<Integer> currentList = lists.get(i);
            boolean isSubset = false;

            // 检查当前列表是否被更长的列表所包含
            for (int j = 0; j < lists.size(); j++) {
                if (i != j) {
                    List<Integer> otherList = lists.get(j);
                    if (isSubset(currentList, otherList)) {
                        isSubset = true;
                        break;
                    }
                }
            }

            // 如果不是其他列表的子集，则添加到结果列表
            if (!isSubset) {
                filteredLists.add(currentList);
            }
        }

        return filteredLists;
    }

    private static boolean isSubset(List<Integer> list1, List<Integer> list2) {
        return list2.containsAll(list1);
    }

    public static int[][] generateRandomGraph(int numNodes, double edgeProbability) {
        int[][] adjacencyMatrix = new int[numNodes][numNodes];
        Random random = new Random();

        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (random.nextDouble() < edgeProbability) {
                    // 以edgeProbability的概率在节点i和节点j之间添加一条边
                    adjacencyMatrix[i][j] = 1;
                    adjacencyMatrix[j][i] = 1;
                }
            }
        }

        return adjacencyMatrix;
    }

    public static boolean validateCliques(int[][] adjacencyMatrix, List<List<Integer>> cliques) {
        for (List<Integer> clique : cliques) {
            // 检查clique中的节点是否形成一个团
            for (int i = 0; i < clique.size(); i++) {
                for (int j = i + 1; j < clique.size(); j++) {
                    int node1 = clique.get(i);
                    int node2 = clique.get(j);
                    if (adjacencyMatrix[node1][node2] != 1) {
                        return false;
                    }
                }
            }

            // 检查clique是否是最大团
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                if (!clique.contains(i)) {
                    boolean isAdjacentToAll = true;
                    for (int node : clique) {
                        if (adjacencyMatrix[i][node] != 1) {
                            isAdjacentToAll = false;
                            break;
                        }
                    }
                    if (isAdjacentToAll) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int numNodes = 50; // 节点数量
        double edgeProbability = 0.5; // 边的概率

        int[][] adjacencyMatrix = generateRandomGraph(numNodes, edgeProbability);

        // 调用你的最大团查找算法
        AllMaxCliques allMaxCliquesFinder = new AllMaxCliques(adjacencyMatrix);
        allMaxCliquesFinder.findAllMaxCliques();
        List<List<Integer>> allCliques = allMaxCliquesFinder.getAllMaxCliques();
//        List<List<Integer>> allCliques = allMaxCliquesFinder.getAllCliques();

        System.out.println("All Maximum Cliques:");
        for (List<Integer> clique : allCliques) {
            System.out.println(clique);
        }

        // 验证找到的最大团
        boolean isValid = validateCliques(adjacencyMatrix, allCliques);
        if (isValid) {
            System.out.println("All Maximum Cliques are valid.");
        } else {
            System.out.println("Some Maximum Cliques are invalid.");
        }
    }

    public void findAllMaxCliques() {
        int n = graph.length;
        for (int i = 0; i < n; i++) {
            List<Integer> currentClique = new ArrayList<>();
            currentClique.add(i); // 初始化当前团，将节点 i 添加到其中
            findMaxCliques(i, currentClique);
        }
    }

    private void findMaxCliques(int startNode, List<Integer> currentClique) {
        int n = graph.length;

        // 检查当前团是否是一个最大团
        if (isClique(currentClique)) {
            allCliques.add(new ArrayList<>(currentClique)); // 添加当前团的副本到结果列表
        }

        // 继续搜索下一个最大团
        for (int i = startNode + 1; i < n; i++) {
            if (canAddToClique(i, currentClique)) {
                currentClique.add(i);
                findMaxCliques(i, currentClique);
                currentClique.remove(currentClique.size() - 1); // 回溯
            }
        }
    }

    private boolean isClique(List<Integer> currentClique) {
        if (currentClique.size()<=1){
            return false;
        }
        for (int i = 0; i < currentClique.size(); i++) {
            for (int j = i + 1; j < currentClique.size(); j++) {
                if (graph[currentClique.get(i)][currentClique.get(j)] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canAddToClique(int node, List<Integer> currentClique) {
        for (int i : currentClique) {
            if (graph[node][i] == 0) {
                return false;
            }
        }
        return true;
    }

    public List<List<Integer>> getAllCliques() {
        return allCliques;
    }

    public List<List<Integer>> getAllMaxCliques() {

        return filterSublists(allCliques);
    }
}
