package fipa.mock.agents;

import jade.core.AID;
import fipa.agent.Consumer;
import fipa.impl.protocol.auction.english.EnglishAuctionInitiatorFactory;
import fipa.protocol.prodcons.ConsumerBehaviour;

/**
 * Fictive implementation of the {@link Consumer} interface. Created only with
 * testing purpose. It is going to exist until real interface implementation is
 * supplied by the team responsible for the implementation of the consumer
 * agent.
 * 
 * @author Nikolay Vasilev
 */
public class ConsumerMockAgent extends MockAgent implements Consumer {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = -414529357634217116L;

    // --- Instance Variables --------------------------------------------------

    /**
     * Instance variable used for counting how many times the consumer agents
     * are going to bid during one auction session.
     */
    private int biddingParticipationCnt = -1;

    // --- Constructors --------------------------------------------------------

    public ConsumerMockAgent(int biddingParticipationCnt) {
	this.sdType = EnglishAuctionInitiatorFactory.SEARCHED_SERVICE_DESC_TYPE;
	this.sdName = "piruleta-eating";
	this.biddingParticipationCnt = biddingParticipationCnt;
    }

    // --- Methods (inherited by Agent) ----------------------------------------

    @Override
    protected void setup() {
	registerService();
	// ...
	ConsumerBehaviour cb = new ConsumerBehaviour(this);
	addBehaviour(cb);
	// ...
    }

    @Override
    protected void takeDown() {
	deregisterService();
    }

    // --- Methods (inherited by Consumer) -------------------------------------

    @Override
    public boolean isPriceAcceptable(double offeredPrice) {
	boolean isAcceptable = biddingParticipationCnt > 0;
	biddingParticipationCnt--;
	return isAcceptable;
    }

    @Override
    public boolean isReadyToPay(double price) {
	return true;
    }

    @Override
    public boolean executePayment(AID aid, double price) {
	return true;
    }
}
