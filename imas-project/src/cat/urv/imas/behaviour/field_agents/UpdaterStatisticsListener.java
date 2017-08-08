/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.field_agents;

import cat.urv.imas.agent.FieldAgent;

import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends statistics when get the requests message
 * @author carles
 */
public class UpdaterStatisticsListener extends CyclicBehaviour {

    public UpdaterStatisticsListener(Agent a) {
        super(a);
    }

    
    public FieldAgent getAgent(){
        return (FieldAgent)super.getAgent();
    }
    
    @Override
    public void action() {
        ACLMessage m = getAgent().receive(MessageTemplates.STATISTICS_REQUESTS);
        if(m!=null){
            ACLMessage reply = m.createReply();
            try {
                reply.setContentObject(getAgent().getStatistics());
                reply.setPerformative(ACLMessage.INFORM);
                reply.setConversationId(MessageProtocol.STATISTICS_RESPONSE);
                getAgent().send(reply);
            } catch (IOException ex) {
                Logger.getLogger(UpdaterStatisticsListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
