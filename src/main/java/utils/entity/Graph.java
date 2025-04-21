package utils.entity;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private int numVertices;
    private boolean[][] adjMatrix;

    public Graph(int numVertices, boolean[][] adjMatrix) {
        this.numVertices = numVertices;
        this.adjMatrix = adjMatrix;
    }

    public static void main(String[] args) {
        int numVertices = 5;
        boolean[][] adjMatrix = {
                {false, false, false, true, true},
                {true, false, true, true, true},
                {true, true, false, true, true},
                {true, true, true, false, true},
                {true, true, true, true, false}
        };

        Graph graph = new Graph(numVertices, adjMatrix);
        List<Integer> largestCompleteSubgraph = graph.findLargestCompleteSubgraph();

        // 打印最大完全子图
        System.out.println("Largest Complete Subgraph: " + largestCompleteSubgraph);
    }

    // 深度优先搜索算法来查找完全子图，并验证是否为完全子图
    private void findCompleteSubgraphs(int start, int currentSize, List<Integer> currentSubgraph, List<List<Integer>> completeSubgraphs) {
        if (currentSize > 1) {
            // 检查当前子图是否是完全子图
            boolean isComplete = true;
            for (int i = 0; i < currentSize - 1; i++) {
                for (int j = i + 1; j < currentSize; j++) {
                    int node1 = currentSubgraph.get(i);
                    int node2 = currentSubgraph.get(j);
                    if (!adjMatrix[node1][node2] && !adjMatrix[node2][node1]) {
                        isComplete = false;
                        break;
                    }
                }
                if (!isComplete) {
                    break;
                }
            }
            if (isComplete) {
                completeSubgraphs.add(new ArrayList<>(currentSubgraph));
            }
        }

        for (int i = start; i < numVertices; i++) {
            currentSubgraph.add(i);
            findCompleteSubgraphs(i + 1, currentSize + 1, currentSubgraph, completeSubgraphs);
            currentSubgraph.remove(currentSubgraph.size() - 1);
        }
    }

    // 查找最大完全子图的入口方法
    public List<Integer> findLargestCompleteSubgraph() {
        List<List<Integer>> completeSubgraphs = new ArrayList<>();
        List<Integer> currentSubgraph = new ArrayList<>();
        findCompleteSubgraphs(0, 0, currentSubgraph, completeSubgraphs);

        // 寻找最大完全子图
        List<Integer> largestCompleteSubgraph = new ArrayList<>();
        for (List<Integer> subgraph : completeSubgraphs) {
            if (subgraph.size() > largestCompleteSubgraph.size()) {
                largestCompleteSubgraph = subgraph;
            }
        }

        return largestCompleteSubgraph;
    }
}
