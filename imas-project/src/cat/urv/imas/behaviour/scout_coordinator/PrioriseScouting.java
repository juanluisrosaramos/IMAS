/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scout_coordinator;

import cat.urv.imas.agent.ScoutCoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;

import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.MessageContent;

import jade.core.AID;
import jade.core.Agent;

import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;


/**
 * Behavior to control how the buildings are visiting and send contracts for 
 * those that need to be scouted
 * @author carles
 */
public class PrioriseScouting extends OneShotBehaviour{
    //final private ArrayList<SettableBuildingCell> list = getAgent().getBuildingList();
    final int timeThreshold = 100; //TODO: tune this value
    //hashmap with raised contracts (k: building, v: last time raise a contract)
    final HashMap<String,Integer> contracts= new HashMap<>();
    final int N_TURNS = 50;
    int counter = 0;

    
    public PrioriseScouting(Agent a, int turn) {
        super(a);
        counter = turn;
    }   
    
    @Override
    public ScoutCoordinatorAgent getAgent(){
        return (ScoutCoordinatorAgent)this.myAgent;
    }    
    
    @Override
    public void action() {    
        if((this.counter % N_TURNS)!=0) {
            return;
        }
        ArrayList<SettableBuildingCell> contracts = nextContracts(getAgent().getScoutsAID().size());
        for(SettableBuildingCell c:contracts){
            getAgent().log("Raising a contract for scouting buildingCell: y=>"+c.getRow()+" x=>"+c.getCol());
            // Fill the CFP message
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            for (AID aid: getAgent().getScoutsAID()) {
                msg.addReceiver(aid);
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 5 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
            msg.setContent(MessageContent.CFP_SCOUTING);
            getAgent().addBehaviour(new CNScoutingRequest(getAgent(), msg,c,getAgent().getScoutsAID().size())); 
        }
    }
    
    private ArrayList<SettableBuildingCell> nextContracts(int n){
    // It takes buildings less visited and raise contracts to try to be scouted
        ArrayList<SettableBuildingCell> oldestContracts= new ArrayList<>();
        
        for(SettableBuildingCell building: getAgent().getOldestVisitedBuildings()){
        //loop to select those buildings that have the oldest contracts in time
            String k = UtilsAgents.getKey(building);
            if(!this.contracts.containsKey(k)){
            //if not in contract: add it with counter 0
                this.contracts.put(k, 0);
            }
            if(oldestContracts.isEmpty()){
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
            
            SettableBuildingCell oldestCell = oldestContracts.get(0);
            if(this.contracts.get(k) < this.contracts.get(UtilsAgents.getKey(oldestCell))){
                oldestContracts.clear();
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
            else if(this.contracts.get(k).equals(this.contracts.get(UtilsAgents.getKey(oldestCell)))){
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
        }
        Random rnd = new Random();
        ArrayList<SettableBuildingCell> result = new ArrayList<>();
        //get n oldcontracts randomly
        for(int i=0;i<n;i++){
            if(!oldestContracts.isEmpty()){
                int r = rnd.nextInt(oldestContracts.size());
                SettableBuildingCell oldContract = oldestContracts.get(r);
                result.add(oldContract);
                String k = UtilsAgents.getKey(oldContract);
                this.contracts.put(k, this.contracts.get(k)+1);
            }
        }
        return result;
    }
    private SettableBuildingCell nextContract() {
        SettableBuildingCell result=null;
        ArrayList<SettableBuildingCell> oldestContracts= new ArrayList<>();
        
        for(SettableBuildingCell building: getAgent().getOldestVisitedBuildings()){
        //loop to select those buildings that have the oldest contracts in time
            String k = UtilsAgents.getKey(building);
            if(!this.contracts.containsKey(k)){
            //if not in contract: add it with counter 0
                this.contracts.put(k, 0);
            }
            if(oldestContracts.isEmpty()){
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
            
            SettableBuildingCell oldestCell = oldestContracts.get(0);
            if(this.contracts.get(k) < this.contracts.get(UtilsAgents.getKey(oldestCell))){
                oldestContracts.clear();
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
            else if(this.contracts.get(k).equals(this.contracts.get(UtilsAgents.getKey(oldestCell)))){
                oldestContracts.add(getAgent().buildingFromKey(k));
            }
        }
        
        if(!oldestContracts.isEmpty()){
            int r = (new Random()).nextInt(oldestContracts.size());
            result = oldestContracts.get(r);
            String k = UtilsAgents.getKey(result);
            this.contracts.put(k, this.contracts.get(k)+1);
        }

        return result;
    }    
}
