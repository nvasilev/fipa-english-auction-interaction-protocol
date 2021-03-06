package fipa.examples;

import fipa.mock.agents.ThreeBidsConsumerReadyToPayWithPaymentFailure;

/**
 * Class which shows example of bidding between one producer and three
 * consumers. The first consumer (<code>consumer1</code>) bids only the first
 * time. The second consumer (<code>consumer2</code>) bids only the first two
 * times. The third consumer (<code>consumer3</code>) bids only the first three
 * times, but in that way he wins the auction. Afterwards he does not refuse to
 * pay and the payment finishes successfully.
 * 
 * <p>
 * There is a <code>Sniffer</code> agent which is used for visualization of the
 * communication between the producer and consumers.
 * </p>
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class ExampleProdConsPaymentFails extends ExampleProdCons {

	// --- Constructors --------------------------------------------------------

	public ExampleProdConsPaymentFails() {
		nickname2ClassMap.put("consumer3",
				ThreeBidsConsumerReadyToPayWithPaymentFailure.class);
	}

	// --- Methods -------------------------------------------------------------

	public static void main(String[] args) {
		(new ExampleProdConsPaymentFails()).executeExample();
	}
}
