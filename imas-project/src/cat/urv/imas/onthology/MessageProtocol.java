/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author carles
 */
public class MessageProtocol {
    public static final String SIM_STEP= "Next simulation step";
    public static final String SIM_FINISH= "Simulation step finished";
    public static final String CHECK_CELL_MOVEMENT = "Check_cell_movement";
    public static final String NEW_GARBAGE_DETECTED = "new_garbage_detected";
    public static final String GATHERED_GARBAGE = "gathered_garbage";
    public static final String STATISTICS_REQUEST = "statistics_request";
    public static final String STATISTICS_RESPONSE = "statistics_response";
    public static final String PATH_REQUEST = "path_request";
    public static final String PATH_RESPONSE = "path_response";
    public static final String GARBAGE_REQUEST = "garbage_request";
    public static final String RC_PATH = "recycling_center_path";
    public static final String RECYCLING_GARBAGE = "recycling_garbage";
    
    public static ACLMessage getInformMsg(String conversationId, AID receiver) {
        ACLMessage initialRequest = new ACLMessage(ACLMessage.INFORM);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(receiver);
        //initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        initialRequest.setConversationId(conversationId);

        return initialRequest;
    }
    public static ACLMessage getRequestMsg(String conversationId) {
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        initialRequest.setConversationId(conversationId);
        return initialRequest;
    }
    
    public static ACLMessage getRequestMsg(String conversationId, AID receiver) {
        ACLMessage initialRequest = getRequestMsg(conversationId);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(receiver);
        
        return initialRequest;
    }
}
