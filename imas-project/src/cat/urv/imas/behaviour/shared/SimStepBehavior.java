/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.shared;

import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Behavior is added to all ImasAgent except the SystemAgent
 * Receive the simulation TICK message and call to method simulationStep
 *  to be implemented by each class of agent
 * 
 * @author carles
 */
public class SimStepBehavior extends SimpleBehaviour{

    @Override
    public void action() {
        ImasAgent ia = (ImasAgent) this.getAgent();

        ACLMessage msg = ia.receive(MessageTemplates.SIM_STEP_TEMPLATE);
        if(msg!=null)  {
            try {
                ia.simulationStep(msg.getContentObject());                     
            } catch (UnreadableException ex) {
                ex.printStackTrace();
                Logger.getLogger(ImasAgent.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        else{
            block();
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
