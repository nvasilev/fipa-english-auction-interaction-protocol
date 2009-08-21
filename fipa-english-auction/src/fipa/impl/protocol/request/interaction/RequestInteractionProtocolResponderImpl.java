package fipa.impl.protocol.request.interaction;

import fipa.agent.Consumer;
import fipa.protocol.request.interaction.RequestInteractionProtocolResponder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;

/**
 * Class responsible for the connection of the
 * <code>Request Interaction Protocol</code> behaviour implementation and the
 * consumer agent.
 * 
 * @author Nikolay Vasilev
 */
public class RequestInteractionProtocolResponderImpl extends
	RequestInteractionProtocolResponder {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = 6568448496275435985L;

    // --- Constructors --------------------------------------------------------

    public RequestInteractionProtocolResponderImpl(Agent agent, DataStore store) {
	super(agent, store);
    }

    // --- Methods (Inherited) -------------------------------------------------

    @Override
    protected boolean isReadyToPay(double price) {
	Consumer consumer = (Consumer) myAgent;
	return consumer.isReadyToPay(price);
    }

    @Override
    protected boolean executePayment(AID aid, double price) {
	Consumer consumer = (Consumer) myAgent;
	return consumer.executePayment(aid, price);
    }
}
