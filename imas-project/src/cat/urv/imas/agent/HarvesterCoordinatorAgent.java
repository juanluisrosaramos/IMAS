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
import cat.urv.imas.behaviour.harvester_coordinator.GarbageAuctionRequest;
import cat.urv.imas.behaviour.harvester_coordinator.PendingDetectedGarbage;

import cat.urv.imas.behaviour.shared.CheckMovementBehavior;
import cat.urv.imas.behaviour.shared.GarbageDetectionListener;
import cat.urv.imas.behaviour.shared.GarbageGatheredListener;
import cat.urv.imas.behaviour.shared.PathFinderBehavior;
import cat.urv.imas.behaviour.shared.RecyclingGarbageListener;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;

import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.PathFinder;
import jade.core.*;
import jade.domain.FIPANames;

import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Integer.max;
import static java.lang.Integer.min;


import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class HarvesterCoordinatorAgent extends ImasAgent implements IAgentWithMap, IMovementChecker, IGarbageListener{
    ArrayList<SettableBuildingCell> pendingGarbage= new ArrayList<>();

    public ArrayList<SettableBuildingCell> getFailedDetectedGarbage() {
        return pendingGarbage;
    }

    public void setFailedDetectedGarbage(ArrayList<SettableBuildingCell> failedDetectedGarbage) {
        this.pendingGarbage = failedDetectedGarbage;
    }
    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID coordinatorAgent;
    private CheckMovementBehavior movementChecker;
    private boolean firstTime=true;
    
    /**
     * Builds the coordinator agent.
     */
    public HarvesterCoordinatorAgent() {
        super(AgentType.HARVESTER_COORDINATOR);
    }
    
    @Override
    protected void setupConcrete() {
        this.coordinatorAgent = this.searchAgent(AgentType.COORDINATOR);
        this.movementChecker = new CheckMovementBehavior(this);
        this.addBehaviour(this.movementChecker);
        this.addBehaviour(new MapRequestBehavior(this, 
                MessageProtocol.getRequestMsg(MessageContent.GET_MAP, this.getSA()))); 
        this.addBehaviour(new GarbageDetectionListener(this));
        this.addBehaviour(new GarbageGatheredListener(this));
        this.addBehaviour(new RecyclingGarbageListener(this));
        this.addBehaviour(new PendingDetectedGarbage(this,30000));
    }
    
    @Override
    public AID myCoordinator() {
        return this.coordinatorAgent;
    }
    
    @Override
    public void simulationStep(Object o){
        this.movementChecker.restart();
    }
    
    public ArrayList<AID> getHarvesterAID() {
        ArrayList<AID> result = new ArrayList<AID>();
        if(this.game==null) return result;
        List<Cell> harv_cells= this.game.getAgentList().get(AgentType.HARVESTER);
        for(Cell cell: harv_cells){
            AID aid = ((StreetCell)cell).getAgent().getAID();
            result.add(aid);
        }
        return result;
    }
    
    PathFinder finder; 
    
    @Override
    public void setGame(GameSettings game) {
        this.game = game;
        if(firstTime){
            this.addBehaviour(new PathFinderBehavior(this,this.game.getMap()));
            firstTime = false;
        };
        finder = new PathFinder(getGame().getMap());
    }

    @Override
    public GameSettings getGame() {
        return this.game;
    }

    @Override
    public void onDetectedGarbage(ArrayList<Cell> detectedGarbage) {
        for(Cell garbage:detectedGarbage){
            ACLMessage msg = getAuctionACLMessage(garbage);

            addBehaviour(new GarbageAuctionRequest(this,msg, (SettableBuildingCell) garbage, this.getHarvesterAID().size()));
        }
    }  

    public ACLMessage getAuctionACLMessage(Cell garbage) {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION);
        msg.setConversationId(MessageProtocol.GARBAGE_REQUEST);
        // We want to receive a reply in 5 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
        for (AID aid: getHarvesterAID()) {
            msg.addReceiver(aid);
        }
        try {
            msg.setContentObject((Serializable) garbage);
        } catch (IOException ex) {
            Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

    @Override
    public void onLeaveCell(StreetCell cell) {
        
    }

    @Override
    public void onGatherGarbage(AID sender, SettableBuildingCell buildingWithGarbage, int y, int x, GarbageType gt, int quantity) {
        ACLMessage msg = MessageProtocol.getInformMsg(MessageProtocol.RC_PATH, sender);
        //ArrayList<String> path = getBestRC(y,x,gt,quantity);
        ArrayList<String> path = null;
        ArrayList<Object> ans  = getBestRC(y,x,gt,quantity,path); 
        //0: path, 1: recycling center
        try {
            //msg.setContentObject(path);
            msg.setContentObject(ans);
            this.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private ArrayList<Object> getBestRC(int y, int x, GarbageType currentGarbageType, int quantity,ArrayList<String> path) {
        //get list of recyc centers. i dont know how to acces this SystemAgent method i implemented properly
        ArrayList<RecyclingCenterCell> listOfRecycCenters = getGame().getRecyclingCenterList();
        
        //get prices of different recyc centers
        //prices of recycling plastic, glass and paper, respectively.
        int[] garbPrices = new int[listOfRecycCenters.size()];
        for (int i = 0; i < listOfRecycCenters.size(); i++) {
            int[] garbagePrices = listOfRecycCenters.get(i).getPrices();           
            if (null!=currentGarbageType)
                switch (currentGarbageType) {
                case PLASTIC:
                    garbPrices[i] = (garbagePrices[0]);
                    break;
                case GLASS:
                    garbPrices[i] = (garbagePrices[1]);
                    break;
                case PAPER:
                    garbPrices[i] = (garbagePrices[2]);
                    break;
                default:
                    break;
            }
                
        }
        
        //get paths and lengths of path to recycling centers
        ArrayList<ArrayList<String>> paths = new ArrayList<>();
        int[] pathDistances = new int[listOfRecycCenters.size()];
        ArrayList<StreetCell> targetStreet=new ArrayList<>();
        for (int i = 0; i < listOfRecycCenters.size(); i++){
            RecyclingCenterCell recyc = listOfRecycCenters.get(i);
            StreetCell building = getMinStreetCell(y,x,recyc.getRow(),recyc.getCol());
            targetStreet.add(building);
            if(!(getGame().get(y, x) instanceof StreetCell)){
                System.out.println("---");
            }
            //ArrayList<String>  path = finder.getPath( y, x, building.getRow(),building.getCol());
            path = finder.getPath( y, x, building.getRow(),building.getCol());
            paths.add(path);
            pathDistances[i] =  path.size();
        }
        //get price per unit of length for each recyc center and record index of highest
        
        List pathValues = new ArrayList();
        int maxValIndex = 0;
        double maxVal = 0;
        
        for (int i = 0; i< listOfRecycCenters.size(); i++){
            
            double pathval = garbPrices[i]/pathDistances[i];
            pathValues.add(pathval);
            if (pathval>maxVal){
                maxValIndex = i;
                maxVal = pathval;
            }
        }
        ArrayList<Object> result = new ArrayList<>();
        result.add(paths.get(maxValIndex));
        result.add(listOfRecycCenters.get(maxValIndex));
        result.add(targetStreet.get(maxValIndex));
        return result; 
    }
    
    private StreetCell getMinStreetCell(int srcY,int srcX, int tarY, int tarX){
        ArrayList<StreetCell> candidates=new ArrayList<>();

        for(int i=tarY-1;i<tarY+2;i++){
            for(int j=tarX-1;j<tarX+2;j++){
                if((i==tarY) && (j==tarX)){}
                else{
                    int ii = min(max(i,0),getGame().getMap().length-1);
                    int jj = min(max(j,0),getGame().getMap()[0].length-1);
                    if(getGame().get(ii, jj) instanceof StreetCell){
                        candidates.add((StreetCell)getGame().get(ii,jj));
                    }
                }
            }
        }
        StreetCell min=null;
        long minDistance = Integer.MAX_VALUE;
        for(StreetCell candidate:candidates){
            long currDistance = (long) UtilsAgents.manhattanDistance(candidate.getCol(), srcX, candidate.getRow(), srcY);
            if( currDistance < minDistance){
                minDistance = currDistance;
                min = candidate;
            }
        }
        return min;
    }

    @Override
    public void onRecyclingGarbage(RecyclingCenterCell rec, int qty, SettableBuildingCell sourceBuildingWithGarbage) {
        
    }

    public void addToPendingGarbage(SettableBuildingCell detectedGarbage) {
        if(!pendingGarbage.contains(detectedGarbage)){
            this.pendingGarbage.add(detectedGarbage);
        }
    }

    public void removeFromPendingGarbage(SettableBuildingCell detectedGarbage) {
        if(pendingGarbage.contains(detectedGarbage)){
            pendingGarbage.remove(detectedGarbage);
        }
    }
}
