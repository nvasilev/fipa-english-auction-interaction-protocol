package fipa.mock.agents;

import jade.core.AID;
import fipa.protocol.prodcons.ConsumerBehaviour;

/**
 * Fictive implementation of the {@link Consumer} interface. Created only with
 * testing purpose. It is going to exist until real interface implementation is
 * supplied by the team responsible for the implementation of the consumer
 * agent.
 * 
 * @author Ruben Rios
 * @author Nikolay Vasilev
 */
public class ConsumerMockAgent extends MockAgent {

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
		// this.sdType =
		// EnglishAuctionInitiatorFactory.SEARCHED_SERVICE_DESC_TYPE;
		this.sdName = "piruleta-eating" + hashCode();
		this.biddingParticipationCnt = biddingParticipationCnt;
	}

	// --- Methods (inherited by Agent) ----------------------------------------

	@Override
	protected void setup() {
		super.setup();
		// ...
		ConsumerBehaviour cb = new ConsumerBehaviour(this) {

			private static final long serialVersionUID = -5673575923480301964L;

			@Override
			public boolean isPriceAcceptable(double offeredPrice) {
				return ((ConsumerMockAgent) myAgent)
						.isPriceAcceptable(offeredPrice);
			}

			@Override
			public boolean isReadyToPay(double price) {
				return ((ConsumerMockAgent) myAgent).isReadyToPay(price);
			}

			@Override
			public boolean executePayment(AID aid, double price) {
				return ((ConsumerMockAgent) myAgent).executePayment(aid, price);
			}
		};
		addBehaviour(cb);
		// ...
	}

	// --- Methods -------------------------------------------------------------

	protected boolean isPriceAcceptable(double offeredPrice) {
		boolean isAcceptable = biddingParticipationCnt > 0;
		biddingParticipationCnt--;
		return isAcceptable;
	}

	protected boolean isReadyToPay(double price) {
		return true;
	}

	protected boolean executePayment(AID aid, double price) {
		return true;
	}
}
