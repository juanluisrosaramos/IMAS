/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvester_coordinator;

import cat.urv.imas.agent.GarbageInfo;
import cat.urv.imas.agent.HarvesterCoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.behaviour.scout_coordinator.CNScoutingRequest;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carles
 */
public class GarbageAuctionRequest extends jade.proto.ContractNetInitiator{
    int nResponders = 0;
    
    SettableBuildingCell detectedGarbage;
    
    @Override
    public HarvesterCoordinatorAgent getAgent(){
        return (HarvesterCoordinatorAgent)myAgent;
    }

    public GarbageAuctionRequest(HarvesterCoordinatorAgent a, ACLMessage acceptance, SettableBuildingCell detectedGarbage,int expected_responders) {
        //detectedGarbage even being a Map ONLY contains one element (=one building). See usage
        super(a, acceptance);
        
        nResponders = expected_responders;
        this.detectedGarbage = detectedGarbage;
    }
    
    @Override
    protected void handleRefuse(ACLMessage refuse) {
        getAgent().log("AReq Agent "+refuse.getSender().getName()+" refused");
    }
				
    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            getAgent().log("Responder does not exist");
        }
        else {
            getAgent().log("AReq Agent "+failure.getSender().getName()+" failed");
        }
        
        // Immediate failure --> we will not receive a response from this CN agent
        nResponders--;
    }
    
    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            getAgent().log("AReq Timeout expired: missing "+(nResponders - responses.size())+" responses");
        }
        // Evaluate proposals.
        int[] bestProposal = null;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                int[] prop_params=null;
                try {
                    prop_params = (int[]) msg.getContentObject();
                } catch (UnreadableException ex) {
                    Logger.getLogger(CNScoutingRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (betterOffer(prop_params,bestProposal)) {
                    bestProposal = prop_params;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            getAgent().log("AReq Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
            try {
                accept.setContentObject(new int[]{detectedGarbage.getRow(),detectedGarbage.getCol()});
            } catch (IOException ex) {
                Logger.getLogger(CNScoutingRequest.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            getAgent().removeFromPendingGarbage(this.detectedGarbage);
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
        else {
            getAgent().addToPendingGarbage(this.detectedGarbage);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        getAgent().log("AReq Agent "+inform.getSender().getName()+" successfully performed the requested action");
    }
    
    private boolean betterOffer(int[] prop_params, int[] bestProposal) {
        if(prop_params==null) return false;
        if(bestProposal==null) return true;
        
        double d1 = UtilsAgents.manhattanDistance(prop_params[0], detectedGarbage.getRow(), prop_params[1], detectedGarbage.getCol());
        double d2 = UtilsAgents.manhattanDistance(bestProposal[0], detectedGarbage.getRow(), bestProposal[1], detectedGarbage.getCol());
        double capacity_a = 0.5*d1+0.5*prop_params[2];
        double capacity_b = 0.5*d2+0.5*bestProposal[2];
        return capacity_a > capacity_b;
    }
}
