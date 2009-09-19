package fipa.protocol.request.interaction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;

/**
 * Class which implements the main functionality from the
 * <code>FIPA Request Interaction Protocol</code> from the side of the
 * responder.
 * 
 * @author Ruben Rios
 * @author Nikolay Vasilev
 */
public abstract class RequestInteractionProtocolResponder extends
		AchieveREResponder {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 5548179188959434895L;
	private static final String REQUEST_IP_RESPONDER = "[Request-IP-Responder]";

	// --- Constants (MsgTemplates) --------------------------------------------

	protected static final MessageTemplate MSG_TEMPLATE_PROTOCOL = MessageTemplate
			.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

	protected static final MessageTemplate REQUEST_MSG_TEMPLATE_PERFORMATIVE = MessageTemplate
			.MatchPerformative(ACLMessage.REQUEST);

	protected static final MessageTemplate REQUEST_MSG_TEMPLATE = MessageTemplate
			.and(MSG_TEMPLATE_PROTOCOL, REQUEST_MSG_TEMPLATE_PERFORMATIVE);

	// --- Class Variables -----------------------------------------------------

	private static Logger LOG = Logger
			.getLogger(RequestInteractionProtocolResponder.class.getName());

	// --- Constructors --------------------------------------------------------

	public RequestInteractionProtocolResponder(Agent agent, DataStore store) {
		super(agent, REQUEST_MSG_TEMPLATE, store);
	}

	// --- Methods (abstract) --------------------------------------------------

	/**
	 * Makes query to the agent in order to understand whether the agent will
	 * pay or not.
	 * 
	 * @param price
	 *            The proposed price for paying.
	 * @return Returns <code>true</code> if the agent is agree to pay and
	 *         <code>false</code> otherwise.
	 */
	protected abstract boolean isReadyToPay(double price);

	/**
	 * Invokes the executePayment of the consumer agent in order to be paid the
	 * amount due.
	 * 
	 * @param aid
	 *            The <code>AID</code> of the producer.
	 * @param price
	 *            The price which must be paid.
	 * @return Returns <code>true</code> if the payment finished successfully
	 *         and <code>false</code> otherwise.
	 */
	protected abstract boolean executePayment(AID aid, double price);

	// --- Methods (inherited by Behaviour) ------------------------------------

	@Override
	public void onStart() {
		super.onStart();
	}

	// --- Methods (inherited by AchieveREResponder) ---------------------------

	@Override
	protected ACLMessage handleRequest(ACLMessage request)
			throws NotUnderstoodException, RefuseException {
		if (request == null) {
			throw new NotUnderstoodException("Invalid request msg: null.");
		}
		String content = request.getContent();
		if (content == null || content.isEmpty()) {
			throw new NotUnderstoodException("Invalid request msg content: "
					+ content);
		}
		String priceStr = content
				.substring(RequestInteractionProtocolInitiator.PAYMENT_EXPECTED
						.length());
		Double price = null;
		try {
			price = Double.valueOf(priceStr.trim());
		} catch (NumberFormatException nfe) {
			throw new NotUnderstoodException(
					"Invalid request content - price: " + priceStr);
		}

		// if the consumer is not ready to pay, REFUSE message is sent as answer
		if (!isReadyToPay(price)) {
			ACLMessage refuseMsg = request.createReply();
			refuseMsg.setPerformative(ACLMessage.REFUSE);
			refuseMsg.setContent("Can not pay the price.");
			log("Request handled by the creation of refuse msg: \n" + refuseMsg);
			return refuseMsg;
		}

		// if the payment finish successfully, INFORM message is sent as answer
		if (executePayment(request.getSender(), price)) {
			ACLMessage informDoneMsg = request.createReply();
			informDoneMsg.setPerformative(ACLMessage.INFORM);
			informDoneMsg.setContent("Payment finished successfully");
			log("Request handled by the creation of inform-done msg: \n"
					+ informDoneMsg);
			return informDoneMsg;
		}

		// if the payment fails, FAILURE message is sent as answer
		ACLMessage failureMsg = request.createReply();
		failureMsg.setPerformative(ACLMessage.FAILURE);
		failureMsg.setContent("Payment procedure failed.");
		log("Request handled by the creation of failure msg: \n" + failureMsg);
		return failureMsg;
	}

	// --- Methods -------------------------------------------------------------

	private void log(String msg) {
		Level logLevel = ProdConsConfiguration.instance().getLogLevel();
		if (LOG.isLoggable(logLevel)) {
			LOG.log(logLevel, REQUEST_IP_RESPONDER + " ["
					+ myAgent.getLocalName() + "] " + msg + "\n");
		}
	}
}
