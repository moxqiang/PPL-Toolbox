package utils.entity;

import java.util.ArrayList;
import java.util.List;

public class SubgraphsFinder {

    public static List<List<Integer>> findAllSubgraphs(int[][] adjacencyMatrix) {
        List<List<Integer>> subgraphs = new ArrayList<>();
        int n = adjacencyMatrix.length;
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                List<Integer> subgraph = new ArrayList<>();
                findSubgraph(adjacencyMatrix, visited, i, subgraph);
                subgraphs.add(subgraph);
            }
        }

        return subgraphs;
    }

    public static List<List<Integer>> findAllSubgraphsWithoutSingleton(int[][] adjacencyMatrix) {
        List<List<Integer>> subgraphs = new ArrayList<>();
        int n = adjacencyMatrix.length;
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                List<Integer> subgraph = new ArrayList<>();
                findSubgraph(adjacencyMatrix, visited, i, subgraph);
                subgraphs.add(subgraph);
            }
        }

        List<List<Integer>> newSubgraphs = new ArrayList<>();

        for (int i = 0; i < subgraphs.size(); i++) {
            if (subgraphs.get(i).size() > 1){
                newSubgraphs.add(subgraphs.get(i));
            }
        }

        return newSubgraphs;
    }

    private static void findSubgraph(int[][] adjacencyMatrix, boolean[] visited, int node, List<Integer> subgraph) {
        visited[node] = true;
        subgraph.add(node);

        for (int i = 0; i < adjacencyMatrix.length; i++) {
            if (adjacencyMatrix[node][i] == 1 && !visited[i]) {
                findSubgraph(adjacencyMatrix, visited, i, subgraph);
            }
        }
    }

    public static void main(String[] args) {
        int[][] adjacencyMatrix = {
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 1, 0, 0, 1},
                {0, 0, 0, 0, 1},
                {0, 0, 1, 1, 0}
        };

        List<List<Integer>> subgraphs = findAllSubgraphs(adjacencyMatrix);
        System.out.println("All Subgraphs:");

        for (List<Integer> subgraph : subgraphs) {
            System.out.println(subgraph);
        }
    }
}
