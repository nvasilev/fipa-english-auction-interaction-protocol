package fipa.impl.protocol.auction.english;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import fipa.protocol.auction.english.EnglishAuctionInitiator;

/**
 * Classical factory class, responsible for the creation of
 * <code>EnglishAuctionInitiator</code> objects.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class EnglishAuctionInitiatorFactory {

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
		ACLMessage informMsg = prepareInformInitiationMessage(producerAgent);
		EnglishAuctionInitiatorImpl producerBehaviour = new EnglishAuctionInitiatorImpl(
				producerAgent, informMsg, dataStore);
		return producerBehaviour;
	}

	protected List<AID> getParticipantsAIDs(Agent agent) {
		List<AID> agentsAidList = null;
		try {
			DFAgentDescription[] result = DFService.search(agent, null);
			agentsAidList = new ArrayList<AID>();
			for (int i = 0; i < result.length; ++i) {
				AID potentialConsumerAid = result[i].getName();
				if (agent.getAID().equals(potentialConsumerAid)
						|| agentsAidList.contains(potentialConsumerAid)) {
					continue;
				}
				agentsAidList.add(potentialConsumerAid);
			}
		} catch (FIPAException fe) {
			throw new RuntimeException(
					"Problem during the searching for consumers...", fe);
		}
		System.out.println(agentsAidList);
		return (agentsAidList == null || agentsAidList.isEmpty()) ? null
				: agentsAidList;
	}

	public ACLMessage prepareInformInitiationMessage(Agent producerAgent) {
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
}
