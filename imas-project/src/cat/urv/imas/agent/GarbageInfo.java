/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import jade.core.AID;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author carles
 */
public class GarbageInfo implements Serializable{
    private Map<GarbageType,Integer> building_garbage;
    private SettableBuildingCell cell;

    public void setGarbageQty(Integer qty) {
        building_garbage.put(this.getGarbageType(),qty);
    }

    public GarbageInfo(SettableBuildingCell cell, Map<GarbageType, Integer> building_garbage) {
        this.building_garbage = building_garbage;
        this.cell = cell;
    }
    
    public GarbageType getGarbageType(){
        return (GarbageType) building_garbage.keySet().toArray()[0];
    }

    public Integer getGarbageQty(){
        return building_garbage.get(getGarbageType());
    }
    public SettableBuildingCell getCell(){
        return cell;
    }
    
}
