/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.shared;

import cat.urv.imas.agent.IGarbageListener;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Context: when garbage is detected a message is raised (with a list of all detected garbage
 *  this behavior is added to SystemAgent (to update the map) and to HarvesterCoordinator (to start auctions)
 * @author carles
 */
public class GarbageDetectionListener extends CyclicBehaviour{

    public GarbageDetectionListener(Agent a) {
        super(a);
    }
    
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)myAgent;
    }

    @Override
    public void action() {
        ACLMessage msg = getAgent().receive(MessageTemplates.GARBAGE_DETECTION);
        if(msg ==null){
            //xthis.block();
        }
        else{
            msg.removeReceiver(getAgent().getAID());
            try {
                ArrayList<Cell> detectedGarbage;
                detectedGarbage = (ArrayList<Cell>) msg.getContentObject();
                if(getAgent() instanceof IGarbageListener){
                    ((IGarbageListener)getAgent()).onDetectedGarbage(detectedGarbage);
                }
            } catch (UnreadableException ex) {
                Logger.getLogger(GarbageDetectionListener.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            if(getAgent().myCoordinator()!=getAgent().getAID()){
                msg.addReceiver(getAgent().myCoordinator());
                getAgent().send(msg);
            }
        }
    }
}
