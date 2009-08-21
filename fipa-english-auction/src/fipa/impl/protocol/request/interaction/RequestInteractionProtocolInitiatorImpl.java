package fipa.impl.protocol.request.interaction;

import fipa.agent.Producer;
import fipa.protocol.request.interaction.RequestInteractionProtocolInitiator;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

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
	Producer producer = (Producer) myAgent;
	producer
		.handleTerminateEvent(Producer.AuctionTerminationEvent.WINNER_REFUSE_TO_PAY);
	super.handleRefuse(refuse);
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
	Producer producer = (Producer) myAgent;
	producer
		.handleTerminateEvent(Producer.AuctionTerminationEvent.PAYMENT_FAILURE);
	super.handleFailure(failure);
    }

    @Override
    protected void handleInform(ACLMessage inform) {
	Producer producer = (Producer) myAgent;
	producer
		.handleTerminateEvent(Producer.AuctionTerminationEvent.PAYMENT_OK);
	super.handleInform(inform);
    }
}
