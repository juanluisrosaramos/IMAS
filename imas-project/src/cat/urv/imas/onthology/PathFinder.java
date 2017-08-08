/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author carles
 */
public class PathFinder {
    SimpleGraph<String,DefaultEdge> graph;
    Cell[][] map;

    public Cell[][] getMap() {
        return map;
    }
    FloydWarshallShortestPaths solver;
    public PathFinder(Cell[][] map) {
        this.map = map;
        this.graph = buildGraph();
        
        solver = new FloydWarshallShortestPaths(this.graph);
    }
    
   private SimpleGraph<String,DefaultEdge> buildGraph(){
       SimpleGraph<String,DefaultEdge> g = new SimpleGraph(DefaultEdge.class);
       for(int i=0;i<map.length;i++){
            int ii = i-1;
            for(int j=0;j<map[0].length;j++){
                int jj = j+1;
                if(map[i][j] instanceof StreetCell){
                    String current = UtilsAgents.getKey(map[i][j]);
                    g.addVertex(current);
                    if(ii>-1){
                        if(map[ii][j] instanceof StreetCell){
                            String adjacent = UtilsAgents.getKey(map[ii][j]);
                            g.addVertex(adjacent);
                            g.addEdge(current, adjacent);
                        }
                    } 
                    if(jj<map[0].length){
                        if(map[i][jj] instanceof StreetCell){
                            String adjacent = UtilsAgents.getKey(map[i][jj]);
                            g.addVertex(adjacent);
                            g.addEdge(current, adjacent);
                        }
                    }
                }
            }
        }
       return g;
   }
   public List<String> getPath(String source, String dest){       
       return solver.getShortestPathAsVertexList(source, dest);    
   }
   public List<String> getPath2(String source, String dest){
       DijkstraShortestPath solver;
        solver = new DijkstraShortestPath(this.graph,source,dest);
        return solver.getPath().getVertexList();
       //return solver.getShortestPathAsVertexList(source, dest);    
   }
   public  ArrayList<String> getPath(int r_src, int c_src, int r_tar, int c_tar) {
        
        String source = UtilsAgents.getKey(this.map[r_src][c_src]);
        String target = UtilsAgents.getKey(this.map[r_tar][c_tar]);
        ArrayList<String> result = (ArrayList<String>) this.getPath2(source, target);
        if (result == null) {
            return new ArrayList<>();
        } else {
            if (result.get(0).equals(UtilsAgents.getKey(this.map[r_tar][c_tar]))) {
                Collections.reverse(result);
            }
            return result;
        }
    }
}
