package fipa.impl.protocol.request.interaction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import fipa.protocol.prodcons.ConsumerBehaviour;
import fipa.protocol.request.interaction.RequestInteractionProtocolResponder;

/**
 * Class responsible for the connection of the
 * <code>Request Interaction Protocol</code> behaviour implementation and the
 * consumer agent.
 * 
 * @author Ruben Rios
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
		ConsumerBehaviour parent = (ConsumerBehaviour) getParent();
		return parent.isReadyToPay(price);
	}

	@Override
	protected boolean executePayment(AID aid, double price) {
		ConsumerBehaviour parent = (ConsumerBehaviour) getParent();
		return parent.executePayment(aid, price);
	}
}
