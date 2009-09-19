package fipa.protocol.request.interaction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;
import fipa.protocol.auction.english.EnglishAuctionInitiator;

/**
 * Class which implements the main functionality from the
 * <code>FIPA Request Interaction Protocol</code> from the side of the
 * initiator.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class RequestInteractionProtocolInitiator extends AchieveREInitiator {

	// --- Constants -----------------------------------------------------------
	private static final long serialVersionUID = -6614401932014459786L;

	private static final String REQUEST_IP_INITIATOR = "[Request-IP-Initiator]";
	public static final String PAYMENT_EXPECTED = "Payment expected.";

	// --- Constants (state exit values) ---------------------------------------
	private static final String WINNER_REQUEST = EnglishAuctionInitiator.WINNER_REQUEST;

	// --- Class Variables -----------------------------------------------------
	private static Logger LOG = Logger
			.getLogger(RequestInteractionProtocolInitiator.class.getName());

	// --- Constructors --------------------------------------------------------
	public RequestInteractionProtocolInitiator(Agent agent, ACLMessage msg,
			DataStore store) {
		super(agent, msg, store);
	}

	// --- Methods (inherited by Behaviour) ------------------------------------

	@Override
	public void onStart() {
		ACLMessage requestInitiation = getRequest();
		super.reset(requestInitiation);
		super.onStart();
	}

	@Override
	public int onEnd() {
		getDataStore().remove(WINNER_REQUEST);
		return super.onEnd();
	}

	// --- Methods (inherited by AchieveREInitiator) ---------------------------
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		// do nothing
		log("Refuse msg handled successfully: \n" + refuse);
	}

	@Override
	protected void handleAgree(ACLMessage agree) {
		// do nothing
		log("Agree msg handled successfully: \n" + agree);
	}

	@Override
	protected void handleFailure(ACLMessage failure) {
		// do nothing
		log("Failure msg handled successfully: \n" + failure);
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		// do nothing
		log("Inform msg handled successfully: \n" + inform);
	}

	// --- Methods -------------------------------------------------------------

	/**
	 * Obtains the request left in the data store from the
	 * <code>English Auction IP</code>, and prepares it for creation of the
	 * initiation message of the <code>Request IP</code>.
	 * 
	 * @return Returns the initiation request message for starting the
	 *         <code>Request IP</code>, using the request message stored by the
	 *         <code>English Auction IP</code>. If there is no previously stored
	 *         message, <code>null</code> is returned as result.
	 */
	private ACLMessage getRequest() {
		ACLMessage winnerRequest = (ACLMessage) getDataStore().get(
				WINNER_REQUEST);
		if (winnerRequest == null) {
			return null;
		}
		ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setSender(myAgent.getAID());
		Iterator receiversIt = winnerRequest.getAllIntendedReceiver();
		requestMsg.addReceiver((AID) receiversIt.next());
		requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		requestMsg.setPerformative(ACLMessage.REQUEST);
		requestMsg.setContent(winnerRequest.getContent());

		log("Created the following request: \n" + requestMsg);

		return requestMsg;
	}

	private void log(String msg) {
		Level logLevel = ProdConsConfiguration.instance().getLogLevel();
		if (LOG.isLoggable(logLevel)) {
			LOG.log(logLevel, REQUEST_IP_INITIATOR + " ["
					+ myAgent.getLocalName() + "] " + msg + "\n");
		}
	}
}
