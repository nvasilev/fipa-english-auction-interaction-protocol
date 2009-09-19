package fipa.mock.agents;

public class SleepyAgent extends DoNothingAgent {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = -7487404277456278854L;

	// --- Constructors --------------------------------------------------------

	public SleepyAgent() {
		this.sdName = "sleepy-agent" + hashCode();
		this.sdType = "sleepy-agent" + hashCode();
	}
}
