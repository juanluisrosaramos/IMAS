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
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.shared.SimStepBehavior;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import sun.util.logging.resources.logging;

/**
 * Agent abstraction used in this practical work.
 * It gathers common attributes and functionality from all agents.
 */
public class ImasAgent extends Agent {
    
    /**
     * Type of this agent.
     */
    protected AgentType type;
    protected AID sa;
    
    /**
     * Agents' owner.
     */
    public static final String OWNER = "urv";
    /**
     * Language used for communication.
     */
    public static final String LANGUAGE = "serialized-object";
    /**
     * Onthology used in the communication.
     */
    public static final String ONTOLOGY = "serialized-object";
    private boolean logging=true;
    
    /**
     * Creates the agent.
     * @param type type of agent to set.
     */
    public ImasAgent(AgentType type) {
        super();
        this.type = type;
    }
    
    /**
     * Informs the type of agent.
     * @return the type of agent.
     */
    public AgentType getType() {
        return this.type;
    }
    
    /**
     * Add a new message to the log.
     *
     * @param str message to show
     */
    public void log(String str) {
        if(logging){
            System.out.println(getLocalName() + ": " + str);
        }
    }
    
    /**
     * Add a new message to the error log.
     *
     * @param str message to show
     */
    public void errorLog(String str) {
        System.err.println(getLocalName() + ": " + str);
    }
    
    protected AID getSA(){
        if (sa!=null){
            return sa;
        }
        else{
            this.sa = this.searchAgent(AgentType.SYSTEM, null);
            return sa;
        }
    }
    
    @Override
    protected void setup() {
        super.setup(); 
        this.setupBase();
        this.setupConcrete();
        this.addStepSimBehavior();
    }
     
    private void setupBase() {
        /* ** Very Important Line (VIL), provided in the skeleton *********
        from Jade doc: This method declares this agent attitude towards 
        object-to-agent communication, that is, whether the agent accepts 
        to communicate with other non-JADE components living within the same JVM.
        */
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        
        //1. initialize service description
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(getType().toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        // 2. Register the agent to the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }
    }
  
    protected void setupConcrete(){
    }

    protected AID searchAgent(AgentType agentType, String name){
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(agentType.toString());
        if (name !=null){
            searchCriterion.setName(name);
        }
        return UtilsAgents.searchAgent(this, searchCriterion);
    }
    
    protected AID searchAgent(AgentType agentType){
        return this.searchAgent(agentType, null);
    }
    
    protected AgentController createAgent(String agentName, Class klass, Object[] args) throws StaleProxyException {
        String className = klass.getName();
        final ContainerController cc = this.getContainerController();
        AgentController agentController = cc.createNewAgent(agentName, className, args);
        agentController.start();
   
        return agentController;
    }
    
    protected AgentController createAgent(String agentName, Class klass) throws StaleProxyException {
        return createAgent(agentName,klass,null);
    }
    
    public void simulationStep(Object o){
        throw new UnsupportedOperationException("This method should be implemented in the subclasses");
    }

    public int numBuildings(Cell[][] array,int idx, char dir) {
        Cell[] v = {};
        if (dir=='r'){
            v = array[idx];
        }
        else if (dir=='c'){
            v = new Cell[array[idx].length];
            for(int i=0; i<array[idx].length; i++){
                v[i] = array[idx][i];
            }
        }
        
        int result = 0;
        for (int i = 1; i < v.length; i++) {
            if(v[i].getCellType()==CellType.BUILDING){
                result +=1;
            }
        }
        return result;
    }
    // getting the maximum value
    public int getMaxValue(int[] array) {
        int maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    public AID myCoordinator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void processMessage(ACLMessage msg){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    protected void addStepSimBehavior() {
    //to be rewritten by those (SystemAgent) that don't want this behavior
        this.addBehaviour(new SimStepBehavior());
    }

}
