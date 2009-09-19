package fipa.protocol.prodcons;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;
import fipa.impl.protocol.auction.english.EnglishAuctionInitiatorFactory;
import fipa.protocol.auction.english.EnglishAuctionInitiator;
import fipa.protocol.request.interaction.RequestInteractionProtocolInitiator;

/**
 * Behaviour which must be used by the producer-agent. It encloses the
 * implementations of the initiator's part of the both protocols
 * <code>FIPA English Auction Interaction Protocol</code> and correspondingly
 * the <code>FIPA Request Interaction Protocol</code>. First the
 * <code>English Auction IP</code> is invoked, and after there is winner from
 * the bidding, is executed the <code>Request IP</code>.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public abstract class ProducerBehaviour extends FSMBehaviour {

	// --- Enums ---------------------------------------------------------------

	/**
	 * Enumeration which is used in the communication between the agent and the
	 * behaviour. Its aim is to notify the producer for the exit of the auction.
	 */
	public enum AuctionTerminationEvent {
		NO_WINNER,

		WINNER_REFUSE_TO_PAY,

		PAYMENT_FAILURE,

		PAYMENT_OK
	}

	// --- Constants -----------------------------------------------------------

	// bloody eclipse ;)
	private static final long serialVersionUID = 2898778289967324086L;

	private static final String PRODUCER = "[Producer-Protocol] ";

	// --- Constants (States) --------------------------------------------------

	protected static final String ENGLISH_AUCTION = "English-Auction";
	protected static final String REQUEST_INTERACTION_PROTOCOL = "Request-Interaction-Protocol";
	protected static final String DUMMY_END = "End";

	// --- Constants (state exit values) --------------------------------------

	protected static final int WINNER_REQUEST_PREPARED = EnglishAuctionInitiator.WINNER_REQUEST_PREPARED;

	// --- Class Variables -----------------------------------------------------

	private static Logger LOG = Logger.getLogger(ProducerBehaviour.class
			.getName());

	// --- Constructors --------------------------------------------------------

	public ProducerBehaviour(Agent agent) {
		this(agent, new DataStore());
	}

	public ProducerBehaviour(Agent agent, DataStore store) {
		super(agent);
		setDataStore(store);

		// //////////////////////// REGISTER TRANSITIONS ///////////////////////
		{
			registerDefaultTransition(ENGLISH_AUCTION, DUMMY_END);
			registerTransition(ENGLISH_AUCTION, REQUEST_INTERACTION_PROTOCOL,
					WINNER_REQUEST_PREPARED);
			registerDefaultTransition(REQUEST_INTERACTION_PROTOCOL, DUMMY_END);
		}

		// //////////////////////// REGISTER STATES ////////////////////////////

		// ENGLISH_AUCTION
		{
			Behaviour englishAuction = EnglishAuctionInitiatorFactory
					.instance().createEnglishAuctionInitiator(myAgent,
							getDataStore());
			englishAuction.setDataStore(getDataStore());
			registerFirstState(englishAuction, ENGLISH_AUCTION);
		}

		// REQUEST_INTERACTION_PROTOCOL
		{
			Behaviour prepareRequest = new RequestInteractionProtocolInitiator(
					myAgent, null, getDataStore());
			registerState(prepareRequest, REQUEST_INTERACTION_PROTOCOL);
		}

		// DUMMY_END
		{
			Behaviour dummyEnd = new OneShotBehaviour(myAgent) {
				private static final long serialVersionUID = -4216350173804405598L;

				public void action() {
					log("End producer's behaviour.");
				}
			};
			registerLastState(dummyEnd, DUMMY_END);
		}
	}

	// --- Methods (abstract) --------------------------------------------------

	/**
	 * Returns the new price offered by the producer.
	 * 
	 * @return Returns the new price offered by the producer.
	 */
	public abstract double getPrice();

	/**
	 * Notifies the producer for the exit event from the auction.
	 * 
	 * @param event
	 *            The termination event from the auction.
	 */
	public abstract void handleTerminateEvent(AuctionTerminationEvent event);

	// --- Methods -------------------------------------------------------------

	private void log(String msg) {
		Level logLevel = ProdConsConfiguration.instance().getLogLevel();
		if (LOG.isLoggable(logLevel)) {
			LOG.log(logLevel, PRODUCER + " [" + myAgent.getLocalName() + "] "
					+ msg + "\n");
		}
	}
}
