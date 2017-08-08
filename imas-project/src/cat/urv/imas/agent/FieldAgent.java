/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import static java.lang.Math.abs;
import java.util.ArrayList;


/**
 *
 * @author carles
 */
public abstract class FieldAgent extends ImasAgent{
    
    private int successCounter=0; //count how many times reach a target building
    public int myPathIterator;

    public int getSuccessCounter() {
        return successCounter;
    }

    public int getFailureCounter() {
        return failureCounter;
    }
    private int failureCounter=0; //count how many times doesn't reach a target building
    private int targetX=-1;
    private int targetY=-1;
    
    protected StreetCell currentCell;

    public void setCurrentCell(StreetCell currentCell) {
        this.currentCell = currentCell;
    }
    public ArrayList<String> myPath;
    
    public  boolean hasTargetBuilding(){
        return (targetX!=-1) || (targetY!=-1);
    }
    
    public FieldAgent(AgentType type) {
        super(type);
    }
        
    public  void setTargetCell(int x, int y) throws java.lang.Exception {
        if(hasTargetBuilding()){
            throw new Exception("Is not possible set a target to an agent with already target");
        }
        log("FA: Set target: " + y + " , " + x);
        this.targetX = x;
        this.targetY = y;

    }

    protected void setNewPath(ArrayList<String> path) {
        this.myPath = path;
        if(myPath.size()>0){
            log("Following path=>"+myPath);
        }
        
        this.myPathIterator = -1;
    }

    protected ArrayList<Integer> getPathParameters() {
        ArrayList<Integer> position = new ArrayList<>();
        position.add(this.currentCell.getRow());
        position.add(this.currentCell.getCol());        
        position.add(this.targetY);
        position.add(this.targetX);
        return position;
    }

    public StreetCell nextPathCell() {
        this.myPathIterator +=1;
        if(myPathIterator >= myPath.size()){
            this.resetTarget();
            return null;
        }
        else{
            return UtilsAgents.keyToCell(myPath.get(myPathIterator));
        }
    }

    public  int getTargetX(){ return this.targetX;}
    public  int getTargetY(){ return this.targetY;}
    
    public void setScope(Cell[][] sc){
        this.currentCell = (StreetCell)sc[1][1];
        if(this.hasTargetBuilding()){
            if(isTargetReached(sc)){
                log("FA: Reached: " + this.targetY + "," + this.targetX+"!!");
                this.updateReachedCounter(true);
                int tmpX = this.targetX;
                int tmpY = this.targetY;
                
                this.onReachTarget(sc,tmpY,tmpX);
            }
        }    
    }

    private boolean isTargetReached(Cell[][] sc) {
    //given an scope of cells 
        
        Cell c = sc[1][1]; 
        
        //Too far in X component
        if(abs(targetX-c.getCol())>1) return false;
        //Too far in Y component
        if(abs(targetY-c.getRow())>1) return false;
        
        boolean found = false;
        for(int i=0; i<sc.length && !found;i++){
            for(int j=0; j<sc[0].length && !found;j++){
                boolean valid_targets = 
                        (sc[i][j] instanceof SettableBuildingCell) 
                        || (sc[i][j] instanceof RecyclingCenterCell);
                if(valid_targets){
                    found = this.targetX==sc[i][j].getCol() && this.targetY==sc[i][j].getRow();
                }
            }
        }
        return found;
    }
    
    public  void resetTarget() {
        this.targetX = -1;
        this.targetY = -1;
    }
    
    public int getCurrentX(){
        return this.currentCell.getCol();
    }
    public int getCurrentY(){
        return this.currentCell.getRow();
    }

    public void updateReachedCounter(boolean success) {
        if(success){
            this.successCounter +=1;
        }
        else{
            this.failureCounter +=1;
        }
    }
    
    public abstract Serializable getStatistics();


    protected void onReachTarget(Cell[][] scope,int targetY, int targetX) {

    }

    public void undoPathMovement() {
        this.myPathIterator -=1;
    }
    
}
