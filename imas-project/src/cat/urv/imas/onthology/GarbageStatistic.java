/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.GarbageInfo;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;
import static java.lang.Integer.max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *class to track garbage stats
 * @author sean
 */
public class GarbageStatistic {
   private int rowLocation;
   private int colLocation;
   private int turnCreated;
   private int turnDiscovered;
   private int turnCollected;
   private int turnRecycled;
   
   
   
   static int totalGarbage;
   static int totalDiscoveredGarbage;
   static int totalCollectedGarbage;
   
   static int totalRecyclePoints = 0;
   static int currentPoints;
   
   //array to hold all garbage stat objects
   static ArrayList<GarbageStatistic> arrayOfTrackedGarbage = new ArrayList<GarbageStatistic>();
   
   //constructor adds garbage stat object to holder array
   public GarbageStatistic(int rowLocation, int colLocation, int turnCreated){
       this.colLocation = colLocation;
       this.rowLocation = rowLocation;
       this.turnCreated = turnCreated;
       arrayOfTrackedGarbage.add(this);
       totalGarbage++;
   }
   public int getRow(){
       return this.rowLocation;
   }
   
   public int getCol(){
       return this.colLocation;
   }
   
   public int getDiscoveryTime(){
       return max(turnCollected-turnDiscovered,0);
   }
   
   public int getCollectionTime(){
       return  max(turnDiscovered-turnCreated,0);
   }
   
   public static void setTurnDiscovered(int discoveryTurn, int rowDiscovery, int colDiscovery){
       for (int i = 0; i<arrayOfTrackedGarbage.size();i++){
           
           if (arrayOfTrackedGarbage.get(i).getRow()==rowDiscovery & arrayOfTrackedGarbage.get(i).getCol()==colDiscovery){
               arrayOfTrackedGarbage.get(i).turnDiscovered = discoveryTurn;
               totalDiscoveredGarbage++;
           }
       }
       
   }
      
   public static void setTurnCollected(int collectTurn, int rowCollected, int colCollected){
       for (int i = 0; i < arrayOfTrackedGarbage.size(); i++){
           if (arrayOfTrackedGarbage.get(i).getRow()==rowCollected & arrayOfTrackedGarbage.get(i).getCol()==colCollected){
               arrayOfTrackedGarbage.get(i).turnCollected = collectTurn;
               totalCollectedGarbage++;
           }
       }
   }

   public static void setTurnRecycled(int recycTurn, int recyclePoints, int rowCollected, int colCollected){
       for (int i = 0; i < arrayOfTrackedGarbage.size(); i++){
           if (arrayOfTrackedGarbage.get(i).getRow()==rowCollected & arrayOfTrackedGarbage.get(i).getCol()==colCollected){
               arrayOfTrackedGarbage.get(i).turnRecycled = recycTurn;
               totalCollectedGarbage++;
           }
       }
   }
   
   public static double averageDiscoveryTime(){
       double totalTime = 0;
       for (int i = 0; i < arrayOfTrackedGarbage.size(); i++){
           totalTime = totalTime + arrayOfTrackedGarbage.get(i).getDiscoveryTime();
       }
       return totalTime/arrayOfTrackedGarbage.size();
   }
   
   public static double averageCollectionTime(){
       double totalTime = 0;
       for (int i = 0; i < arrayOfTrackedGarbage.size(); i++){
           totalTime = totalTime + arrayOfTrackedGarbage.get(i).getCollectionTime();
       }
       return totalTime/arrayOfTrackedGarbage.size();
   }
   public static double ratioDiscoveredGarbage() { return totalDiscoveredGarbage/totalGarbage;}
   public static double ratioCollectedGarbage() { return totalCollectedGarbage/totalGarbage;}
}
