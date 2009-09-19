package fipa.protocol.prodcons;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;
import fipa.impl.protocol.auction.english.EnglishAuctionParticipantImpl;
import fipa.impl.protocol.request.interaction.RequestInteractionProtocolResponderImpl;
import fipa.protocol.auction.english.EnglishAuctionParticipant;

/**
 * Behaviour which must be used by the consumer-agent. It encloses the
 * implementations of the initiator's part of the both protocols
 * <code>FIPA English Auction Interaction Protocol</code> and correspondingly
 * the <code>FIPA Request Interaction Protocol</code>. First the
 * <code>English Auction IP</code> is invoked, and afterwards if
 * <code>myAgent</code> is winner in the bidding, is executed the
 * <code>Request IP</code>.
 * 
 * @author Ruben Rios
 * @author Nikolay Vasilev
 */
public abstract class ConsumerBehaviour extends FSMBehaviour {

	// --- Constants -----------------------------------------------------------

	// bloody eclipse ;)
	private static final long serialVersionUID = 6832626727315693679L;

	private static final String CONSUMER = "[Consumer] ";

	// --- Constants (States) --------------------------------------------------

	protected static final String ENGLISH_AUCTION = "English-Auction";
	protected static final String REQUEST_INTERACTION_PROTOCOL = "Request-Interaction-Protocol";
	protected static final String DUMMY_END = "End";

	// --- Constants (states exit values) --------------------------------------

	protected static final int REQUEST_EXPECTED = EnglishAuctionParticipant.REQUEST_EXPECTED;

	// --- Class Variables -----------------------------------------------------

	private static Logger LOG = Logger.getLogger(ConsumerBehaviour.class
			.getName());

	// --- Constructors --------------------------------------------------------

	public ConsumerBehaviour(Agent agent) {
		this(agent, new DataStore());
	}

	public ConsumerBehaviour(Agent agent, DataStore store) {
		super(agent);

		setDataStore(store);

		// //////////////////////// REGISTER TRANSITIONS ///////////////////////
		{
			registerDefaultTransition(ENGLISH_AUCTION, DUMMY_END);
			registerTransition(ENGLISH_AUCTION, REQUEST_INTERACTION_PROTOCOL,
					REQUEST_EXPECTED);
			registerDefaultTransition(REQUEST_INTERACTION_PROTOCOL, DUMMY_END);
		}

		// //////////////////////// REGISTER STATES ////////////////////////////

		// ENGLISH_AUCTION
		{
			Behaviour englishAuction = new EnglishAuctionParticipantImpl(
					myAgent, getDataStore());
			englishAuction.setDataStore(getDataStore());
			registerFirstState(englishAuction, ENGLISH_AUCTION);
		}

		// REQUEST_INTERACTION_PROTOCOL
		{
			Behaviour prepareRequest = new RequestInteractionProtocolResponderImpl(
					myAgent, getDataStore());
			registerState(prepareRequest, REQUEST_INTERACTION_PROTOCOL);
		}

		// DUMMY_END
		{
			Behaviour dummyEnd = new OneShotBehaviour(myAgent) {
				private static final long serialVersionUID = -4216350173804405598L;

				public void action() {
					log("End consumer's behaviour.");
				}
			};
			registerLastState(dummyEnd, DUMMY_END);
		}

	}

	// --- Methods (abstract) --------------------------------------------------

	/**
	 * Returns <code>true</code> if the price offered by the producer for
	 * selling the stock is accepted by the consumer and <code>false</code>
	 * otherwise.
	 * 
	 * @return Returns <code>true</code> if the price offered by the producer
	 *         for selling the stock is accepted by the consumer and
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean isPriceAcceptable(double offeredPrice);

	/**
	 * Checks if the consumer is ready to pay the asked price.
	 * 
	 * @param price
	 *            The price that is expected to be paid.
	 * @return Returns <code>true</code> if the consumer is ready to pay the
	 *         price, and <code>false</code> otherwise.
	 */
	public abstract boolean isReadyToPay(double price);

	/**
	 * Executes the payment to the producer.
	 * 
	 * @param aid
	 *            The AID of the producer to whom this consumer is goign to pay.
	 * @param price
	 *            The price which must be paid to the producer.
	 * @return Returns <code>true</code> if the payment finished successfully
	 *         and <code>false</code> otherwise.
	 */
	public abstract boolean executePayment(AID aid, double price);

	// --- Methods -------------------------------------------------------------

	private void log(String msg) {
		Level logLevel = ProdConsConfiguration.instance().getLogLevel();
		if (LOG.isLoggable(logLevel)) {
			LOG.log(logLevel, CONSUMER + " [" + myAgent.getLocalName() + "] "
					+ msg + "\n");
		}
	}
}
