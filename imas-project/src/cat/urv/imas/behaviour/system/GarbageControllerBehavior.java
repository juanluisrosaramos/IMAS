/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageStatistic;
import cat.urv.imas.onthology.GarbageType;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author carles
 * 
 */
class GarbageControllerBehavior extends OneShotBehaviour {

    final private Random rnd = new Random();
    
    
    ArrayList<SettableBuildingCell> occupiedBuildings = new ArrayList<>();
    
    public GarbageControllerBehavior(Agent a) {
        super(a);
    }
    
    @Override
    public SystemAgent getAgent(){
        return (SystemAgent)this.myAgent;
    }
    
    private GarbageType randGarbageType(){
        int i = rnd.nextInt(4)+1;
        switch(i){
            case 1: return GarbageType.GLASS;
            case 2: return GarbageType.PAPER;
            case 3: return GarbageType.PLASTIC;
            default: return GarbageType.GLASS;
        }
    }
    
    @Override
    public void action() {
        if(rnd.nextInt(100)+1 < getAgent().getGame().getNewGarbageProbability()) {
            return;
        }
        
        if (reachMaxBuildingsWithGarbage()){
            return;
        }
        
        ArrayList<SettableBuildingCell> list = getAgent().getBuildingList();
        int trials = 3;
        SettableBuildingCell building;
        int MAX_GARBAGE = this.getAgent().getGame().getMaxAmountOfNewGargabe();
        boolean assignedGarbage = false;
        do{
            int i = (new Random()).nextInt(list.size());
            building = list.get(i);
            try{
                new GarbageStatistic(building.getRow(), building.getCol(), getAgent().getSim_step());
                building.setGarbage(randGarbageType(), rnd.nextInt(MAX_GARBAGE)+1);
                assignedGarbage = true;
            }
            catch (IllegalStateException ex){  } //nothing to do //nothing to do
            trials = trials - 1;
        } while( (trials>=0) && !assignedGarbage);
        if (assignedGarbage) occupiedBuildings.add(building);
    }
    

    
    private boolean reachMaxBuildingsWithGarbage() {
    // returns if the maximum buildings with garbage has been reach
        GameSettings game = this.getAgent().getGame();
        int counter = 0;
        for(int i=0;i<this.occupiedBuildings.size(); i++){
            SettableBuildingCell c = this.occupiedBuildings.get(i);
            // get the cell directly from the map (can be changed)
            SettableBuildingCell c2 = (SettableBuildingCell)game.get(c.getRow(), c.getCol());
            if (!c2.isFound()){
                counter +=1;
            }
            else{ // if is empty update the list
                this.occupiedBuildings.remove(c);
            }
        }
        return (counter > game.getMaxNumberBuildingWithNewGargabe());
    }
}
