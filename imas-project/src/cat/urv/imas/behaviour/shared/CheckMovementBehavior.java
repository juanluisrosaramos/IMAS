package cat.urv.imas.behaviour.shared;

/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import cat.urv.imas.agent.IMovementChecker;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Behavior that:
 *   1. receive (wait) check_movement message
 *   2. check if a movement is valid or not.
 *   3. propagate the message to the superior coordinator
 * @author carles
 */
public class CheckMovementBehavior extends SimpleBehaviour {
    final private List<Cell> occupied = Collections.synchronizedList(new ArrayList<Cell>()); 
    
    
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)myAgent;
    }

    public CheckMovementBehavior(Agent a) {
        super(a);   
    }
    
    @Override
    public void restart() {
        this.occupied.clear();
        super.restart(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void action() {
        ACLMessage msg = getAgent().receive(MessageTemplates.CHECK_MOVEMENT);
        if(msg==null){
            this.block();
        }
        else{
            msg.removeReceiver(getAgent().getAID());
            
            try {
                ArrayList<Cell> al = (ArrayList<Cell>) msg.getContentObject();

                StreetCell currentCell = (StreetCell) al.get(0);
                StreetCell newCell     = (StreetCell) al.get(1);
                
                if(!this.occupied.contains(newCell)){
                    this.occupied.add(newCell);
                } 
                else{ // collision
                    getAgent().log("<COLLISION DETECTED> currentCell: "+currentCell+" newCell: "+ newCell);
                    //prevent movement
                    al.remove(newCell); 
                    al.add(currentCell);
                    this.occupied.add(currentCell);
                }
                if(getAgent().myCoordinator()!=getAgent().getAID()){
                    msg.setContentObject(al);
                    msg.addReceiver(getAgent().myCoordinator());
                    getAgent().send(msg);
                }
                
                if(getAgent() instanceof IMovementChecker){
                    ((IMovementChecker)getAgent()).onLeaveCell(currentCell);
                }

            } catch (UnreadableException ex) {
                Logger.getLogger(CheckMovementBehavior.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            } catch (IOException ex) {
                Logger.getLogger(CheckMovementBehavior.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
