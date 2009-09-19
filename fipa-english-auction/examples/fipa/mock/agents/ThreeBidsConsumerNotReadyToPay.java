package fipa.mock.agents;

public class ThreeBidsConsumerNotReadyToPay extends ThreeBidsConsumer {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = -8080303418827842010L;

	// --- Constructors --------------------------------------------------------

	public ThreeBidsConsumerNotReadyToPay() {
		this.sdName = "three-bid-agent-not-ready-to-pay" + hashCode();
		this.sdType = "three-bid-agent-not-ready-to-pay" + hashCode();
	}

	// --- Methods (inherited by Consumer) -------------------------------------

	@Override
	public boolean isReadyToPay(double price) {
		return false;
	}
}
