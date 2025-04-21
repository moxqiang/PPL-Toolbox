//import edu.uci.ics.jung.graph.Graph;
//import edu.uci.ics.jung.graph.UndirectedSparseGraph;
//import edu.uci.ics.jung.algorithms
//import edu.uci.ics.jung.algorithms.community.LouvainCommunity;
//import edu.uci.ics.jung.algorithms.util.MapUtils;
//
//public class LouvainExample {
//    public static void main(String[] args) {
//        // 创建一个无向图
//        Graph<Integer, String> graph = new UndirectedSparseGraph<>();
//
//        // 添加节点
//        for (int i = 1; i <= 6; i++) {
//            graph.addVertex(i);
//        }
//
//        // 添加边及其权重
//        graph.addEdge("1-2", 1, 2);
//        graph.addEdge("1-3", 1, 3);
//        graph.addEdge("2-3", 2, 3);
//        graph.addEdge("4-5", 4, 5);
//        graph.addEdge("4-6", 4, 6);
//        graph.addEdge("5-6", 5, 6);
//
//        // 创建Louvain算法对象
//        LouvainCommunity<Integer, String> louvain = new LouvainCommunity<>(graph);
//
//        // 运行Louvain算法
//        louvain.execute();
//
//        // 获取社区结果
//        MapUtils.mapWithKeys(louvain.getPartition());
//
//        // 输出社区结果
//        for (Integer vertex : graph.getVertices()) {
//            System.out.println("Node " + vertex + " belongs to community " + louvain.getPartition().get(vertex));
//        }
//    }
//}
