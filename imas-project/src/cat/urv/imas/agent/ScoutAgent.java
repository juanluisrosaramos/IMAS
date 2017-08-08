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



import cat.urv.imas.behaviour.field_agents.PerformMovementBehavior;
import cat.urv.imas.behaviour.field_agents.UpdaterStatisticsListener;
import cat.urv.imas.behaviour.scout.CNScoutingResponse;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The scout  agent
 * agent every round and share the necessary information to other coordinators.
 */
public class ScoutAgent extends FieldAgent  {

    @Override
    protected void onReachTarget(Cell[][] scope, int targetY, int targetX) {
        this.resetTarget();
    }

    @Override
    public void setTargetCell(int x, int y) throws Exception {
        super.setTargetCell(x, y);
        //requests to ask the optimal path
        ACLMessage req = MessageProtocol.getRequestMsg(MessageProtocol.PATH_REQUEST,this.myCoordinator());
        req.setContentObject(getPathParameters());
        this.send(req);
        ACLMessage ans = this.blockingReceive(MessageTemplates.PATH_RESPONSE);
        ArrayList<String> path = (ArrayList<String>) ans.getContentObject();
        setNewPath(path);
    }

    private AID scoutCoordinatorAgent;

    private PerformMovementBehavior movementBehavior;
    

    /**
     * Builds the coordinator agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }

    @Override
    protected void setupConcrete() {
        Object[] args = getArguments();
        this.setCurrentCell((StreetCell)args[0]);
        this.scoutCoordinatorAgent = this.searchAgent(AgentType.SCOUT_COORDINATOR);
        this.movementBehavior = new PerformMovementBehavior(this);
        this.addBehaviour(new CNScoutingResponse(this,MessageTemplates.SCOUTING_RESPONSE));
        this.addBehaviour(new UpdaterStatisticsListener(this));
    }

    protected int building(Cell c){
        if(c==null) return 0;
        if(c.getCellType()==CellType.BUILDING) return 1;
        return 0;
    }
    
    @Override
    public AID myCoordinator() {
        return this.scoutCoordinatorAgent;
    }
    
    @Override
    public void simulationStep(Object o){
        ArrayList data = (ArrayList) o;
        Cell[][] scope = (Cell[][])data.get(0);
        this.movementBehavior.setScope(scope);
        this.addBehaviour(movementBehavior); //add each time because oneshotbehavior
    }
    


    @Override
    public Serializable getStatistics() {
        Map<String,ArrayList<String>> statistics = new HashMap<>();
        ArrayList<String> lst = new ArrayList<>();
        lst.add(Integer.toString(getSuccessCounter()));
        lst.add(Integer.toString(getFailureCounter()));
        statistics.put(getName(),lst);
        
        return (Serializable) statistics;
    }
}
