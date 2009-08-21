package fipa.mock.agents;

public class ThreeBidsConsumerNotReadyToPay extends ThreeBidsConsumer {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = -8080303418827842010L;

    // --- Constructors --------------------------------------------------------

    public ThreeBidsConsumerNotReadyToPay() {
	super();
    }

    // --- Methods (inherited by Consumer) -------------------------------------

    @Override
    public boolean isReadyToPay(double price) {
	return false;
    }
}
