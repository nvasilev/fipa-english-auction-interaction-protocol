package fipa.impl.protocol.auction.english;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import fipa.agent.Producer;
import fipa.protocol.auction.english.EnglishAuctionInitiator;

/**
 * Class responsible for the connection between the
 * <code>English Auction Interaction Protocol</code> behaviour implementation
 * and the producer agent.
 * 
 * @author Nikolay Vasilev
 */
public class EnglishAuctionInitiatorImpl extends EnglishAuctionInitiator {

    // --- Constants -----------------------------------------------------------

    private static final long serialVersionUID = -3509028894807886168L;

    // --- Constructors --------------------------------------------------------

    public EnglishAuctionInitiatorImpl(Agent agent, ACLMessage inform,
	    DataStore dataStore) {
	super(agent, inform, dataStore);
    }

    // --- Methods (inherited by EnglishAuctionInitiator) ----------------------

    @Override
    protected List<ACLMessage> prepareInitiations(ACLMessage informInitiationMsg) {
	List<ACLMessage> initiationsList = new ArrayList<ACLMessage>(1);
	initiationsList.add(informInitiationMsg);
	return initiationsList;
    }

    @Override
    protected String getCfpMsgContent() {
	Producer producer = (Producer) myAgent;
	double price = producer.getPrice();
	return Double.toString(price);
    }
}
