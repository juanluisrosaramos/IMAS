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

import cat.urv.imas.behaviour.scout.CNScoutingResponse;
import cat.urv.imas.behaviour.shared.GarbageDetectionListener;
import cat.urv.imas.behaviour.shared.GarbageGatheredListener;
import cat.urv.imas.behaviour.shared.RecyclingGarbageListener;
import cat.urv.imas.behaviour.shared.SimStepBehavior;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.MapResponseBehavior;
import cat.urv.imas.behaviour.system.SimulationBehavior;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageStatistic;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageTemplates;
import jade.core.*;
import jade.wrapper.StaleProxyException;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the ontology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent implements IGarbageListener {
    protected int messagesSent=0;
    protected int receivedMessages=0;
    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private GameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent, scoutCoordinatorAgent, harvesterCoordinatorAgent;
    private final ArrayList<AID> agents=new ArrayList<>();
    
    
    final private Map<AID, Map<String,Integer>> garbageStatistics = new HashMap<>();
    public Integer sim_step = 0;

    public Integer getSim_step() {
        return sim_step;
    }
    public Integer next_step(){
        sim_step +=1;
        return sim_step;
    }  
    
    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the 
     * standard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }
    
    /**
     * An error message is shown in the log area of the GUI, as well as in the 
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /*
     * Agent setup concrete method - called from ImasAgent.setup 
     */
    @Override
    protected void setupConcrete() {
        // 1. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");

        // 2. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(ImasAgent.class.getName()).log(Level.SEVERE, null, e);
        }

        //3. add behaviours
        // we wait for the initialization of the game
        this.addBehaviour(new MapResponseBehavior(this, MessageTemplates.GET_MAP));
        // simulation steps 
        this.addBehaviour(new SimulationBehavior(this));
        this.addBehaviour(new GarbageDetectionListener(this));
        this.addBehaviour(new GarbageGatheredListener(this));
        this.addBehaviour(new RecyclingGarbageListener(this));

        try{
            createAgents();
            //simulation();
        } catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(ImasAgent.class.getName()).log(Level.SEVERE, null, e);
        }
    }
   
    
    public List<Cell> getScoutsCells(){
        return this.game.getAgentList().get(AgentType.SCOUT);
    }
    
    public List<Cell> getHarvestersCells(){
        return this.game.getAgentList().get(AgentType.HARVESTER);
    }
    /*
    private List<AID> getAID(List<Cell> lst){
        Vector<AID> result = new Vector<AID>();
        for(Cell cell: lst){
            AID aid = ((StreetCell)cell).getAgent().getAID();
            result.add(aid);
        }
        return result;
    }
    
    private List<AID> getScouts(){
        return getAID(this.getScoutsCells());
    }
    private List<AID> getHarvesters(){
        return getAID(this.getHarvestersCells());
    }
    */
    public ArrayList<SettableBuildingCell> getBuildingList(){
        ArrayList<SettableBuildingCell> buildingCells = new ArrayList<SettableBuildingCell>();
        GameSettings game = this.getGame();
        int N_ROWS = game.getMap().length;
        int N_COLS = game.getMap()[0].length;
        for (int i=0;i<N_ROWS;i++){
            for(int j=0;j<N_COLS;j++){
                if(game.get(i, j) instanceof SettableBuildingCell){
                    buildingCells.add((SettableBuildingCell)game.get(i,j));
                }
            }
        }
        return buildingCells;
    } 
    
    private void createAgents()  throws StaleProxyException, Exception {
        this.createAgent("GeneralCoordinator", CoordinatorAgent.class);
        this.coordinatorAgent = this.searchAgent(AgentType.COORDINATOR);
        this.agents.add(this.coordinatorAgent);
        
        this.createAgent("HarvesterCoordinator", HarvesterCoordinatorAgent.class);
        this.harvesterCoordinatorAgent = this.searchAgent(AgentType.HARVESTER_COORDINATOR);
        this.agents.add(this.harvesterCoordinatorAgent);
        
        this.createAgent("ScoutCoordinator", ScoutCoordinatorAgent.class);
        this.scoutCoordinatorAgent = this.searchAgent(AgentType.SCOUT_COORDINATOR);
        this.agents.add(this.scoutCoordinatorAgent);
        
        int i=1;
        for(Cell cell:getScoutsCells()){
            Object[] args = {cell};
            String name = "Scout_"+i;
            this.createAgent(name, ScoutAgent.class,args);
            AID aid= this.searchAgent(AgentType.SCOUT, name);
            ((StreetCell)cell).getAgent().setAID(aid);
            i +=1;
            this.initGarbageStatistics(aid, name);
            //if(i==2){ break;}
        }
        //TODO: check!!
        CNScoutingResponse.MAX_TURNS_WITH_NO_ANWSER=getScoutsCells().size() *(getScoutsCells().size()-1); //cnrequestbehavior will raise numscouts*(numscouts-1)=contracts
        //CNScoutingResponse.MAX_TURNS_WITH_NO_ANWSER=1000;
        i=1;
        for(Cell cell:getHarvestersCells()){
            Object[] args = {cell};
            String name = "Harvester_"+i;
            this.createAgent(name, HarvesterAgent.class,args);
            AID aid= this.searchAgent(AgentType.HARVESTER, name);
            ((StreetCell)cell).getAgent().setAID(aid);
            i +=1;
            //if(i==2){ break;}
        }
    }

    @Override
    protected void setup(){
        super.setup();
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(int i) {
        messagesSent = i;
    }
    
    @Override
    protected void addStepSimBehavior() {
    // SYSTEM AGENT Doesn't rereceive the sim step message
    }

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    public AID getScoutCoordinatorAgent() {
        return scoutCoordinatorAgent;
    }

    public AID getHarvesterCoordinatorAgent() {
        return harvesterCoordinatorAgent;
    }
    
     /**
     * delegate
     *
     * @param msg String per mostrar
     */
    public void logGui(String msg) {
        this.gui.log(msg);
    }

    /**
     * delegate
     *
     * @param msg String per mostrar
     */
    public void showStatistics(String msg) {
        this.gui.showStatistics(msg);
    }
    
    public void clearStatisticsPanel(){
        this.gui.clearStatistics();
    }
    public void prepareForTest(){
        this.game = InitialGameSettings.load("game.settings");
    }
    
    
    public void incGarbageStatistics(AID aid){
        HashMap<String,Integer> entry = (HashMap<String,Integer>) this.garbageStatistics.get(aid);
        String agent_name = (String) entry.keySet().toArray()[0];
        entry.put(agent_name, entry.get(agent_name)+1);
        this.garbageStatistics.put(aid, entry);
    }
    public void initGarbageStatistics(AID aid, String name){
        HashMap<String,Integer> entry = new HashMap<>();
        entry.put(name, 0);
        this.garbageStatistics.put(aid, entry);
    }
    
    public String garbageStatistics(){
        Map<String,Integer> tmp = new HashMap<>();
        for(AID key:this.garbageStatistics.keySet()){
            Map<String,Integer> m = this.garbageStatistics.get(key);
            String agent_name = (String)m.keySet().toArray()[0];
            
            tmp.put(agent_name, m.get(agent_name));
        }
        
        Object[] agent_names = tmp.keySet().toArray();
        Arrays.sort(agent_names);
        StringBuilder sb = new StringBuilder();
        
        for(Object agent_name:agent_names){
            sb.append(String.format("%s:%s", agent_name, tmp.get(agent_name)));
        }
        return String.format("***GARBAGE***\n%s",sb.toString());
    }

    public void showStatistics(String format, String toString) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onDetectedGarbage(ArrayList<Cell> detectedGarbage) {
        for(int i=0;i<detectedGarbage.size();i++){
            Cell c = (Cell) detectedGarbage.get(i);
            ArrayList<GarbageInfo> gi = this.getGame().detectBuildingsWithGarbage(c.getRow(), c.getCol());
            for(GarbageInfo _gi:gi){
                GarbageStatistic.setTurnDiscovered(this.getSim_step(), _gi.getCell().getRow(), _gi.getCell().getCol());
            }
        }
    } 

    @Override
    public void onGatherGarbage(AID sender, SettableBuildingCell buildingWithGarbage, int y, int x, GarbageType gt, int quantity) {
        SettableBuildingCell c = (SettableBuildingCell) this.getGame().get(buildingWithGarbage.getRow(), buildingWithGarbage.getCol());
        GarbageStatistic.setTurnCollected(this.getSim_step(), c.getRow(), c.getCol());
        for(int i=quantity;i>0;i--){
            c.removeGarbage();
        }

    }
    
    @Override
    public AID myCoordinator() {
        return this.getAID();
    }

    @Override
    public void onRecyclingGarbage(RecyclingCenterCell rec, int qty, SettableBuildingCell sourceBuildingWithGarbage) {
        int POINTS = 100; //TODO
        GarbageStatistic.setTurnRecycled(this.getSim_step(),POINTS, sourceBuildingWithGarbage.getRow(), sourceBuildingWithGarbage.getCol());
    }

    public void showStatistics() {
        StringBuilder s=new StringBuilder();
        s.append(String.format("\nCurrent benefits: %s",100));
        s.append(String.format("\nAvg. Time discovering garbage: %s",GarbageStatistic.averageDiscoveryTime()));
        s.append(String.format("\nAvg. Time collecting garbage: %s",GarbageStatistic.averageCollectionTime()));
        s.append(String.format("\nRatio discovered garbage: %s",GarbageStatistic.ratioDiscoveredGarbage()));
        s.append(String.format("\nRatio collected garbage: %s",GarbageStatistic.ratioCollectedGarbage()));
        this.showStatistics(s.toString());
    }
}
