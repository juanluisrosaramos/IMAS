/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scout;

import cat.urv.imas.agent.ScoutAgent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Behavior to answer contracts for scoutings proposal (used bu scouts)
 * @author carles
 */
public class CNScoutingResponse extends ContractNetResponder{
    public static int MAX_TURNS_WITH_NO_ANWSER;
    private int turnsWithoutAnswering=0;


    public CNScoutingResponse(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
    @Override
    public ScoutAgent getAgent(){
        return (ScoutAgent)this.myAgent;
    }
    
    private String getLocalName(){
        return myAgent.getLocalName();
    }
    
    /**
     *
     * @param cfp
     * @return
     * @throws NotUnderstoodException
     * @throws RefuseException
     */
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        getAgent().log("CN Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName());
        
        if (this.recentlyAnswered()) {
            // We refuse to provide a proposal
            getAgent().log("CN Agent "+getLocalName()+": Refuse");
            throw new RefuseException("evaluation-failed");
        }
        else{
             int r = this.getAgent().getCurrentY();
             int c = this.getAgent().getCurrentX();
             int [] proposal = new int[]{r,c};
            // We provide a proposal
            getAgent().log("CN Agent "+getLocalName()+": Proposing " + proposal);
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            try {
                propose.setContentObject(proposal);
            } catch (IOException ex) {
                Logger.getLogger(CNScoutingResponse.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            return propose;
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        try {
            getAgent().log("CN Agent "+getLocalName()+": Proposal accepted");
            int[] a=(int[]) accept.getContentObject();

            if (performAction(a[0],a[1])) {
                getAgent().log("CN Agent "+getLocalName()+": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else {
                getAgent().log("CN Agent "+getLocalName()+": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        } catch (Exception ex) {
            Logger.getLogger(CNScoutingResponse.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        getAgent().log("CN Agent "+getLocalName()+": Proposal rejected");
    }

    private boolean performAction(int y, int x) throws Exception {
        getAgent().setTargetCell(x,y);
        getAgent().log("CN: going to:" + y +","+x);
        turnsWithoutAnswering =0;
        return true;
    }

    private boolean recentlyAnswered() {
        if (!getAgent().hasTargetBuilding()){ //Am i going to scout a new area?
            return false;
        }
        else{
            this.turnsWithoutAnswering +=1;
            boolean max_reached =  turnsWithoutAnswering > MAX_TURNS_WITH_NO_ANWSER;
            if(max_reached){
                getAgent().log("FA: reseting target not reached:"+ getAgent().getTargetY()+","+getAgent().getTargetX());
                getAgent().resetTarget();
                getAgent().updateReachedCounter(false);
            }
            return !max_reached;
        }
    }
}
