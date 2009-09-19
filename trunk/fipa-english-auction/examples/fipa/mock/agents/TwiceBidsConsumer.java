package fipa.mock.agents;

/**
 * Mock consumer agent which executes bidding only the first two times, and
 * misses all other bidding iterations.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class TwiceBidsConsumer extends ConsumerMockAgent {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = -5186297180014452011L;

	// --- Constructors --------------------------------------------------------

	public TwiceBidsConsumer() {
		super(2);
		this.sdName = "two-bid-agent" + hashCode();
		this.sdType = "two-bid-agent" + hashCode();
	}
}
