/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.StreetCell;

/**
 * Common interface for those controllers that checks movements
 *  closely related with CheckMovementBehavior (which performs the movement checking)
 * @author carles
 */
public interface IMovementChecker {
   // Called when an agent leaves a cell
   public void onLeaveCell(StreetCell cell) ;        
}
