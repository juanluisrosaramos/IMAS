/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.shared;

import cat.urv.imas.agent.IGarbageListener;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
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
public class RecyclingGarbageListener extends CyclicBehaviour{
    public RecyclingGarbageListener(Agent a) {
        super(a);
    }
    
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)myAgent;
    }

    @Override
    public void action() {
        ACLMessage msg = getAgent().receive(MessageTemplates.RECYCLING_GARBAGE);
        if(msg ==null){
            this.block();
        }
        else{
            msg.removeReceiver(getAgent().getAID());
            try {
                ArrayList<Object> params = (ArrayList<Object>) msg.getContentObject();
                RecyclingCenterCell rec = (RecyclingCenterCell) params.get(0);
                int qty = (int) params.get(1);
                SettableBuildingCell sourceBuildingWithGarbage = (SettableBuildingCell) params.get(2);
                if(getAgent() instanceof IGarbageListener){
                    ((IGarbageListener)getAgent()).onRecyclingGarbage(rec,qty,sourceBuildingWithGarbage);
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
