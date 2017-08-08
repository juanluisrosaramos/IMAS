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
package cat.urv.imas.behaviour.system;

import cat.urv.imas.behaviour.shared.ResponseBehaviorBase;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;


public class MapResponseBehavior extends ResponseBehaviorBase {
    private final SystemAgent agent;
    public MapResponseBehavior(SystemAgent agent, MessageTemplate mt) {
        super(agent, mt);
        this.agent = agent;
    }

    @Override
    protected Serializable prepareContentRequested() {
        return agent.getGame();
    }
}