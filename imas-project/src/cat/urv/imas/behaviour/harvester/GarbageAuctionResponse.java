/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvester;

import cat.urv.imas.agent.GarbageInfo;
import cat.urv.imas.agent.HarvesterAgent;
import cat.urv.imas.map.SettableBuildingCell;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carles
 */
public class GarbageAuctionResponse extends jade.proto.ContractNetResponder {
    private String getLocalName(){
        return myAgent.getLocalName();
    }
    
    public HarvesterAgent getAgent(){
        return (HarvesterAgent) myAgent;
    }
    
    public GarbageAuctionResponse(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName());
        SettableBuildingCell gi=null;
        try {
            gi = (SettableBuildingCell) cfp.getContentObject();
        } catch (UnreadableException ex) {
            Logger.getLogger(GarbageAuctionResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (((getAgent().hasTargetBuilding()) ||                              //not going for garbage
                (getAgent().getCapacity()==0)) ||                             //enough capacity
                (!getAgent().supportsGarbageType(gi.getGarbageType()))) {     //supports kind of garbage
            // We refuse if we have carrying garbage or type not supported by the agent
            getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": Refuse");
            throw new RefuseException("evaluation-failed");
        }
        else{
            int [] proposal = new int[]{this.getAgent().getCurrentY(),this.getAgent().getCurrentX(), this.getAgent().getCapacity()};
            // We provide a proposal
            getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": Proposing ");
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
            try {
                propose.setContentObject(proposal);
            } catch (IOException ex) {
                Logger.getLogger(GarbageAuctionResponse.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            return propose;
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        try {
            getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": Proposal accepted");
            int[] a=(int[]) accept.getContentObject();

            if (performAction(a[0],a[1])) {
                getAgent().log("CN Agent "+getLocalName()+": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else {
                getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        } catch (Exception ex) {
            Logger.getLogger(GarbageAuctionResponse.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new FailureException("unexpected-error");
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        getAgent().log("[AUCTION_ANS] Agent "+getLocalName()+": Proposal rejected");
    }

    private boolean performAction(int y, int x) throws Exception {
        //X,Y are the coordinates of the building to gather the garbage
        getAgent().setTargetCell(x,y);
        getAgent().log("[AUCTION_ANS]: going to:" + y +","+x);
        return true;
    }
    
}
