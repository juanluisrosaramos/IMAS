/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.GarbageInfo;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.HarvesterInfoAgent;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

    
/**
 * Behavior which receives all the movements of all the scouts and check if they are valid
 * @author carles
 */
public class ReceiveSimResponsesBehavior extends OneShotBehaviour{
    boolean SLOW_MOTION = false;
    final private List<StreetCell> occupied = Collections.synchronizedList(new ArrayList<StreetCell>()); 
    ReceiveSimResponsesBehavior(Agent myAgent) {
        super(myAgent);
    }

    @Override
    public void onStart() {
        super.onStart(); //To change body of generated methods, choose Tools | Templates.
        this.occupied.clear();
    }
    
    @Override
    public SystemAgent getAgent(){ return (SystemAgent)this.myAgent; }
    
    @Override
    public void action() {
        try {
            SystemAgent sa = getAgent();

            for(int i=sa.getMessagesSent(); i>0; i--){
                // receive all simulation finish message before start again
                ACLMessage msg = sa.blockingReceive(MessageTemplates.CHECK_MOVEMENT);

                updateMap(msg.getContentObject());
                
            }
            sa.updateGUI();

            if(SLOW_MOTION) Thread.sleep(100);

        } catch (UnreadableException | CloneNotSupportedException | InterruptedException ex) {
            Logger.getLogger(ReceiveSimResponsesBehavior.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public  void updateMap(Serializable contentObject) throws CloneNotSupportedException  {
        ArrayList al = (ArrayList) contentObject;
        StreetCell _sc1 = (StreetCell) al.get(0);
        StreetCell _sc2 = (StreetCell) al.get(1);
        
        Cell[][] map = this.getAgent().getGame().getMap();
        StreetCell sc1 = (StreetCell) map[_sc1.getRow()][_sc1.getCol()];
        StreetCell sc2 = (StreetCell) map[_sc2.getRow()][_sc2.getCol()];
        boolean movementOk = false;
        if(this.occupied.contains(sc2)){
            System.out.println("????");
        }
        else{
            //remove from sc1
            InfoAgent ia = sc1.getAgent(); //get agent to add into the new cell
            try{
                if(!sc2.isThereAnAgent()){
                    sc1.removeAgent(sc1.getAgent());
                    sc2.addAgent(ia);
                    this.occupied.add(sc2);
                    this.removeFromFSLists(sc1);
                    this.addToFSLists(sc2);
                    movementOk=true;
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                Logger.getLogger(ReceiveSimResponsesBehavior.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }

    private void removeFromFSLists(Cell c){
        SystemAgent sa = this.getAgent();
        if (sa.getScoutsCells().contains(c)){
            sa.getScoutsCells().remove(c);
        } else if (sa.getHarvestersCells().contains(c)){
            sa.getHarvestersCells().remove(c);
        }
    }

    private void addToFSLists(Cell c){
        SystemAgent sa = this.getAgent();
        StreetCell sc = (StreetCell)c;
        if(sc.getAgent().getType()==AgentType.HARVESTER){
            sa.getHarvestersCells().add(c);
        } else if(sc.getAgent().getType()==AgentType.SCOUT){
            sa.getScoutsCells().add(c);
        }
    }
}
