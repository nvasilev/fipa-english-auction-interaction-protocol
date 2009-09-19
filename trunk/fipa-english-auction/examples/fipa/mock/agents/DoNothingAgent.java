package fipa.mock.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;

public abstract class DoNothingAgent extends MockAgent {

	// --- Constants -----------------------------------------------------------
	private static final long serialVersionUID = 1010977061910730301L;

	// --- Class Variables -----------------------------------------------------
	private static Logger LOG = Logger
			.getLogger(DoNothingAgent.class.getName());

	// --- Constructors --------------------------------------------------------
	public DoNothingAgent() {
	}

	@Override
	protected void setup() {
		super.setup();
		Behaviour b = new ListenInOn(this);
		addBehaviour(b);
	}

	// --- Nested Classes ------------------------------------------------------

	private static class ListenInOn extends OneShotBehaviour {
		private static final String DO_NOTHIHNG_AGENT = "[Do-Nothing-Agent] ";
		private boolean finished = false;

		public ListenInOn(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				log("Received msg: \n" + msg);
			} else {
				block();
			}
		}

		private void log(String msg) {
			Level logLevel = ProdConsConfiguration.instance().getLogLevel();
			if (LOG.isLoggable(logLevel)) {
				LOG.log(logLevel, DO_NOTHIHNG_AGENT + " ["
						+ myAgent.getLocalName() + "] " + msg + "\n");
			}
		}
	}
}
