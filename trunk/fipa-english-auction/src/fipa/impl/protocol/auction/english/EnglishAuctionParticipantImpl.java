package fipa.impl.protocol.auction.english;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import fipa.protocol.auction.english.EnglishAuctionParticipant;
import fipa.protocol.prodcons.ConsumerBehaviour;

/**
 * Class responsible for the connection between the
 * <code>English Auction Interaction Protocol</code> behaviour implementation
 * and the consumer agent.
 * 
 * @author Ruben Rios
 * @author Nikolay Vasilev
 */
public class EnglishAuctionParticipantImpl extends EnglishAuctionParticipant {

	// --- Constants -----------------------------------------------------------

	private static final long serialVersionUID = 7268235927256700412L;

	// --- Constructors --------------------------------------------------------

	public EnglishAuctionParticipantImpl(Agent agent, DataStore store) {
		super(agent, INITIATION_INFORM_MSG_TEMPLATE, store);
	}

	// --- Methods (inherited by EnglishAuctionParticipant) --------------------

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,
			FailureException, NotUnderstoodException {
		if (cfp == null) {
			throw new FailureException("Invalid CFP message: null");
		}
		String cfpContent = cfp.getContent();
		double offeredPrice = getOfferedPrice(cfpContent);
		ConsumerBehaviour parent = (ConsumerBehaviour) getParent();
		// if the offered price is not accepted by the consumer the returned msg
		// is null, which means that the consumer does not want to reply to the
		// proposal
		if (!parent.isPriceAcceptable(offeredPrice)) {
			return null;
		}

		ACLMessage reply = cfp.createReply();
		reply.setPerformative(ACLMessage.PROPOSE);
		reply.setInReplyTo(cfp.getInReplyTo());
		reply.setContent(Double.toString(offeredPrice));

		return reply;
	}

	// --- Methods -------------------------------------------------------------

	/**
	 * Obtains the offered by the producer price.
	 * 
	 * @param cfpContent
	 *            The <code>CFP</code> message from which is going to be
	 *            extracted the price.
	 * 
	 * @return Returns the price proposed with the <code>CFP</code> message.
	 * @throws NotUnderstoodException
	 *             Throws <code>NotUnderstoodException</code> exception in case
	 *             the content of the <code>CFP</code> message is not valid
	 */
	protected double getOfferedPrice(String cfpContent)
			throws NotUnderstoodException {
		if (cfpContent == null || cfpContent.trim().isEmpty()) {
			throw new NotUnderstoodException("Invalid CFP msg content: "
					+ cfpContent);
		}
		double offeredPrice = -1;
		try {
			offeredPrice = Double.parseDouble(cfpContent.trim());
		} catch (NumberFormatException nfe) {
			throw new NotUnderstoodException(
					"Invlaid CFP Msg content (not a double number): "
							+ cfpContent.trim());
		}
		return offeredPrice;
	}
}
