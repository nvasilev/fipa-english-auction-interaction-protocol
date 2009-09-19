package fipa.mock.agents;

/**
 * Mock consumer agent which executes bidding only once, and misses all other
 * bidding iterations.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class OnceBidConsumer extends ConsumerMockAgent {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 8540426195260835551L;

	// --- Constructors --------------------------------------------------------

	public OnceBidConsumer() {
		super(1);
		this.sdName = "one-bid-agent" + hashCode();
		this.sdType = "one-bid-agent" + hashCode();
	}
}
