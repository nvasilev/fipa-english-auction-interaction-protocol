package fipa.mock.agents;

import fipa.agent.Consumer;
import fipa.agent.Producer;
import fipa.protocol.prodcons.ProducerBehaviour;

/**
 * Fictive implementation of the {@link Consumer} interface. Created only with
 * testing purpose. It is going to be used until real interface implementation
 * is supplied by the team responsible for the implementation of the producer
 * agent.
 * 
 * @author Nikolay Vasilev
 */
public class ProducerMockAgent extends MockAgent implements Producer {

    // --- Constants -----------------------------------------------------------

    // bloody eclipse
    private static final long serialVersionUID = 5584489281431406739L;

    private static final double INITIAL_PRICE = 10;

    // --- Instance Variables --------------------------------------------------

    private ProducerBehaviour pb;
    private double lastProposedPrice;

    // --- Constructor ---------------------------------------------------------

    public ProducerMockAgent() {
	this.sdType = "producer";
	this.sdName = "piruleta-production";
	lastProposedPrice = -1;
    }

    // --- Methods (Agent) -----------------------------------------------------

    @Override
    protected void setup() {
	registerService();
	// arranging little delay, until the consumer agents are loaded
	try {
	    Thread.sleep(1000l);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	// ...
	pb = new ProducerBehaviour(this);
	addBehaviour(pb);
	// ...
    }

    // --- Methods (Producer) --------------------------------------------------

    @Override
    public double getPrice() {
	if (lastProposedPrice == -1) {
	    lastProposedPrice = INITIAL_PRICE;
	    return lastProposedPrice;
	}
	++lastProposedPrice;
	return lastProposedPrice;
    }

    @Override
    public void handleTerminateEvent(AuctionTerminationEvent event) {
	// do nothing
    }
}
