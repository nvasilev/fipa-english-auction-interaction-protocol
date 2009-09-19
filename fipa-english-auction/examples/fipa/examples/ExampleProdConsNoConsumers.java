package fipa.examples;

import jade.core.Agent;

import java.util.HashMap;

import fipa.mock.agents.LazyAgent;
import fipa.mock.agents.ProducerMockAgent;
import fipa.mock.agents.SleepyAgent;

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
public class ExampleProdConsNoConsumers extends ExampleProdCons {

	// --- Constructor ---------------------------------------------------------

	public ExampleProdConsNoConsumers() {
		nickname2ClassMap = new HashMap<String, Class<? extends Agent>>();
		nickname2ClassMap.put("lazy-agent", LazyAgent.class);
		nickname2ClassMap.put("sleepy-agent", SleepyAgent.class);
		nickname2ClassMap.put("producer", ProducerMockAgent.class);
	}

	// --- Methods -------------------------------------------------------------

	public static void main(String[] args) {
		(new ExampleProdConsNoConsumers()).executeExample();
	}
}
