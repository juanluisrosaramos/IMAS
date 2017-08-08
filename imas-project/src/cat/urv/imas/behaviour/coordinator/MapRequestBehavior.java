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
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.behaviour.shared.RequestBehaviorBase;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.agent.IAgentWithMap;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.GameSettings;

/**
 * Behavior for the Coordinator agent to deal with AGREE messages.
 * The Coordinator Agent sends a REQUEST for the
 * information of the game settings. The System Agent sends an AGREE and 
 * then it informs of this information which is stored by the Coordinator Agent. 
 * 
 * NOTE: The game is processed by another behavior that we add after the 
 * INFORM has been processed.
 */
public class MapRequestBehavior extends RequestBehaviorBase {

    public MapRequestBehavior(ImasAgent a, ACLMessage msg) {
        super(a, msg);
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        IAgentWithMap agentWithMap = (IAgentWithMap)getAgent();
        getAgent().log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            GameSettings game = (GameSettings) msg.getContentObject();
            agentWithMap.setGame(game);
            getAgent().log(game.getShortString());
        } catch (Exception e) {
            getAgent().errorLog("Incorrect content: " + e.toString());
        }
    }
}
