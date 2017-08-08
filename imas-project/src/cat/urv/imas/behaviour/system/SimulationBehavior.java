/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;


/**
 *
 * @author carles
 */
public class SimulationBehavior extends FSMBehaviour {
    
    public SimulationBehavior(Agent a) {
        super(a);
        this.registerFirstState(new GarbageControllerBehavior(this.myAgent),"Garbage");
        this.registerState(new SendSimStepBehavior(this.myAgent), "SSim");
        this.registerState(new ReceiveSimResponsesBehavior(this.myAgent), "RSim");
        this.registerState(new UpdaterStatistics(this.myAgent), "UpdaterStatistics");
        this.registerDefaultTransition("Garbage", "SSim", new String[]{"Garbage"});
        this.registerDefaultTransition("SSim", "RSim",new String[]{"SSim"});
        //this.registerDefaultTransition("RSim", "Garbage",new String[]{"RSim"});
        this.registerDefaultTransition("RSim", "UpdaterStatistics",new String[]{"RSim"});
        this.registerDefaultTransition("UpdaterStatistics", "Garbage",new String[]{"UpdaterStatistics"});
    }    
}
