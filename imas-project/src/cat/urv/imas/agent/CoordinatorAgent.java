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

import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.MapRequestBehavior;
import cat.urv.imas.behaviour.shared.CheckMovementBehavior;
import cat.urv.imas.behaviour.shared.GarbageDetectionListener;
import cat.urv.imas.behaviour.shared.GarbageGatheredListener;
import cat.urv.imas.behaviour.shared.RecyclingGarbageListener;
import cat.urv.imas.behaviour.shared.RequestBehaviorBase;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.*;


/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent implements IAgentWithMap {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID systemAgent;
    private AID harvesterCoordinatorAgent;
    private AID scoutCoordinatorAgent;
    private CheckMovementBehavior movementChecker;
    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }
    
    /*
     * Agent setup concrete method - called from ImasAgent.setup 
     */
    @Override
    protected void setupConcrete() {
        // search SystemAgent
        this.systemAgent = this.searchAgent(AgentType.SYSTEM);
        /* *******************************************************************/
        //we add a behaviour that sends the message and waits for an answer
        this.addBehaviour(new MapRequestBehavior(this, MessageProtocol.getRequestMsg(MessageContent.GET_MAP, this.systemAgent)));
        this.movementChecker = new CheckMovementBehavior(this);
        this.addBehaviour(this.movementChecker);
        this.addBehaviour(new GarbageGatheredListener(this));
        this.addBehaviour(new GarbageDetectionListener(this));
        this.addBehaviour(new RecyclingGarbageListener(this));
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    } 
    
    @Override
    public AID myCoordinator() {
        return this.systemAgent;
    }
    
    @Override
    public void simulationStep(Object o){
        this.movementChecker.restart();
    }
}
