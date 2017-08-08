/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;
import java.util.Arrays;

/**
 *
 * @author 
 */
public class FloydMarshall {
      // graph represented by an adjacency matrix
    private double[][] graph;

    private boolean negativeCycle;

    public FloydMarshall(int n) {
        this.graph = new double[n][n];
        initGraph();
    }
     private void initGraph() {
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                if (i == j) {
                    graph[i][j] = 0;
                } else {
                    graph[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    public boolean hasNegativeCycle() {
        return this.negativeCycle;
    }

    // utility function to add edges to the adjacencyList
    public void addEdge(int from, int to, double weight) {
        graph[from][to] = weight;
    }

    // all-pairs shortest path
    public double[][] floydWarshall() {
        double[][] distances;
        int n = this.graph.length;
        distances = Arrays.copyOf(this.graph, n);

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    distances[i][j] = Math.min(distances[i][j], distances[i][k] + distances[k][j]);
                }
            }

            if (distances[k][k] < 0.0) {
                this.negativeCycle = true;
            }
        }

        return distances;
    }

}
