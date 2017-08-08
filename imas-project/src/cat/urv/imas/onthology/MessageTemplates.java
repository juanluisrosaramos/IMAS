/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.behaviour.system.MapResponseBehavior;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author carles
 */
public class MessageTemplates {
    public static final MessageTemplate GET_MAP = 
            MessageTemplate.and(MessageTemplate.MatchConversationId(MessageContent.GET_MAP),
                MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), 
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
   public static final MessageTemplate SCOUTING_RESPONSE = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                                MessageTemplate.and(
                                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                                        MessageTemplate.MatchContent(MessageContent.CFP_SCOUTING)));
    public static final MessageTemplate CHECK_MOVEMENT = MessageTemplate.MatchProtocol(MessageProtocol.CHECK_CELL_MOVEMENT);
    public static final MessageTemplate SIM_FINISH_TEMPLATE = MessageTemplate.MatchProtocol(MessageProtocol.SIM_FINISH);
    public static final MessageTemplate SIM_STEP_TEMPLATE = MessageTemplate.MatchProtocol(MessageProtocol.SIM_STEP);
    public static final MessageTemplate GARBAGE_DETECTION = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                MessageTemplate.MatchConversationId(MessageProtocol.NEW_GARBAGE_DETECTED));
    
    public static final MessageTemplate GATHERED_GARBAGE = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                MessageTemplate.MatchConversationId(MessageProtocol.GATHERED_GARBAGE));
    
    public static final MessageTemplate STATISTICS_REQUESTS = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchConversationId(MessageProtocol.STATISTICS_REQUEST))
            ;
    public static final MessageTemplate STATISTICS_RESPONSE = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId(MessageProtocol.STATISTICS_RESPONSE));
    
    public static final MessageTemplate PATH_REQUEST = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchConversationId(MessageProtocol.PATH_REQUEST));
    
    public static final MessageTemplate PATH_RESPONSE = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId(MessageProtocol.PATH_RESPONSE));
    
    public static final MessageTemplate GARBAGE_REQUEST = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION),
                                MessageTemplate.and(
                                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                                        MessageTemplate.MatchConversationId(MessageProtocol.GARBAGE_REQUEST)));
    
    public static final MessageTemplate RECYCLING_CENTER_PATH = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId(MessageProtocol.RC_PATH));
    
    public static final MessageTemplate RECYCLING_GARBAGE = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId(MessageProtocol.RECYCLING_GARBAGE));
}
