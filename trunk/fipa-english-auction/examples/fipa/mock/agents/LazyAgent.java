package fipa.mock.agents;

public class LazyAgent extends DoNothingAgent {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 7506222461496168925L;

	// --- Constructors --------------------------------------------------------

	public LazyAgent() {
		this.sdName = "lazy-agent" + hashCode();
		this.sdType = "lazy-agent" + hashCode();
	}
}
