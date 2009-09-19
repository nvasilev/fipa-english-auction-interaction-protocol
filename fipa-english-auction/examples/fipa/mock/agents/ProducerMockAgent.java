package fipa.mock.agents;

import fipa.protocol.prodcons.ProducerBehaviour;

/**
 * Fictive implementation of the {@link Consumer} interface. Created only with
 * testing purpose. It is going to be used until real interface implementation
 * is supplied by the team responsible for the implementation of the producer
 * agent.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class ProducerMockAgent extends MockAgent {

	// --- Constants -----------------------------------------------------------

	// bloody eclipse
	private static final long serialVersionUID = 5584489281431406739L;

	private static final double INITIAL_PRICE = 10;

	// --- Instance Variables --------------------------------------------------

	private ProducerBehaviour pb;
	private double lastProposedPrice;

	// --- Constructor ---------------------------------------------------------

	public ProducerMockAgent() {
		this.sdType = "producer" + hashCode();
		this.sdName = "piruleta-production" + hashCode();
		lastProposedPrice = -1;
	}

	// --- Methods (Agent) -----------------------------------------------------

	@Override
	protected void setup() {
		super.setup();
		// arranging little delay, until the consumer agents are loaded
		try {
			Thread.sleep(2000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// ...
		pb = new ProducerBehaviour(this) {
			private static final long serialVersionUID = 1938386002679822533L;

			@Override
			public double getPrice() {
				return ((ProducerMockAgent) myAgent).getPrice();
			}

			@Override
			public void handleTerminateEvent(AuctionTerminationEvent event) {
				((ProducerMockAgent) myAgent).handleTerminateEvent(event);
			}
		};
		addBehaviour(pb);
		// ...
	}

	// --- Methods -------------------------------------------------------------

	public double getPrice() {
		if (lastProposedPrice == -1) {
			lastProposedPrice = INITIAL_PRICE;
			return lastProposedPrice;
		}
		++lastProposedPrice;
		return lastProposedPrice;
	}

	public void handleTerminateEvent(
			ProducerBehaviour.AuctionTerminationEvent event) {
		// do nothing
	}
}
