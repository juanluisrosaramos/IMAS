/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scout_coordinator;

import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.SettableBuildingCell;
import jade.core.AID;
import jade.core.Agent;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Behavior to manage a ContractNet messages for a building to be scouted
 * @author carles
 */

public class CNScoutingRequest extends jade.proto.ContractNetInitiator{
    
    private final SettableBuildingCell building;
    private int nResponders;
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)this.myAgent;
    }
    public CNScoutingRequest(Agent a, ACLMessage cfp, SettableBuildingCell building, int expectedResponders ) {
        super(a, cfp);
        this.nResponders = expectedResponders;
        this.building = building;
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        getAgent().log("CN Agent "+propose.getSender().getName()+" proposed ");
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        getAgent().log("CN Agent "+refuse.getSender().getName()+" refused");
    }
				
    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            getAgent().log("Responder does not exist");
        }
        else {
            getAgent().log("CN Agent "+failure.getSender().getName()+" failed");
        }
        
        // Immediate failure --> we will not receive a response from this CN agent
        nResponders--;
    }
    
    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            getAgent().log("Timeout expired: missing "+(nResponders - responses.size())+" responses");
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
                int[] position=null;
                try {
                    position = (int[]) msg.getContentObject();
                } catch (UnreadableException ex) {
                    Logger.getLogger(CNScoutingRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (closer(position,bestProposal)) {
                    bestProposal = position;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            getAgent().log("CN Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
            try {
                accept.setContentObject(new int[]{this.building.getRow(),this.building.getCol()});
            } catch (IOException ex) {
                Logger.getLogger(CNScoutingRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }						
    }
				
    @Override
    protected void handleInform(ACLMessage inform) {
        getAgent().log("CN Agent "+inform.getSender().getName()+" successfully performed the requested action");
    }
    private boolean closer(int[] position, int[] bestProposal) {
        if(position==null) return false;
        if(bestProposal==null) return true;
        double d1 = UtilsAgents.manhattanDistance(position[0], building.getRow(), position[1], building.getCol());
        double d2 = UtilsAgents.manhattanDistance(bestProposal[0], building.getRow(), bestProposal[1], building.getCol());
        return d1 < d2;
    }
}
