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
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carles
 */
public class UpdaterStatistics extends OneShotBehaviour{
    long counter = 0;
    public UpdaterStatistics(Agent a) {
        super(a);
    }
    
    public SystemAgent getAgent(){
        return (SystemAgent)super.getAgent();
    }
    
    @Override
    public void action() {
        counter ++;
        if((counter%50)!=0) return;
        getAgent().clearStatisticsPanel();
        getAgent().showStatistics();
        /*
        int nrequests = 0;
        ACLMessage request = MessageProtocol.getRequestMsg(MessageProtocol.STATISTICS_REQUEST);
        for(Cell cell:getAgent().getScoutsCells()){
            StreetCell sc = (StreetCell) cell;
            if ((sc.getAgent()!=null) && (sc.getAgent().getAID()==null)){
                continue; //TODO: this is happening because we are not creating all the agents, remove this if when create everything
            }
            nrequests +=1;
            request.addReceiver(sc.getAgent().getAID());
        }
        
        getAgent().send(request);
        try {
            getAgent().clearStatisticsPanel();
            
            Map<String,ArrayList<String>> tmp = new HashMap<>();
            for(int i=nrequests;i>0;i--){
                ACLMessage response = getAgent().blockingReceive(MessageTemplates.STATISTICS_RESPONSE);
                Map<String,ArrayList<String>> statistics = (Map<String,ArrayList<String>>) response.getContentObject();
                String agent_name = (String) statistics.keySet().toArray()[0];
                tmp.put(agent_name, statistics.get(agent_name));
                
            }
            Object[] agent_names = tmp.keySet().toArray();
            Arrays.sort(agent_names);
            StringBuilder _statistics = new StringBuilder();
            for(Object agent_name:agent_names){
                _statistics.append(String.format("Name=>%s\n",agent_name));
                _statistics.append(String.format("Success:%s\n", tmp.get(agent_name).get(0)));
                _statistics.append(String.format("Failure:%s\n", tmp.get(agent_name).get(1)));
            }
            getAgent().showStatistics(String.format("****SCOUT****\n%s",_statistics.toString()));
            getAgent().showStatistics(getAgent().garbageStatistics());

        } catch (UnreadableException ex) {
            Logger.getLogger(UpdaterStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
}
