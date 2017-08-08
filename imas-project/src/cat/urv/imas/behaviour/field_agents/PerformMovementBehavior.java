/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.field_agents;

import cat.urv.imas.agent.FieldAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.agent.ScoutAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageProtocol;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * calculate next movement for a harvester/scout
 * @author carles
 */
public class PerformMovementBehavior extends OneShotBehaviour{
    private int dx, dy = 0;
    int N_VISITED_SIZE=4; //TODO: Study the effect of this parameter (1..N%)
    StreetCell[] visited = new StreetCell[N_VISITED_SIZE];
    int lastVisited = -1;    
    private Cell[][] scope;
    private boolean lastBest=false;
    
    public PerformMovementBehavior(Agent a) {
        super(a);
        this.myAgent = a;
    }
    
    public FieldAgent getAgent(){
        return (FieldAgent)this.myAgent;
    }
    
    //TODO: remove?
    private void addToVisited(StreetCell c) {
        this.lastVisited = (this.lastVisited+1) % N_VISITED_SIZE;
        this.visited[this.lastVisited] = c;
    }
    //TODO: remove?
    private boolean recently_visited(StreetCell c){         
        for(int i=this.lastVisited;i>-1;i--){

            if(this.visited[i].equals(c)){
                return true;
            }
        }
        return false;
    }
    
    public void setScope(Cell[][] scope) {
        getAgent().setScope(scope);
        this.scope = scope;
    }

    protected boolean canMove(Cell c){
        if(c==null) return false;
        boolean r=c.getCellType() == CellType.STREET;
        
        return r &&!((StreetCell)c).isThereAnAgent();
    }

    protected StreetCell randomSelection(ArrayList<StreetCell> candidates){
        StreetCell result=null;
        if(!candidates.isEmpty()){
            Random r = new Random();
            int r2=r.nextInt(candidates.size());
            result = candidates.get(r2);
        }
        return result;
    }
    
    protected StreetCell getNextCell(Cell[][] scope) throws Exception{
        ArrayList<StreetCell> candidates=new ArrayList<>();
        //NO direction => first movement, chose the first free ceel to move
        if((this.dx==0) && (this.dy==0)){
            addToCandidates(scope[0][1],candidates);
            addToCandidates(scope[1][1],candidates);
            addToCandidates(scope[1][0],candidates);
            addToCandidates(scope[1][0],candidates);
            
            return randomSelection(candidates);            
        }
        
        if(this.getAgent().hasTargetBuilding()){
           StreetCell c2 =  getAgent().nextPathCell();
           if(c2!=null) return c2;
        }
        Cell c=null;
        try{
            c = scope[1+this.dy][1+this.dx];//following both gradients
        } catch (Exception e){
            getAgent().log(String.format("ERROR %s %s", this.dy,this.dx));
        }
        if(!this.getAgent().hasTargetBuilding()){
            if(canMove(c)) return (StreetCell)c;//First try => Follow the direction the agent comes
        }
        
        addToCandidates(scope[0][1], candidates);//try UP
        addToCandidates(scope[2][1], candidates);//try DOWN
        addToCandidates(scope[1][0], candidates);
        addToCandidates(scope[1][2], candidates);
        /*if(this.dy==0){ //--> * || * <-- 
            addToCandidates(scope[0][1], candidates);//try UP
            addToCandidates(scope[2][1], candidates);//try DOWN
        }
        else if(this.dx==0){ 
            addToCandidates(scope[1][0], candidates);
            addToCandidates(scope[1][2], candidates);
        }
        */
        
        c = randomSelection(candidates);
        
        if(c!=null) {
            return (StreetCell)c;
        }
        else{
            c = scope[1-this.dy][1-this.dx];
            //undo way (sometimes for a weird situation (maybe dx,dy is not properly calculated) happens that is not a streetcell)
            if (c instanceof StreetCell){
                return (StreetCell)c;   
            }
            else{
                getAgent().log("OMG!");
                c = scope[1][1];
                return (StreetCell)c;   
            }
        }
    }    

    private void addToCandidates(Cell c, ArrayList<StreetCell> candidates) {
        if(canMove(c))  candidates.add(((StreetCell)c));
    }

    @Override
    public void action() {
        if(scope!=null){
            try {
                StreetCell currentCell = (StreetCell)scope[1][1];
                StreetCell newCell= getNextCell(scope); 
                //update the direction of the vectors
                if(newCell==null){ //TODO: IT shouldn't happen but happens with all the field scouts after a long time
                    newCell = currentCell;
                }
                this.dx = newCell.getCol() - currentCell.getCol();
                this.dy = newCell.getRow() - currentCell.getRow();
                if(abs(this.dy)>1 || abs(this.dx)>1){
                    getAgent().undoPathMovement();
                    //TODO: BUG if following route =>assumes can move but always..fix it
                    //System.out.println(String.format("ERR: %s %s\n%s, %s\n%s\nPATH: %s",currentCell.getRow(),currentCell.getCol(),newCell.getRow(),newCell.getCol(),this.getAgent().myPathIterator,this.getAgent().myPath));
                }
                //send message to coordinator
                sendMovementToMyCoordinator(currentCell,newCell); 
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(ScoutAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void sendMovementToMyCoordinator(StreetCell currentCell, StreetCell newCell) throws IOException, UnreadableException {
        //ACLMessage msg = RequestBehaviorBase.getRequestMsg(MessageContent.GET_NEXT_CELL, this.scoutCoordinatorAgent);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.clearAllReceiver();
        msg.addReceiver(this.getAgent().myCoordinator());
        msg.setProtocol(MessageProtocol.CHECK_CELL_MOVEMENT);

        ArrayList content = new ArrayList();
        content.add(currentCell);
        content.add(newCell);
        msg.setContentObject(content);
        this.myAgent.send(msg);
    }

}
