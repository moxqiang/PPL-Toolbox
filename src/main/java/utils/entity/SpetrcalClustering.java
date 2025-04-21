package utils.entity;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.List;

public class SpetrcalClustering {

    public static List<Cluster<DoublePoint>> spectralClustering(int[][] adjacencyMatrix, int numClusters) {
        int dataSize = adjacencyMatrix.length;

        // Step 1: Compute the degree matrix
        RealMatrix degreeMatrix = computeDegreeMatrix(adjacencyMatrix);

        // Step 2: Compute the Laplacian matrix
        RealMatrix laplacianMatrix = computeLaplacianMatrix(degreeMatrix, adjacencyMatrix);

        // Step 3: Compute the first k eigenvectors of the Laplacian matrix
        RealMatrix eigenVectors = computeEigenVectors(laplacianMatrix, numClusters);

        // Step 4: Cluster the data using k-means on the eigenvectors
        List<DoublePoint> eigenvectorData = new ArrayList<>();
        for (int i = 0; i < dataSize; i++) {
            double[] eigenvector = eigenVectors.getRow(i);
            eigenvectorData.add(new DoublePoint(eigenvector));
        }

        Clusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(numClusters);
        return (List<Cluster<DoublePoint>>) clusterer.cluster(eigenvectorData);
    }

    private static RealMatrix computeDegreeMatrix(int[][] adjacencyMatrix) {
        int dataSize = adjacencyMatrix.length;
        RealMatrix degreeMatrix = MatrixUtils.createRealMatrix(dataSize, dataSize);

        for (int i = 0; i < dataSize; i++) {
            int degree = 0;
            for (int j = 0; j < dataSize; j++) {
                degree += adjacencyMatrix[i][j];
            }
            degreeMatrix.setEntry(i, i, degree);
        }

        return degreeMatrix;
    }

    private static RealMatrix computeLaplacianMatrix(RealMatrix degreeMatrix, int[][] adjacencyMatrix) {
        return degreeMatrix.subtract(MatrixUtils.createRealMatrix(convertIntegerArrayToDouble(adjacencyMatrix)));
    }

    public static double[][] convertIntegerArrayToDouble(int[][] integerArray) {
        int numRows = integerArray.length;
        int numCols = integerArray[0].length;

        double[][] doubleArray = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                doubleArray[i][j] = (double) integerArray[i][j];
            }
        }

        return doubleArray;
    }


    private static RealMatrix computeEigenVectors(RealMatrix matrix, int numEigenVectors) {
        EigenDecomposition decomposition = new EigenDecomposition(matrix);
        RealMatrix eigenVectors = decomposition.getV();

        return eigenVectors.getSubMatrix(0, matrix.getRowDimension() - 1, 0, numEigenVectors - 1);
    }

    public static void main(String[] args) {
        // Define your adjacency matrix here (as a 2D array of 0s and 1s)
        int[][] adjacencyMatrix = {
                {0, 1, 0, 0, 1},
                {1, 0, 1, 0, 0},
                {0, 1, 0, 0, 0},
                {0, 0, 0, 0, 1},
                {1, 0, 0, 1, 0}
        };

        int numClusters = 3; // Number of clusters

        List<Cluster<DoublePoint>> clusters = spectralClustering(adjacencyMatrix, numClusters);

        // Print or process the resulting clusters
        for (Cluster<DoublePoint> cluster : clusters) {
            System.out.println("Cluster:");
            for (DoublePoint point : cluster.getPoints()) {
                System.out.println(point);
            }
            System.out.println();
        }
    }
}
