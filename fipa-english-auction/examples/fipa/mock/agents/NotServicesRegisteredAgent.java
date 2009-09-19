package fipa.mock.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

/**
 * Dummy class for agent, which does NOT register any service in the yellow
 * pages, and therefore is not discovered by the producer during the auction.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public class NotServicesRegisteredAgent extends Agent {

	// --- Constants -----------------------------------------------------------
	private static final long serialVersionUID = 4648962952944015383L;

	// --- Constructors --------------------------------------------------------

	public NotServicesRegisteredAgent() {
	}

	// --- Methods (Agent) -----------------------------------------------------

	@Override
	protected void setup() {
		Behaviour b = new CyclicBehaviour() {
			private static final long serialVersionUID = 2397904988995198325L;

			@Override
			public void action() {
				// do nothing, but sleep
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		addBehaviour(b);
	}
}
