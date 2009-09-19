package fipa.mock.agents;

import jade.core.AID;

public class ThreeBidsConsumerReadyToPayWithPaymentFailure extends
		ThreeBidsConsumer {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 2405119923812595103L;

	// --- Constructors --------------------------------------------------------

	public ThreeBidsConsumerReadyToPayWithPaymentFailure() {
		this.sdName = "three-bid-agent-payment-failure" + hashCode();
		this.sdType = "three-bid-agent-payment-failure" + hashCode();
	}

	// --- Methods (inherited by Consumer) -------------------------------------

	@Override
	public boolean isReadyToPay(double price) {
		return true;
	}

	@Override
	public boolean executePayment(AID aid, double price) {
		return false;
	}
}
