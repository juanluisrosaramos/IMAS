/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.shared;

import cat.urv.imas.agent.IGarbageListener;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carles
 */
public class GarbageGatheredListener extends CyclicBehaviour{
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)myAgent;
    }
    public GarbageGatheredListener(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage msg = getAgent().receive(MessageTemplates.GATHERED_GARBAGE);
        if(msg ==null){
            //this.block();
        } 
        else{
            msg.removeReceiver(getAgent().getAID());
            try {
                ArrayList<Object> detectedGarbage;
                detectedGarbage = (ArrayList<Object>) msg.getContentObject();
                SettableBuildingCell building = (SettableBuildingCell) detectedGarbage.get(0);
                int qty = (int) detectedGarbage.get(1);
                int y = (int) detectedGarbage.get(2);
                int x = (int) detectedGarbage.get(3);
                GarbageType gt = (GarbageType) detectedGarbage.get(4);
                if(getAgent() instanceof IGarbageListener){
                    ((IGarbageListener)getAgent()).onGatherGarbage(msg.getSender(), building,y,x,gt,qty);
                }
                
            } catch (UnreadableException ex) {
                Logger.getLogger(GarbageDetectionListener.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            if(getAgent().myCoordinator()!=getAgent().getAID()){
                //Pass up to my coordinator 
                msg.addReceiver(getAgent().myCoordinator());
                getAgent().send(msg);
            }
        }
    }
}
