package fipa.impl.protocol.request.interaction;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import fipa.protocol.prodcons.ProducerBehaviour;
import fipa.protocol.request.interaction.RequestInteractionProtocolInitiator;

public class RequestInteractionProtocolInitiatorImpl extends
		RequestInteractionProtocolInitiator {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 7237690237812699866L;

	// --- Constructors --------------------------------------------------------

	public RequestInteractionProtocolInitiatorImpl(Agent agent, DataStore store) {
		super(agent, null, store);
	}

	// --- Methods -------------------------------------------------------------

	@Override
	protected void handleRefuse(ACLMessage refuse) {
		ProducerBehaviour parent = (ProducerBehaviour) getParent();
		parent
				.handleTerminateEvent(ProducerBehaviour.AuctionTerminationEvent.WINNER_REFUSE_TO_PAY);
		super.handleRefuse(refuse);
	}

	@Override
	protected void handleFailure(ACLMessage failure) {
		ProducerBehaviour parent = (ProducerBehaviour) getParent();
		parent
				.handleTerminateEvent(ProducerBehaviour.AuctionTerminationEvent.PAYMENT_FAILURE);
		super.handleFailure(failure);
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		ProducerBehaviour parent = (ProducerBehaviour) getParent();
		parent
				.handleTerminateEvent(ProducerBehaviour.AuctionTerminationEvent.PAYMENT_OK);
		super.handleInform(inform);
	}
}
