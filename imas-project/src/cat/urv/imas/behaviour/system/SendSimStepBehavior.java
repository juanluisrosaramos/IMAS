/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;


import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageProtocol;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Behavior responsible from sending the simulation ticks (steps)
 * @author carles
 */
public class SendSimStepBehavior extends OneShotBehaviour{
    boolean SLOW_MOTION = false; 
    Integer conversationId = 0;
    
    public SendSimStepBehavior(Agent a) {
        super(a);
    }
    
    @Override
    public void action() {
        getAgent().next_step();
        //System.out.println("SA=>"+getAgent().getSim_step());
        /* TODO: uncomment
        if(ticks ==getAgent().getGame().getSimulationSteps()){
            getAgent().showStatistics("****SIMULATION FINISHED****");
        }
        else{*/
            try {
                this.getAgent().setMessagesSent(0);
                sendTickToScouts();
                sendTickToHarvesters();
                sendTickToCoordinators();
            } catch (IOException ex) {
                Logger.getLogger(SendSimStepBehavior.class.getName()).log(Level.SEVERE, null, ex);
            }
        //}
    }
    
    private Serializable getScope(StreetCell sc) {
        Cell[][] map = this.getAgent().getGame().getMap();

        int r = sc.getRow();
        int c = sc.getCol();

        final Cell[][] result = new Cell[][]{
            {null,null,null},
            {null,null,null},
            {null,null,null},
        };

        for(int i=0;i<3;i++){
            int r_ = r+i-1;
            if ((r_>=0) && (r_<NROWS())){
                for(int j=0;j<3;j++){                
                    int c_ = c+j-1;
                    if((c_>=0) && (c_<NCOLS())){
                        result[i][j] = map[r_][c_];
                    }
                }
            }
        }

        ArrayList al = new ArrayList();
        al.add(result);
        al.add(getAgent().getSim_step());
        return al;
    }

    @Override
    public SystemAgent getAgent(){ return (SystemAgent)this.myAgent; }
    private int NROWS(){return this.getAgent().getGame().getMap().length; }
    private int NCOLS(){return this.getAgent().getGame().getMap()[0].length; }
    private ACLMessage msgNextStep(Serializable data) throws IOException{
        this.conversationId +=1;
        ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
        msg.setProtocol(MessageProtocol.SIM_STEP);
        msg.setConversationId(this.conversationId.toString());
        if(data!=null){
            msg.setContentObject(data);
        }
        return msg;
    }
    
    private ACLMessage msgNextStep() throws IOException{ return this.msgNextStep(null);}
    
    private void sendTickToScouts() throws IOException {
        SystemAgent sa = this.getAgent();

        Cell[][] m0 = sa.getGame().getMap();

        for(Cell cell:sa.getScoutsCells()){
            StreetCell sc = (StreetCell) cell;
            if ((sc.getAgent()!=null) && (sc.getAgent().getAID()==null)){
                continue; //TODO: this is happening because we are not creating all the agents, remove this if when create everything
            }
            sendScope(sc);
        }
    }
    
    private void sendTickToHarvesters() throws IOException {
        SystemAgent sa = this.getAgent();

        Cell[][] m0 = sa.getGame().getMap();

        for(Cell cell:sa.getHarvestersCells()){
            StreetCell sc = (StreetCell) cell;
            if ((sc.getAgent()!=null) && (sc.getAgent().getAID()==null)){
                continue; //TODO: this is happening because we are not creating all the agents, remove this if when create everything
            }
            sendScope(sc);
        }
    }

    private void sendScope(StreetCell sc) throws IOException {
        ACLMessage msg = msgNextStep(getScope(sc));
        msg.addReceiver( sc.getAgent().getAID() );
        this.getAgent().send(msg);
        this.getAgent().setMessagesSent(this.getAgent().getMessagesSent()+1);
    }

    private void sendTickToCoordinators() throws IOException {
        ArrayList<Object> al = new ArrayList<>();
        al.add(getAgent().getSim_step());
        al.add(getAgent().getGame());
        ACLMessage msg = this.msgNextStep();
        msg.addReceiver(this.getAgent().getCoordinatorAgent());
        msg.addReceiver(this.getAgent().getScoutCoordinatorAgent());
        msg.addReceiver(this.getAgent().getHarvesterCoordinatorAgent());
        msg.setContentObject(al);
        this.getAgent().send(msg);
    }
}

