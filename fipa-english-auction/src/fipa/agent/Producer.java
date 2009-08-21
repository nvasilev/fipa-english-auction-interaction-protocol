package fipa.agent;

/**
 * Fictive interface, created with only testing purpose. It is going to exist
 * until real interface is supported by the responsible team.
 * 
 * @author Nikolay Vasilev
 */
public interface Producer {

    /**
     * Enumeration which is used in the communication between the agent and the
     * behaviour. Its aim is to notify the producer for the exit of the auction.
     */
    public enum AuctionTerminationEvent {
	NO_WINNER,

	WINNER_REFUSE_TO_PAY,

	PAYMENT_FAILURE,

	PAYMENT_OK
    }

    /**
     * Returns the new price offered by the producer.
     * 
     * @return Returns the new price offered by the producer.
     */
    double getPrice();

    /**
     * Notifies the producer for the exit event from the auction.
     * 
     * @param event
     *            The termination event from the auction.
     */
    void handleTerminateEvent(AuctionTerminationEvent event);
}
