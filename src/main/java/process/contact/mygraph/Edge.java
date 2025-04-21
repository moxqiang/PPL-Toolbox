package process.contact.mygraph;

import java.util.Objects;

/**
 * 边的信息 * @author binbin.hou * @since 0.0.2
 */
public class Edge<V> {
    /**
     * 开始节点 * @since 0.0.2
     */
    private V from;
    /**
     * 结束节点 * @since 0.0.2
     */
    private V to;
    /**
     * 权重 * @since 0.0.2
     */
    private double weight;
    private double weightO;
    private double weightG;
    private double weightA;

    public Edge(V from, V to) {
        this.from = from;
        this.to = to;
    }

    public Edge(V from, V to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Edge(V from, V to, double weight, double weightO, double weightG, double weightA) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.weightO = weightO;
        this.weightG = weightG;
        this.weightA = weightA;

    }

    public V getFrom() {
        return from;
    }

    public void setFrom(V from) {
        this.from = from;
    }

    public V getTo() {
        return to;
    }

    public void setTo(V to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public double getWeightO() {
        return weightO;
    }

    public void setWeightO(double weightO) {
        this.weightO = weightO;
    }

    public double getWeightG() {
        return weightG;
    }

    public void setWeightG(double weightG) {
        this.weightG = weightG;
    }

    public double getWeightA() {
        return weightA;
    }

    public void setWeightA(double weightA) {
        this.weightA = weightA;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Edge{" + "from=" + from + ", to=" + to + ", weight=" + weight + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge<?> edge = (Edge<?>) o;
        return Double.compare(edge.weight, weight) == 0 && Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }
}