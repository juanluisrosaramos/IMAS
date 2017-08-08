/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.MapRequestBehavior;
import cat.urv.imas.behaviour.scout_coordinator.PrioriseScouting;
import cat.urv.imas.behaviour.shared.CheckMovementBehavior;
import cat.urv.imas.behaviour.shared.PathFinderBehavior;
import cat.urv.imas.behaviour.shared.RequestBehaviorBase;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;

import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.HarvesterInfoAgent;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageProtocol;

import java.util.ArrayList;
import java.util.List;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Integer.min;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class ScoutCoordinatorAgent extends ImasAgent implements IAgentWithMap, IMovementChecker {
        
    //map of k: "row;col", v:last visited time
    final private HashMap<String,Integer> visitedBuildings = new HashMap<>();

    private CheckMovementBehavior movementChecker;
    private int simStep = 0;
    
    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID coordinatorAgent;

    final private Map<String, GarbageInfo> garbageDetected=new ConcurrentHashMap<>();
    private boolean firstTime=true;
    private AID harvesterCoordinatorAgent;
    
    public void addDetectedGarbage(SettableBuildingCell building, GarbageInfo gi){
        this.garbageDetected.put(UtilsAgents.getKey(building), gi);
    }
    
    public int updateDetectedGarbage(SettableBuildingCell building, Integer capacity){
        String building_key = UtilsAgents.getKey(building);
        GarbageInfo gi = this.garbageDetected.get(building_key);
        int removed = min(gi.getGarbageQty(),capacity);
        int new_qty = gi.getGarbageQty()-removed;
        if(new_qty==0){
            this.garbageDetected.remove(building_key);
        }
        else{
            gi.setGarbageQty(new_qty);
            this.garbageDetected.put(building_key, gi);
        }
        for(int i = removed;i>0;i--){
            building.removeGarbage();
        }
        return removed;
    }
    
    public boolean alreadyDiscovered(SettableBuildingCell building) {
        return this.garbageDetected.containsKey(UtilsAgents.getKey(building));
    }
    
    public Map<String, GarbageInfo> getDetectedGarbage() {
        return this.garbageDetected;
    }

    /**
     * Builds the coordinator agent.
     */
    public ScoutCoordinatorAgent() {
        super(AgentType.SCOUT_COORDINATOR);
    }

   @Override
    protected void setupConcrete() {
        this.coordinatorAgent = this.searchAgent(AgentType.COORDINATOR);
        this.harvesterCoordinatorAgent = this.searchAgent(AgentType.HARVESTER_COORDINATOR);
        movementChecker = new CheckMovementBehavior(this);
        this.addBehaviour(new MapRequestBehavior(this, 
                MessageProtocol.getRequestMsg(MessageContent.GET_MAP, this.getSA())));
        this.addBehaviour(movementChecker);
        
    }
    
    @Override
    public void setGame(GameSettings game) {
        this.game = game;
        this.visitedBuildings.clear();
        //initialize the array of visited buildings to priorise the scouting
        for(int i=0;i<game.getMap().length;i++){
            for(int j=0;j<game.getMap()[0].length;j++){
                if(game.get(i, j) instanceof SettableBuildingCell){
                    this.visitedBuildings.put(UtilsAgents.getKey(game.get(i, j)), 0);
                }
            }
        }
        if(firstTime){
            this.addBehaviour(new PathFinderBehavior(this,this.game.getMap()));
            firstTime = false;
        }
        
    }

    @Override
    public GameSettings getGame() {
        return this.game;
    }
    
    @Override
    public AID myCoordinator() {
        return this.coordinatorAgent;
    }
    
    @Override
    public void simulationStep(Object o){
        ArrayList<Object> params = (ArrayList<Object>) o;
        this.setGame((GameSettings) params.get(1));
        simStep = (Integer)params.get(0);
        
        this.movementChecker.restart();
        this.addBehaviour(new PrioriseScouting(this,simStep)); //oneshot
    }

    public ArrayList<AID> getScoutsAID() {
        ArrayList<AID> result = new ArrayList<>();
        List<Cell> scout_cells= this.game.getAgentList().get(AgentType.SCOUT);
        for(Cell cell: scout_cells){
            AID aid = ((StreetCell)cell).getAgent().getAID();
            result.add(aid);
        }
        return result;
    }

    @Override
    public void onLeaveCell(StreetCell cell) {
        // Called when an agent leaves a cell 
        // It updates the hash that holds last times that a building has been
        // visited
        if (this.game==null){ //not received yet
            return;
        }
        updateVisitedBuildings(cell);
        detectGarbage(cell);
    }

    private void updateVisitedBuildings(StreetCell cell) {
        for(int i = cell.getRow()-1; i<cell.getRow()+2;i++){
            for(int j = cell.getCol()-1; j<cell.getCol()+2;j++){
                if ((j!=cell.getCol()) && (i!=cell.getRow())){ 
                    if (this.game.get(i, j) instanceof SettableBuildingCell)
                    {   String k = UtilsAgents.getKey(this.game.get(i, j));
                    int time = this.visitedBuildings.get(k) + 1;
                    this.visitedBuildings.put(k,time);
                    }
                }
            }
        }
    }
    
    public ArrayList<SettableBuildingCell> getOldestVisitedBuildings(){
        ArrayList<SettableBuildingCell> result = new ArrayList<>(); 
        String k_min = null;
        for(String k:this.visitedBuildings.keySet()){
            if(k_min==null){
                k_min = k;
            }
            // recently visited?
            else if(this.visitedBuildings.get(k)<this.visitedBuildings.get(k_min)){
                k_min = k;
                result.clear();
                
                result.add(buildingFromKey(k_min));
            }
            else if(this.visitedBuildings.get(k).equals(this.visitedBuildings.get(k_min))){
                result.add(buildingFromKey(k));
            }
        }       
        return result;
    }
    
    public SettableBuildingCell buildingFromKey(String key){
        String[] rc = key.split(";");//rc[0]=Y,rc[1]=X
        return (SettableBuildingCell)this.getGame().get(Integer.parseInt(rc[0]), Integer.parseInt(rc[1]));
    }

    private void detectGarbage(StreetCell currentCell) {
        //because our implementation scouts don't receive the game but only the scope can't use
        // this given method, so that is coordinator the responsible for marking as detected the cells
        
        ArrayList<GarbageInfo> detectedGarbage = this.getGame().detectBuildingsWithGarbage(currentCell.getRow(), currentCell.getCol()); 
        if(detectedGarbage.size()>0){       
                ArrayList<Cell> discoveries = new ArrayList<>();
                for(GarbageInfo e:detectedGarbage){
                    SettableBuildingCell building = e.getCell();
                    if(!this.alreadyDiscovered(building)){
                        this.addDetectedGarbage(building, e);
                        //this.incGarbageStatistics(currentCell.getAgent().getAID());
                        discoveries.add(building);
                    }
                }
                
                if(discoveries.size()>0){
                    try{   
                        ACLMessage msg = MessageProtocol.getInformMsg(MessageProtocol.NEW_GARBAGE_DETECTED,this.harvesterCoordinatorAgent);
                        msg.setContentObject((Serializable) discoveries);
                        this.send(msg);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        }    
    }
}
