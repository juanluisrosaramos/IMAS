/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import jade.core.AID;
import java.util.ArrayList;

/**
 * interface to trigger all the garbage events: detect, gathered, recycled
 * @author carles
 */
public interface IGarbageListener {
    public void onDetectedGarbage(ArrayList<Cell> detectedGarbage);
    public void onGatherGarbage(AID sender, SettableBuildingCell buildingWithGarbage, int y, int x, GarbageType gt, int quantity);
    public void onRecyclingGarbage(RecyclingCenterCell rec, int qty, SettableBuildingCell sourceBuildingWithGarbage);
}
