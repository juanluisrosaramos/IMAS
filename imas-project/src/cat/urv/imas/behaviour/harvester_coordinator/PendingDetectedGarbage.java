/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvester_coordinator;

import cat.urv.imas.agent.HarvesterCoordinatorAgent;
import cat.urv.imas.map.SettableBuildingCell;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.ArrayList;

/**
 * From time to time this behaviour raises as many auctions as detected garbage not processed
 * @author carles
 */
public class PendingDetectedGarbage extends TickerBehaviour {

    public PendingDetectedGarbage(Agent a, long period) {
        super(a, period);
    }

    @Override
    public HarvesterCoordinatorAgent getAgent() {
        return (HarvesterCoordinatorAgent) super.getAgent(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void onTick() {
        //clone because there can be removed by the GarbageAuctionRequest
        ArrayList<SettableBuildingCell> lst = (ArrayList<SettableBuildingCell>) getAgent().getFailedDetectedGarbage().clone();
        for(SettableBuildingCell pendingDetectedGarbage: lst){
            getAgent().addBehaviour(
                new GarbageAuctionRequest(
                        getAgent(),
                        getAgent().getAuctionACLMessage(pendingDetectedGarbage),
                        pendingDetectedGarbage, 
                        getAgent().getHarvesterAID().size())
                );
        }
    }
    
}
