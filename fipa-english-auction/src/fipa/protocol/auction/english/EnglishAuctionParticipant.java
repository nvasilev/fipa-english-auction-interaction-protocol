package fipa.protocol.auction.english;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.proto.states.ReplySender;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;

/**
 * Class implementing the participant part of the
 * <code>FIPA English Auction Interaction Protocol</code>.
 * 
 * @author Nikolay Vasilev
 */
public class EnglishAuctionParticipant extends FSMBehaviour {

    // --- Constants -----------------------------------------------------------

    // bloody eclipse ;)
    private static final long serialVersionUID = -1969180655480478032L;

    private static final String EA_PARTICIPANT = "[EA-Participant] ";
    protected static final long DEADLINE = NextMsgReceiver.INFINITE;

    // --- Constants (States) --------------------------------------------------

    private static final String RECEIVE_INITIATION_INFORM = "Receive-Initiation-Inform";
    private static final String RECEIVE_NEXT = "Receive-Next";
    protected static final String CHECK_IN_SEQ = "Check-In-Seq";
    public static final String HANDLE_CFP = "Handle-Cfp";
    protected static final String HANDLE_OUT_OF_SEQUENCE = "Handle-Out-of-Seq";
    protected static final String SEND_REPLY = "Send-Reply";
    protected static final String HANDLE_ACCEPT_PROPOSAL = "Handle-Accept-Proposal";
    protected static final String HANDLE_REJECT_PROPOSAL = "Handle-Reject-Proposal";
    protected static final String TERMINATE_BIDDING_ITERATION = "Terminate-Bidding-Iteration";
    protected static final String HANDLE_CLOSING_INFORM = "Handle-Closing-Inform";
    private static final String DUMMY_END = "end";

    // --- Constants (MsgTemplates) --------------------------------------------

    protected static final MessageTemplate MSG_TEMPLATE_PROTOCOL = MessageTemplate
	    .MatchProtocol(FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION);

    protected static final MessageTemplate INFORM_MSG_TEMPLATE_PERFORMATIVE = MessageTemplate
	    .MatchPerformative(ACLMessage.INFORM);

    protected static final MessageTemplate CFP_MSG_TEMPLATE_PERFORMATIVE = MessageTemplate
	    .MatchPerformative(ACLMessage.CFP);

    protected static final MessageTemplate INITIATION_INFORM_MSG_TEMPLATE = MessageTemplate
	    .and(MSG_TEMPLATE_PROTOCOL, INFORM_MSG_TEMPLATE_PERFORMATIVE);

    protected static final MessageTemplate CFP_MSG_TEMPLATE = MessageTemplate
	    .and(MSG_TEMPLATE_PROTOCOL, CFP_MSG_TEMPLATE_PERFORMATIVE);

    // --- Constants (protocol steps) ------------------------------------------

    /**
     * Expecting initiation inform msg to be received.
     */
    private static final int STEP_INIT_AUCTION = 1;

    /**
     * Expecting CFP msg to be received.
     */
    private static final int STEP_INIT_ITERATION = 2;

    /**
     * Expecting accept-proposal/reject-proposal/request/ msg to be received
     */
    private static final int STEP_END_ITERATION = 3;

    /**
     * Terminating the bidding iteration.
     */
    private static final int STEP_TERMINATE_ITERATION = 4;

    /**
     * Terminating the auction.
     */
    private static final int STEP_TERMINATE_AUCTION = 5;

    // --- Constants (states exit values) --------------------------------------

    private static final int OUT_OF_SEQUENCE_EXIT_CODE = -98765;
    private static final int CLOSING_INFORM = -1;
    public static final int REQUEST_EXPECTED = -2;
    protected static final int NOT_INTERESTED = -1;

    // --- Class Variables -----------------------------------------------------

    private static Logger LOG = Logger
	    .getLogger(EnglishAuctionParticipant.class.getName());

    // --- Class Variables (dataStore keys) ------------------------------------

    /**
     * Key to retrieve from the DataStore the stored initiation INFORM message.
     */
    protected final String INITIATION_INFORM_KEY = "__Initiation-inform-key"
	    + hashCode();

    /**
     * Key to retrieve from the DataStore the stored CFP ACLMessage.
     */
    protected final String CFP_KEY = "__Cfp-Key" + hashCode();

    /**
     * Key to retrieve from the DataStore of the behaviour the last received
     * ACLMessage
     */
    public final String RECEIVED_KEY = "__Received_key" + hashCode();

    /**
     * Key to set into the DataStore of the behaviour the new ACLMessage to be
     * sent back to the initiator as a reply.
     */
    public final String REPLY_KEY = "__Reply_key" + hashCode();

    /**
     * Key to retrieve from the DataStore of the behaviour the last sent PROPOSE
     * ACLMessage
     */
    public final String PROPOSE_KEY = REPLY_KEY;

    /**
     * Key to retrieve from the DataStore information whether the participant is
     * winner in the auction or not.
     */
    public final String WINNER_KEY = "__Winner_key" + hashCode();

    // --- Instance Variables --------------------------------------------------

    private int step = STEP_INIT_AUCTION;

    // --- Constructors --------------------------------------------------------

    public EnglishAuctionParticipant(Agent agent,
	    MessageTemplate informInitTemplate, DataStore store) {
	super(agent);
	setDataStore(store);

	step = STEP_INIT_AUCTION;

	// //////////////////////// REGISTER TRANSITIONS ///////////////////////

	registerDefaultTransition(RECEIVE_INITIATION_INFORM, RECEIVE_NEXT);
	registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);
	registerTransition(RECEIVE_NEXT, DUMMY_END, MsgReceiver.TIMEOUT_EXPIRED);
	registerTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE,
		OUT_OF_SEQUENCE_EXIT_CODE);
	registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP);
	registerTransition(CHECK_IN_SEQ, HANDLE_ACCEPT_PROPOSAL,
		ACLMessage.ACCEPT_PROPOSAL);
	registerTransition(CHECK_IN_SEQ, HANDLE_REJECT_PROPOSAL,
		ACLMessage.REJECT_PROPOSAL);
	registerTransition(CHECK_IN_SEQ, HANDLE_CLOSING_INFORM, CLOSING_INFORM);
	// registerTransition(CHECK_IN_SEQ, HANDLE_REQUEST, ACLMessage.REQUEST);
	registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, DUMMY_END);
	registerDefaultTransition(HANDLE_CFP, SEND_REPLY);
	registerDefaultTransition(SEND_REPLY, RECEIVE_NEXT);
	registerDefaultTransition(HANDLE_ACCEPT_PROPOSAL,
		TERMINATE_BIDDING_ITERATION);
	registerDefaultTransition(HANDLE_REJECT_PROPOSAL,
		TERMINATE_BIDDING_ITERATION);
	registerDefaultTransition(TERMINATE_BIDDING_ITERATION, RECEIVE_NEXT);
	registerDefaultTransition(HANDLE_CLOSING_INFORM, DUMMY_END);

	// //////////////////////// REGISTER STATES ////////////////////////////

	// RECEIVE_INITIATION_INFORM
	{
	    Behaviour receiveInitiationInform = new InitInformReceiver(myAgent,
		    informInitTemplate, -1, getDataStore(),
		    INITIATION_INFORM_KEY);
	    registerFirstState(receiveInitiationInform,
		    RECEIVE_INITIATION_INFORM);
	}

	// RECEIVE_NEXT
	{
	    Behaviour receiveNext = new NextMsgReceiver(myAgent,
		    getDataStore(), RECEIVED_KEY);
	    registerState(receiveNext, RECEIVE_NEXT);
	}

	// CHECK_IN_SEQ
	{
	    Behaviour seqChecker = new SeqChecker(myAgent);
	    seqChecker.setDataStore(getDataStore());
	    registerState(seqChecker, CHECK_IN_SEQ);
	}

	// HANDLE_OUT_OF_SEQUENCE
	{
	    Behaviour outOfSeqHandler = new OutOfSeqHandler(myAgent);
	    outOfSeqHandler.setDataStore(getDataStore());
	    registerState(outOfSeqHandler, HANDLE_OUT_OF_SEQUENCE);
	}

	// HANDLE_CFP
	{
	    Behaviour cfpHandler = new CfpHandler(myAgent);
	    cfpHandler.setDataStore(getDataStore());
	    registerState(cfpHandler, HANDLE_CFP);
	}

	// SEND_REPLY
	{
	    NextReplySender nextReplySender = new NextReplySender(myAgent,
		    REPLY_KEY, RECEIVED_KEY);
	    nextReplySender.setDataStore(getDataStore());
	    registerState(nextReplySender, SEND_REPLY);
	}

	// HANDLE_ACCEPT_PROPOSAL
	{
	    Behaviour acceptProposalHandler = new AcceptProposalHandler(myAgent);
	    acceptProposalHandler.setDataStore(getDataStore());
	    registerState(acceptProposalHandler, HANDLE_ACCEPT_PROPOSAL);
	}

	// HANDLE_REJECT_PROPOSAL
	{
	    Behaviour rejectProposalHandler = new RejectProposalHandler(myAgent);
	    rejectProposalHandler.setDataStore(getDataStore());
	    registerState(rejectProposalHandler, HANDLE_REJECT_PROPOSAL);
	}

	// TERMINATE_BIDDING_ITERATION
	{
	    Behaviour terminateBiddingIteration = new TerminateBiddingIteration(
		    myAgent);
	    terminateBiddingIteration.setDataStore(getDataStore());
	    registerState(terminateBiddingIteration,
		    TERMINATE_BIDDING_ITERATION);
	}

	// HANDLE_CLOSING_INFORM
	{
	    Behaviour closingInformHandler = new ClosingInformHandler(myAgent);
	    closingInformHandler.setDataStore(getDataStore());
	    registerState(closingInformHandler, HANDLE_CLOSING_INFORM);
	}

	// DUMMY_END
	{
	    Behaviour dummyEnd = new OneShotBehaviour(myAgent) {
		private static final long serialVersionUID = -7238636424650380074L;

		public void action() {
		    log("End english-auction behaviour.");
		}
	    };
	    registerLastState(dummyEnd, DUMMY_END);
	}
    }

    // --- Methods (inherited by Behaviour) ------------------------------------

    @Override
    public int onEnd() {
	DataStore ds = getDataStore();
	ds.remove(INITIATION_INFORM_KEY);
	ds.remove(RECEIVED_KEY);
	ds.remove(CFP_KEY);
	ds.remove(REPLY_KEY);
	Integer winnerKey = (Integer) ds.remove(WINNER_KEY);
	if (winnerKey != null && REQUEST_EXPECTED == winnerKey.intValue()) {
	    return REQUEST_EXPECTED;
	}
	return super.onEnd();
    }

    // --- Methods (handlers) --------------------------------------------------
    public void handleInitiationInform(ACLMessage initiationInform) {
	// do nothing
    }

    protected void handleOutOfSequence(ACLMessage msg) {
	ACLMessage cfp = (ACLMessage) getDataStore().get(RECEIVED_KEY);
	ACLMessage propose = (ACLMessage) getDataStore().get(PROPOSE_KEY);
	handleOutOfSequence(cfp, propose, msg);
    }

    protected void handleOutOfSequence(ACLMessage cfp, ACLMessage propose,
	    ACLMessage msg) {
	// do nothing
    }

    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,
	    FailureException, NotUnderstoodException {
	return null;
    }

    protected void handleAcceptProposal(ACLMessage cfp, ACLMessage propose,
	    ACLMessage accept) throws FailureException {
	// do nothing
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose,
	    ACLMessage reject) {
	// do nothing
    }

    protected void handleClosingInform(ACLMessage closingInform) {
	// do nothing
    }

    // --- Methods -------------------------------------------------------------

    public int getStep() {
	return step;
    }

    public void setStep(int step) {
	if (step < STEP_INIT_AUCTION || step > STEP_TERMINATE_AUCTION) {
	    return;
	}
	this.step = step;
    }

    private void setMessageToReplyKey(String key) {
	ReplySender rs = (ReplySender) getState(SEND_REPLY);
	rs.setMsgKey(key);
    }

    /**
     * This method can be redefined by protocol specific implementations to
     * customize a reply that is going to be sent back to the initiator. This
     * default implementation does nothing.
     */
    protected void beforeReply(ACLMessage reply) {
    }

    /**
     * This method can be redefined by protocol specific implementations to
     * update the status of the protocol just after a reply has been sent. This
     * default implementation does nothing.
     */
    protected void afterReply(ACLMessage reply) {
    }

    protected boolean checkInSequence(ACLMessage received) {
	switch (getStep()) {
	case 1:
	    return received.getPerformative() == ACLMessage.INFORM
		    && EnglishAuctionInitiator.ENGLISH_AUCTION_STARTS
			    .equals(received.getContent());
	case 2:
	    return received.getPerformative() == ACLMessage.CFP;
	case 3:
	    return received.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
		    || received.getPerformative() == ACLMessage.REJECT_PROPOSAL
		    || (received.getPerformative() == ACLMessage.INFORM && !EnglishAuctionInitiator.ENGLISH_AUCTION_STARTS
			    .equals(received.getContent()));
	case 4:
	    return received.getPerformative() == ACLMessage.CFP
		    || (received.getPerformative() == ACLMessage.INFORM
			    && received.getContent() != null && received
			    .getContent()
			    .startsWith(
				    EnglishAuctionInitiator.ENGLISH_AUCTION_ENDED));
	case 5:
	default:
	    return false;
	}
    }

    private void log(String msg) {
	Level logLevel = ProdConsConfiguration.instance().getLogLevel();
	if (LOG.isLoggable(logLevel)) {
	    LOG.log(logLevel, EA_PARTICIPANT + " [" + myAgent.getLocalName()
		    + "] " + msg + "\n");
	}
    }

    // --- Inner Classes -------------------------------------------------------

    private static class InitInformReceiver extends MsgReceiver {
	private static final long serialVersionUID = 6893454190521586350L;

	public InitInformReceiver(Agent agent, MessageTemplate mt,
		long deadline, DataStore ds, Object msgKey) {
	    super(agent, mt, deadline, ds, msgKey);
	}

	@Override
	public void action() {
	    super.action();
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    parent.setStep(EnglishAuctionParticipant.STEP_INIT_ITERATION);
	    ACLMessage initiationMsg = (ACLMessage) getDataStore().get(
		    parent.INITIATION_INFORM_KEY);
	    if (initiationMsg != null) {
		parent.log("EA session opened - initiation "
			+ "inform received: \n" + initiationMsg);
	    } else {
		parent.log("Waiting for initiation inform msg.");
	    }
	}
    }

    private static class NextMsgReceiver extends MsgReceiver {

	private static final long serialVersionUID = -4660893622127843520L;

	public NextMsgReceiver(Agent a, DataStore ds, String key) {
	    super(a, null, DEADLINE, ds, key);
	}

	public int onEnd() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    parent.setMessageToReplyKey((String) receivedMsgKey);
	    parent.log("Received msg: \n" + getDataStore().get(receivedMsgKey));
	    return super.onEnd();
	}
    }

    private static class SeqChecker extends OneShotBehaviour {
	private static final long serialVersionUID = -7063087812750856421L;
	private int ret;

	public SeqChecker(Agent a) {
	    super(a);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage received = (ACLMessage) getDataStore().get(
		    parent.RECEIVED_KEY);
	    if (received != null && parent.checkInSequence(received)) {
		parent.log("The received msg is in sequence.");
		if (ACLMessage.INFORM == received.getPerformative()
			&& received.getContent() != null
			&& received.getContent().startsWith(
				EnglishAuctionInitiator.ENGLISH_AUCTION_ENDED)) {
		    ret = CLOSING_INFORM;
		} else {
		    ret = received.getPerformative();
		}
	    } else {
		parent.log("The received msg is out of sequence.");
		ret = OUT_OF_SEQUENCE_EXIT_CODE;
	    }
	}

	public int onEnd() {
	    return ret;
	}
    }

    private static class OutOfSeqHandler extends OneShotBehaviour {
	private static final long serialVersionUID = 4430520852066571556L;

	public OutOfSeqHandler(Agent a) {
	    super(a);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    parent.handleOutOfSequence((ACLMessage) getDataStore().get(
		    parent.RECEIVED_KEY));
	}
    }

    private static class NextReplySender extends ReplySender {
	private static final long serialVersionUID = -8396463168737001522L;

	public NextReplySender(Agent a, String replyKey, String msgKey) {
	    super(a, replyKey, msgKey);
	}

	public void onStart() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage reply = (ACLMessage) getDataStore()
		    .get(parent.REPLY_KEY);
	    parent.beforeReply(reply);
	}

	public int onEnd() {
	    int ret = super.onEnd();
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();

	    ACLMessage reply = null;
	    if (reply != null) {
		MsgReceiver mr = (MsgReceiver) parent.getState(RECEIVE_NEXT);
		mr.setTemplate(createNextMsgTemplate(reply));

		Date d = reply.getReplyByDate();
		if (d != null && d.getTime() > System.currentTimeMillis()) {
		    mr.setDeadline(d.getTime());
		} else {
		    mr.setDeadline(MsgReceiver.INFINITE);
		}
	    }

	    parent.afterReply(reply);
	    return ret;
	}

	private MessageTemplate createNextMsgTemplate(ACLMessage reply) {
	    return MessageTemplate.and(MessageTemplate
		    .MatchConversationId(reply.getConversationId()),
		    MessageTemplate.not(MessageTemplate
			    .MatchCustom(reply, true)));
	}
    }

    private static class CfpHandler extends OneShotBehaviour {
	private static final long serialVersionUID = 4766407563773001L;
	private int ret = 0;

	public CfpHandler(Agent a) {
	    super(a);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage reply = null;
	    try {
		ACLMessage cfp = (ACLMessage) getDataStore().get(
			parent.RECEIVED_KEY);
		getDataStore().put(parent.CFP_KEY, cfp);
		reply = parent.handleCfp(cfp);
		if (reply == null) {
		    ret = NOT_INTERESTED;
		}
	    } catch (FIPAException fe) {
		reply = fe.getACLMessage();
	    }
	    if (ret == NOT_INTERESTED) {
		parent.log("Handling cfp msg: not interested.");
	    } else {
		parent.log("Handling cfp msg by creation of the reply: \n"
			+ reply);
	    }
	    getDataStore().put(parent.REPLY_KEY, reply);
	}

	@Override
	public int onEnd() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    parent.setStep(EnglishAuctionParticipant.STEP_END_ITERATION);
	    return ret;
	}
    }

    private static class AcceptProposalHandler extends OneShotBehaviour {
	private static final long serialVersionUID = 1102952643478182881L;

	public AcceptProposalHandler(Agent a) {
	    super(a);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage cfpMsg = (ACLMessage) getDataStore().get(parent.CFP_KEY);
	    ACLMessage proposeMsg = (ACLMessage) getDataStore().get(
		    parent.REPLY_KEY);
	    ACLMessage acceptProposalMsg = (ACLMessage) getDataStore().get(
		    parent.RECEIVED_KEY);
	    try {
		parent.handleAcceptProposal(cfpMsg, proposeMsg,
			acceptProposalMsg);
	    } catch (FIPAException fe) {
		// reply = fe.getACLMessage();
	    }
	    parent.setStep(EnglishAuctionParticipant.STEP_TERMINATE_ITERATION);
	    parent.log("Accept propsal msg handled successfully.");
	}
    }

    private static class RejectProposalHandler extends OneShotBehaviour {
	private static final long serialVersionUID = -5786893965909888597L;

	public RejectProposalHandler(Agent a) {
	    super(a);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage cfpMsg = (ACLMessage) getDataStore().get(parent.CFP_KEY);
	    ACLMessage proposeMsg = (ACLMessage) getDataStore().get(
		    parent.REPLY_KEY);
	    ACLMessage rejectProposalMsg = (ACLMessage) getDataStore().get(
		    parent.RECEIVED_KEY);
	    parent.handleRejectProposal(cfpMsg, proposeMsg, rejectProposalMsg);
	    parent.setStep(EnglishAuctionParticipant.STEP_TERMINATE_ITERATION);
	    parent.log("Reject propsal msg handled successfully.");
	}
    }

    private static class TerminateBiddingIteration extends OneShotBehaviour {
	private static final long serialVersionUID = -8612665235190871756L;

	public TerminateBiddingIteration(Agent agent) {
	    super(agent);
	}

	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    resetDataStore(parent);
	    parent.log("The bidding iteration terminated successfully.");
	}

	private void resetDataStore(EnglishAuctionParticipant parent) {
	    DataStore ds = getDataStore();
	    ds.remove(parent.CFP_KEY);
	    ds.remove(parent.RECEIVED_KEY);
	    ds.remove(parent.REPLY_KEY);
	}
    }

    private static class ClosingInformHandler extends OneShotBehaviour {
	private static final long serialVersionUID = 361155018163196078L;
	private int ret = -1;

	public ClosingInformHandler(Agent agent) {
	    super(agent);
	}

	@Override
	public void action() {
	    EnglishAuctionParticipant parent = (EnglishAuctionParticipant) getParent();
	    ACLMessage closingInform = (ACLMessage) getDataStore().get(
		    parent.RECEIVED_KEY);
	    if (isMyAgentWinner(closingInform)) {
		ret = REQUEST_EXPECTED;
		getDataStore().put(parent.WINNER_KEY,
			new Integer(REQUEST_EXPECTED));
		parent.log("I am the winner!!!");
	    } else {
		parent
			.setStep(EnglishAuctionParticipant.STEP_TERMINATE_AUCTION);
	    }
	    parent.handleClosingInform(closingInform);
	    parent.log("Closing auction inform msg handled successfully.");
	}

	@Override
	public int onEnd() {
	    return ret;
	}

	private boolean isMyAgentWinner(ACLMessage closingInform) {
	    String closingInformContent = closingInform.getContent();
	    String prefix = EnglishAuctionInitiator.ENGLISH_AUCTION_ENDED
		    + EnglishAuctionInitiator.WINNER_AID;
	    String winnerAID = closingInformContent.substring(prefix.length());
	    return myAgent.getAID().toString().equals(winnerAID);
	}
    }
}