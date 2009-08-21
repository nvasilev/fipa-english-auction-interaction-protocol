package fipa.mock.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Mock agent used for test and example purposes.
 * 
 * @author Nikolay Vasilev
 */
public abstract class MockAgent extends Agent {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = 8709831035051753194L;

    // --- Instance Variables --------------------------------------------------

    protected String sdType;
    protected String sdName;

    // --- Constructors --------------------------------------------------------

    public MockAgent() {
    }

    // --- Getters and Setters -------------------------------------------------

    public String getSdType() {
	return sdType;
    }

    public void setSdType(String sdType) {
	this.sdType = sdType;
    }

    public String getSdName() {
	return sdName;
    }

    public void setSdName(String sdName) {
	this.sdName = sdName;
    }

    // --- Methods -------------------------------------------------------------

    /**
     * Registers the service that this agent offers in the yellow pages.
     */
    protected void registerService() {
	DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setType(getSdType());
	sd.setName(getSdName());
	dfd.addServices(sd);
	try {
	    DFService.register(this, dfd);
	} catch (FIPAException fe) {
	    throw new IllegalStateException(
		    "Appeared problem during the service registration.", fe);
	}
    }

    /**
     * Deregisters the service that this agent offers in the yellow pages.
     */
    protected void deregisterService() {
	try {
	    DFService.deregister(this);
	} catch (FIPAException fe) {
	    throw new IllegalStateException(
		    "Appeared problem during the service deregistration.", fe);
	}
    }
}
