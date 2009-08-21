package fipa.mock.agents;

/**
 * Mock consumer agent which bids the first three times, and misses all other
 * bidding iterations.
 * 
 * @author Nikolay Vasilev
 */
public abstract class ThreeBidsConsumer extends ConsumerMockAgent {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = -5186297180014452011L;

    // --- Constructors --------------------------------------------------------

    public ThreeBidsConsumer() {
	super(3);
    }
}
