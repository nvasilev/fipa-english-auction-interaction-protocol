package fipa.mock.agents;

import jade.core.AID;

public class ThreeBidsConsumerReadyToPayPaymentOK extends ThreeBidsConsumer {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = 7199719606519097440L;

    // --- Constructors --------------------------------------------------------

    public ThreeBidsConsumerReadyToPayPaymentOK() {
	super();
    }

    // --- Methods (inherited by Consumer) -------------------------------------

    @Override
    public boolean isReadyToPay(double price) {
	return true;
    }

    @Override
    public boolean executePayment(AID aid, double price) {
	return true;
    }
}