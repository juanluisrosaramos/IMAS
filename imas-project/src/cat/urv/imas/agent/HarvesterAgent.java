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

import cat.urv.imas.behaviour.field_agents.PerformMovementBehavior;
import cat.urv.imas.behaviour.harvester.GarbageAuctionResponse;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.HarvesterInfoAgent;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class HarvesterAgent extends FieldAgent {
    
    private AID harvesterCoordinatorAgent;
    private PerformMovementBehavior movementBehavior;
    private int currentCapacity = -1;
    private boolean recycling=false;
    private RecyclingCenterCell recyclingCenter;
    private int carried_qty; //qty carrying at this moment
    private SettableBuildingCell source_building; //building with garbage i'm carried on

    @Override
    public boolean hasTargetBuilding() {
        boolean r= super.hasTargetBuilding(); //To change body of generated methods, choose Tools | Templates.
        return r;
    }

    @Override
    public StreetCell nextPathCell() {
        StreetCell s= super.nextPathCell(); //To change body of generated methods, choose Tools | Templates.
        return s;
    }

    public int getCapacity() {
        if(currentCapacity==-1){
            currentCapacity = ((HarvesterInfoAgent)this.currentCell.getAgent()).getCapacity();
        }
        return currentCapacity;
    }
    
    public int setCapacity(int capacity) {
        return ((HarvesterInfoAgent)this.currentCell.getAgent()).getCapacity();
    }

    public Map<GarbageType,Integer> getSupportedGarbage() {
        return supportedGarbage;
    }


    public void setSupportedGarbage(Map<GarbageType,Integer> supportedGarbage) {
        this.supportedGarbage = supportedGarbage;
    }
    private Map<GarbageType,Integer> supportedGarbage;
    
    @Override
    public AID myCoordinator() {
        return this.harvesterCoordinatorAgent;
    } 

    /**
     * Builds the coordinator agent.
     */
    public HarvesterAgent() {
        super(AgentType.HARVESTER);
    }
    
    @Override
    protected void setupConcrete() {
        Object[] args = getArguments();
        this.setCurrentCell((StreetCell)args[0]);
       
        this.supportedGarbage = new HashMap<>();
        GarbageType[] supportedGarbage = ((HarvesterInfoAgent)currentCell.getAgent()).getAllowedTypes();
        for(GarbageType sg:supportedGarbage){
            this.supportedGarbage.put(sg, this.getCapacity());
        }
        
        this.harvesterCoordinatorAgent = this.searchAgent(AgentType.HARVESTER_COORDINATOR);
        this.movementBehavior = new PerformMovementBehavior(this);
        this.addBehaviour(new GarbageAuctionResponse(this, MessageTemplates.GARBAGE_REQUEST));
    }
    
    @Override
    public void simulationStep(Object o){
        ArrayList data = (ArrayList) o;
        Cell[][] scope = (Cell[][])data.get(0);
        this.movementBehavior.setScope(scope);
        this.addBehaviour(movementBehavior); //add each time because oneshotbehavior
    }

    @Override
    public void setTargetCell(int x, int y) throws Exception {
        super.setTargetCell(x, y); //To change body of generated methods, choose Tools | Templates.
        if(!this.recycling){ //if recycling we already have the path
            //requests to ask the optimal path
            ACLMessage req = MessageProtocol.getRequestMsg(MessageProtocol.PATH_REQUEST,this.myCoordinator());
            req.setContentObject(getPathParameters());
            this.send(req);
            ACLMessage ans = this.blockingReceive(MessageTemplates.PATH_RESPONSE);
            ArrayList<String> path = (ArrayList<String>) ans.getContentObject();
            setNewPath(path);
        }
    }

    @Override
    public Serializable getStatistics() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean supportsGarbageType(GarbageType gt) {
        if(gt==null) return false;
        return (getSupportedGarbage().containsKey(gt)); 
    }
    
    @Override
    protected void onReachTarget(Cell[][] scope,int targetY, int targetX) {
        if(this.recycling){
            try {
                ACLMessage msg = MessageProtocol.getInformMsg(MessageProtocol.RECYCLING_GARBAGE, this.myCoordinator());
                ArrayList<Object> a = new ArrayList<>();
                a.add(this.recyclingCenter);
                a.add(carried_qty);
                a.add(source_building);
                msg.setContentObject(a);
                this.send(msg);
                this.carried_qty = 0;
                this.recyclingCenter = null;
                this.recycling = false;
                this.resetTarget(); //will get new auctions
            } catch (IOException ex) {
                Logger.getLogger(HarvesterAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{ 
        //if not recycling then has reached because has arrived to a building to gather garbage (winned an auction that lead here)
            //capture the buildin with garbage i'm going to carry
            source_building = this.getGarbageToGather(scope,targetY, targetX);
            //#units of garbage to pick
            Map<GarbageType, Integer> m = source_building.getGarbage();
            GarbageType k = (GarbageType)m.keySet().toArray()[0];
            int garbage_units = m.get(k);
            carried_qty = min(garbage_units,this.getCapacity());   
            ArrayList<Object> a = new ArrayList<>();

            try{   
                ACLMessage msg = MessageProtocol.getInformMsg(MessageProtocol.GATHERED_GARBAGE, this.myCoordinator());
                a.add(source_building);
                a.add(carried_qty);
                a.add(scope[1][1].getRow());
                a.add(scope[1][1].getCol());
                a.add(k);
                msg.setContentObject( a);
                this.send(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            /* wait for a path to some recycling center (it hasn't sent a explicit request
            *  but is triggered by the previous message
            */
            ACLMessage msg2 = this.blockingReceive(MessageTemplates.RECYCLING_CENTER_PATH);
            try {
                //ArrayList<String> path = (ArrayList<String>) msg2.getContentObject();
                ArrayList<Object> ans = (ArrayList<Object>) msg2.getContentObject();
                ArrayList<String> path = (ArrayList<String>) ans.get(0);
                StreetCell sc = (StreetCell) ans.get(2);
                this.setNewPath(path);
                
                recyclingCenter = (RecyclingCenterCell) ans.get(1);
                this.recycling = true;
                
                this.resetTarget();
                this.setTargetCell(recyclingCenter.getCol(),recyclingCenter.getRow());
                
            } catch (UnreadableException ex) {
                Logger.getLogger(HarvesterAgent.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            } catch (Exception ex) {
                Logger.getLogger(HarvesterAgent.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    } 

    private SettableBuildingCell getGarbageToGather(Cell[][] scope, int targetY, int targetX) {
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(scope[i][j] instanceof SettableBuildingCell){
                    SettableBuildingCell building=(SettableBuildingCell)scope[i][j];
                    if((targetX==building.getCol()) && (targetY==building.getRow())){
                        return building;
                    }
                }
            }
        }
        return null;
    }
    
    private SettableBuildingCell getRecyclingCenter(Cell[][] scope, int targetY, int targetX) {
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(scope[i][j] instanceof SettableBuildingCell){
                    SettableBuildingCell building=(SettableBuildingCell)scope[i][j];
                    if((targetX==building.getCol()) && (targetY==building.getRow())){
                        return building;
                    }
                }
            }
        }
        return null;
    }

}
