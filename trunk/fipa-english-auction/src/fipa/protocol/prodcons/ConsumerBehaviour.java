package fipa.protocol.prodcons;

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


public class ConsumerBehaviour extends FSMBehaviour {

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
    // protected static final int PROPOSE_RECEIVED = 1;
    // protected static final int ALL_PROPOSES_RECEIVED = 2;
    // protected static final int END_OF_BIDDING = 0;

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

    // --- Methods -------------------------------------------------------------

    private void log(String msg) {
	Level logLevel = ProdConsConfiguration.instance().getLogLevel();
	if (LOG.isLoggable(logLevel)) {
	    LOG.log(logLevel, CONSUMER + " [" + myAgent.getLocalName() + "] "
		    + msg + "\n");
	}
    }
}
