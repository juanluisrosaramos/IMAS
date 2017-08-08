/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.shared;

import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.MessageProtocol;
import cat.urv.imas.onthology.MessageTemplates;
import cat.urv.imas.onthology.PathFinder;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Find the minimum distance between two points
 * @author carles
 */
public class PathFinderBehavior extends CyclicBehaviour{

    private final Cell[][] map;
    PathFinder finder ;
    @Override
    public ImasAgent getAgent(){
        return (ImasAgent)myAgent;
    }
    
    private StreetCell getMinStreetCell(int srcY,int srcX, int tarY, int tarX){
        ArrayList<StreetCell> candidates=new ArrayList<>();

        for(int i=tarY-1;i<tarY+2;i++){
            for(int j=tarX-1;j<tarX+2;j++){
                if((i==tarY) && (j==tarX)){}
                else{
                    int ii = min(max(i,0),map.length-1);
                    int jj = min(max(j,0),map[0].length-1);
                    if(map[ii][jj] instanceof StreetCell){
                        candidates.add((StreetCell)map[ii][jj]);
                    }
                }
            }
        }
        StreetCell min=null;
        long minDistance = Integer.MAX_VALUE;
        for(StreetCell candidate:candidates){
            long currDistance = (long) UtilsAgents.manhattanDistance(candidate.getCol(), srcX, candidate.getRow(), srcY);
            if( currDistance < minDistance){
                minDistance = currDistance;
                min = candidate;
            }
        }
        return min;
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplates.PATH_REQUEST);
        if(msg==null){
            this.block();
        }
        else{
            try {
                ArrayList<Integer> positions = (ArrayList<Integer>) msg.getContentObject();
                // positions.get(0) => srcY (streetcell)
                // positions.get(1) => srcX (streetcell)
                // positions.get(2) => tarY (building)
                // positions.get(3) => tarX (building)
                
                int srcY = positions.get(0);
                int srcX = positions.get(1);
                StreetCell min = this.getMinStreetCell(srcY,srcX, positions.get(2),positions.get(3));
                long a = System.currentTimeMillis();
                ArrayList<String> path = finder.getPath(srcY,srcX,min.getRow(),min.getCol());
                System.out.println(String.format("%d",System.currentTimeMillis()-a));
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setConversationId(MessageProtocol.PATH_RESPONSE);
                reply.setContentObject(path);
                myAgent.send(reply);
                
            } catch (UnreadableException | IOException ex) {
                Logger.getLogger(PathFinderBehavior.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            
        }
    }

    public PathFinderBehavior(Agent a, Cell[][] map) {
        super(a);
        this.map = map;
        finder = new PathFinder(map);
    }
    
}
