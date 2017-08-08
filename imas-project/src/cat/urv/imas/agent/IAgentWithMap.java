/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;

/**
 *
 * @author carles
 */
public interface IAgentWithMap {
   public void setGame(GameSettings game);
   public GameSettings getGame();
}
