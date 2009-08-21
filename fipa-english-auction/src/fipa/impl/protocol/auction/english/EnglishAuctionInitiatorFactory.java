package fipa.impl.protocol.auction.english;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import fipa.agent.Producer;
import fipa.protocol.auction.english.EnglishAuctionInitiator;

/**
 * Classical factory class, responsible for the creation of
 * <code>EnglishAuctionInitiator</code> objects.
 * 
 * @author Nikolay Vasilev
 */
public class EnglishAuctionInitiatorFactory {

    // --- Constants -----------------------------------------------------------

    public static final String SEARCHED_SERVICE_DESC_TYPE = "consumer";

    // --- Class Variables -----------------------------------------------------

    private static EnglishAuctionInitiatorFactory instance;

    // --- Constructors --------------------------------------------------------

    private EnglishAuctionInitiatorFactory() {
    }

    // --- Methods -------------------------------------------------------------

    public static EnglishAuctionInitiatorFactory instance() {
	if (instance == null) {
	    instance = new EnglishAuctionInitiatorFactory();
	}
	return instance;
    }

    public EnglishAuctionInitiatorImpl createEnglishAuctionInitiator(
	    Agent producerAgent, DataStore dataStore) {
	verifyProducerAgent(producerAgent);
	ACLMessage informMsg = prepareInformInitiationMessage(producerAgent);
	EnglishAuctionInitiatorImpl producerBehaviour = new EnglishAuctionInitiatorImpl(
		producerAgent, informMsg, dataStore);
	return producerBehaviour;
    }

    protected List<AID> getParticipantsAIDs(Agent agent) {
	verifyProducerAgent(agent);
	DFAgentDescription template = new DFAgentDescription();
	String serviceDescType = getSearchedServiceDescriptionType();
	ServiceDescription sd = new ServiceDescription();
	sd.setType(serviceDescType);
	template.addServices(sd);

	List<AID> agentsAidList = null;
	try {
	    DFAgentDescription[] result = DFService.search(agent, template);
	    agentsAidList = new ArrayList<AID>();
	    for (int i = 0; i < result.length; ++i) {
		agentsAidList.add(result[i].getName());
	    }
	} catch (FIPAException fe) {
	    throw new RuntimeException(
		    "Problem during the searching for consumers...", fe);
	}
	return (agentsAidList == null || agentsAidList.isEmpty()) ? null
		: agentsAidList;
    }

    public ACLMessage prepareInformInitiationMessage(Agent producerAgent) {
	verifyProducerAgent(producerAgent);
	List<AID> participants = getParticipantsAIDs(producerAgent);
	if (participants == null) {
	    return null;
	}
	ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
	for (AID aid : participants) {
	    informMsg.addReceiver(aid);
	}
	informMsg.setPerformative(ACLMessage.INFORM);
	informMsg.setSender(producerAgent.getAID());
	informMsg
		.setProtocol(FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION);
	informMsg.setContent(EnglishAuctionInitiator.ENGLISH_AUCTION_STARTS);

	return informMsg;
    }

    protected String getSearchedServiceDescriptionType() {
	return SEARCHED_SERVICE_DESC_TYPE;
    }

    private void verifyProducerAgent(Agent producerAgent) {
	if (producerAgent == null) {
	    throw new IllegalArgumentException(
		    "Invalid argument - agent: null.");
	}
	if (!(producerAgent instanceof Producer)) {
	    throw new IllegalArgumentException(
		    "The agent must implement the interface "
			    + Producer.class.getName());
	}
    }
}
