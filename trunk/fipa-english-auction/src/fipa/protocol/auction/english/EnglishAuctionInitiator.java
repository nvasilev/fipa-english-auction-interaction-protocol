package fipa.protocol.auction.english;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.util.leap.Collection;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fipa.ProdConsConfiguration;
import fipa.impl.protocol.auction.english.EnglishAuctionInitiatorFactory;

/**
 * Class implementing the initiator part of the
 * <code>FIPA English Auction Interaction Protocol</code>.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public abstract class EnglishAuctionInitiator extends FSMBehaviour {

	// --- Constants -----------------------------------------------------------

	// bloody eclipse ;)
	private static final long serialVersionUID = -1878664192240233668L;

	private static final long TIMEOUT = 1000;

	private static final String EA_INITIATOR = "[EA-Initiator] ";

	public static final String ENGLISH_AUCTION_STARTS = "English auction starts.";
	public static final String ENGLISH_AUCTION_ENDED = "English auction ended.";
	public static final String WINNER_AID = " Winner AID: ";
	public static final String PAYMENT_EXPECTED = "Payment expected: ";

	private static final String BIDDING_ITERATION_WINNER_STR = "Bidding iteration winner: [";
	private static final String WITH_BIDDING_PRICE_STR = "], with bidding price: ";

	// --- Constants (States) --------------------------------------------------

	protected static final String PREPARE_INITIATIONS = "Prepare-Inform";
	protected static final String SEND_INITIATIONS = "Send-Inform-Initiations";
	protected static final String PREPARE_CFP = "Prepare-Cfp";
	protected static final String SEND_CFP = "Send-Cfp";
	protected static final String RECEIVE_REPLY = "Receive-Reply";
	protected static final String CHECK_IN_SEQ = "Check-in-seq";
	protected static final String HANDLE_NOT_UNDERSTOOD = "Handle-Not-Understood";
	protected static final String HANDLE_FAILURE = "Handle-Failure";
	protected static final String HANDLE_OUT_OF_SEQ = "Handle-Out-of-Seq";
	protected static final String HANDLE_PROPOSE = "Handle-Propose";
	protected static final String CHECK_SESSIONS = "Check-Sessions";
	protected static final String PREPARE_PROPOSALS = "Prepare-Proposals";
	protected static final String SEND_PROPOSALS = "Send-Proposals";
	protected static final String TERMINATE_BIDDING_ITERATION = "Terminate-Bidding-Iteration";
	protected static final String PREPARE_AUCTION_CLOSE_INFORM = "Prepare-Close-Informs";
	protected static final String SEND_AUCTION_CLOSE_INFORM = "Send-Close-Informs";
	protected static final String PREPARE_WINNER_REQUEST = "Prepare-Winner-Request";
	protected static final String DUMMY_END = "End";

	// --- Constants (MsgTemplates) --------------------------------------------

	protected static final MessageTemplate MSG_TEMPLATE_PROTOCOL = MessageTemplate
			.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION);

	protected static final MessageTemplate PROPOSE_MSG_TEMPLATE_PERFORMATIVE = MessageTemplate
			.MatchPerformative(ACLMessage.PROPOSE);

	protected static final MessageTemplate PROPOSE_MSG_TEMPLATE = MessageTemplate
			.and(MSG_TEMPLATE_PROTOCOL, PROPOSE_MSG_TEMPLATE_PERFORMATIVE);

	// --- Constants (states exit values) --------------------------------------

	protected static final int NO_PROPOSALS_PREPARED = -1;
	protected static final int PROPOSE_RECEIVED = 1;
	protected static final int ALL_PROPOSES_RECEIVED = 2;
	public static final int WINNER_REQUEST_PREPARED = 3;
	protected static final int END_OF_BIDDING = 0;

	// --- Class Variables -----------------------------------------------------

	private static Logger LOG = Logger.getLogger(EnglishAuctionInitiator.class
			.getName());

	// --- Class Variables (dataStore keys) ------------------------------------

	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * object passed to the constructor of the class.
	 */
	protected final String INITIATION_KEY = "__initiation" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * vector of INFORM <code>ACLMessage</code> objects that have to be sent.
	 **/
	public final String ALL_INFORM_MSGS = "__all-informs" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * vector of CFP <code>ACLMessage</code> objects that have to be sent.
	 */
	public final String ALL_CFP_MSGS = "__all-cfps" + hashCode();

	/**
	 * key to retrieve from the <code>DataStore</code> of the behaviour the
	 * vector of ACCEPT/REJECT_PROPOSAL <code>ACLMessage</code> objects that
	 * have to be sent
	 **/
	public final String ALL_RECEIVED_PROPOSE_MSGS = "__all-proposes"
			+ hashCode();
	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * vector of ACCEPT/REJECT PROPOSAL <code>ACLMessage</code> objects that
	 * have to be sent.
	 */
	public final String ALL_PROPOSAL_MSGS = "__all-proposals" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * vector of all INFORM <code>ACLMessage</code> objects which have to be
	 * sent.
	 */
	public final String ALL_CLOSING_INFOM_MSGS = "__all-closing-informs"
			+ hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> of the behaviour the
	 * first <code>ACLMessage</code> object that has been received (
	 * <code>null</code> if the timeout expired).
	 */
	protected final String REPLY_KEY = "__reply" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> the <code>AID</code> of
	 * the winner in the last bidding iteration.
	 */
	protected final String LAST_WINNER_AID_KEY = "__last-winner" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> the price in the last
	 * successful bidding iteration.
	 */
	protected final String LAST_WON_PRICE_KEY = "__last-won-price" + hashCode();

	/**
	 * Key to retrieve from the <code>DataStore</code> the request
	 * <code>ACLMessage</code> needed by the subsequent behaviour in the parent
	 * protocol - <code>Request Interaction Protocol</code>.
	 */
	public static final String WINNER_REQUEST = "__winner-request";

	// --- Instance Variables --------------------------------------------------

	private ACLMessage initiation;

	// The MessageTemplate used by the proposalsReceiver
	protected MessageTemplate proposalsTemplate = null;

	// This maps the AID of each responder to a Session object holding the
	// status of the protocol as far as that responder is concerned. Sessions
	// are protocol-specific
	protected Map sessions = new HashMap();

	// The MsgReceiver behaviour used to receive replies
	protected MsgReceiver replyReceiver = null;

	// // The MsgReceiver behaviour used to receive replies
	// protected MsgReceiver proposalsReceiver = null;

	// The MsgReceiver behaviour used to receive replies
	protected MsgReceiver cfpReplyReceiver = null;

	// If set to true all responses not yet received are skipped
	private boolean skipNextRespFlag = false;

	private String[] statesToBeReset = null;

	// --- Constructors --------------------------------------------------------

	public EnglishAuctionInitiator(Agent a, ACLMessage inform) {
		this(a, inform, new DataStore());
	}

	public EnglishAuctionInitiator(Agent agent, ACLMessage infrom,
			DataStore store) {
		super(agent);
		this.initiation = infrom;
		setDataStore(store);

		// //////////////////////// REGISTER TRANSITIONS ///////////////////////
		{
			registerDefaultTransition(PREPARE_INITIATIONS, SEND_INITIATIONS);
			registerTransition(SEND_INITIATIONS, DUMMY_END, END_OF_BIDDING);
			registerDefaultTransition(SEND_INITIATIONS, PREPARE_CFP);
			registerDefaultTransition(PREPARE_CFP, SEND_CFP);
			registerTransition(SEND_CFP, DUMMY_END, END_OF_BIDDING);
			registerDefaultTransition(SEND_CFP, RECEIVE_REPLY);

			registerTransition(RECEIVE_REPLY, PREPARE_AUCTION_CLOSE_INFORM,
					MsgReceiver.TIMEOUT_EXPIRED);

			registerTransition(RECEIVE_REPLY, CHECK_SESSIONS,
					MsgReceiver.INTERRUPTED);
			registerDefaultTransition(RECEIVE_REPLY, CHECK_IN_SEQ);

			registerTransition(CHECK_IN_SEQ, HANDLE_NOT_UNDERSTOOD,
					ACLMessage.NOT_UNDERSTOOD);
			registerTransition(CHECK_IN_SEQ, HANDLE_FAILURE, ACLMessage.FAILURE);
			registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
			registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQ);

			registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, RECEIVE_REPLY);
			registerDefaultTransition(HANDLE_FAILURE, RECEIVE_REPLY);
			registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
			registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);

			registerDefaultTransition(CHECK_SESSIONS, RECEIVE_REPLY,
					getStatesToBeReset());
			registerTransition(CHECK_SESSIONS, PREPARE_PROPOSALS,
					PROPOSE_RECEIVED);

			registerDefaultTransition(PREPARE_PROPOSALS, SEND_PROPOSALS);
			registerDefaultTransition(SEND_PROPOSALS,
					TERMINATE_BIDDING_ITERATION);
			registerDefaultTransition(TERMINATE_BIDDING_ITERATION, PREPARE_CFP);

			registerDefaultTransition(PREPARE_AUCTION_CLOSE_INFORM,
					SEND_AUCTION_CLOSE_INFORM);
			registerTransition(SEND_AUCTION_CLOSE_INFORM, DUMMY_END,
					END_OF_BIDDING);
			registerDefaultTransition(SEND_AUCTION_CLOSE_INFORM,
					PREPARE_WINNER_REQUEST);

			registerDefaultTransition(PREPARE_WINNER_REQUEST, DUMMY_END);
		}

		// //////////////////////// REGISTER STATES ////////////////////////////

		// PREPARE_INITIATIONS
		{
			Behaviour prepareInform = new PrepareInitiations(myAgent);
			prepareInform.setDataStore(getDataStore());
			registerFirstState(prepareInform, PREPARE_INITIATIONS);
		}

		// SEND_INITIATIONS
		{
			Behaviour sendInitiations = new SendInitiations(myAgent);
			sendInitiations.setDataStore(getDataStore());
			registerState(sendInitiations, SEND_INITIATIONS);
		}

		// PREPARE_CFP
		{
			Behaviour prepareCfp = new PrepareCfps(myAgent);
			prepareCfp.setDataStore(getDataStore());
			registerState(prepareCfp, PREPARE_CFP);
		}

		// SEND_CFP
		{
			Behaviour sendCfp = new SendCfps(myAgent);
			sendCfp.setDataStore(getDataStore());
			registerState(sendCfp, SEND_CFP);
		}

		// RECEIVE_REPLY
		{
			replyReceiver = new ReplyReceiver(myAgent, null, getDataStore(),
					REPLY_KEY);
			registerState(replyReceiver, RECEIVE_REPLY);
		}

		// CHECK_IN_SEQ
		{
			Behaviour seqChecker = new SeqChecker(myAgent);
			seqChecker.setDataStore(getDataStore());
			registerState(seqChecker, CHECK_IN_SEQ);
		}

		// HANDLE_NOT_UNDERSTOOD
		{
			Behaviour notUnderstoodHandler = new NotUnderstoodHandler(myAgent);
			notUnderstoodHandler.setDataStore(getDataStore());
			registerState(notUnderstoodHandler, HANDLE_NOT_UNDERSTOOD);
		}

		// HANDLE_FAILURE
		{
			Behaviour failureHandler = new FailureHandler(myAgent);
			failureHandler.setDataStore(getDataStore());
			registerState(failureHandler, HANDLE_FAILURE);
		}

		// HANDLE_OUT_OF_SEQ
		{
			Behaviour outOfSeqHandler = new OutOfSequenceHandler(myAgent);
			outOfSeqHandler.setDataStore(getDataStore());
			registerState(outOfSeqHandler, HANDLE_OUT_OF_SEQ);
		}

		// HANDLE_PROPOSE
		{
			Behaviour proposeHandler = new ProposeHandler(myAgent);
			proposeHandler.setDataStore(getDataStore());
			registerState(proposeHandler, HANDLE_PROPOSE);
		}

		// CHECK_SESSIONS
		{
			Behaviour sessionsVerifier = new SessionsVerifier(myAgent);
			sessionsVerifier.setDataStore(getDataStore());
			registerState(sessionsVerifier, CHECK_SESSIONS);
		}

		// PREPARE_PROPOSALS
		{
			Behaviour prepareProposals = new PrepareProposals(myAgent);
			prepareProposals.setDataStore(getDataStore());
			registerState(prepareProposals, PREPARE_PROPOSALS);
		}

		// SEND_PROPOSALS
		{
			Behaviour sendCfp = new SendProposals(myAgent);
			sendCfp.setDataStore(getDataStore());
			registerState(sendCfp, SEND_PROPOSALS);
		}

		// TERMINATE_BIDDING_ITERATION
		{
			Behaviour terminateBiddingIteration = new TerminateBiddingIteration(
					myAgent);
			terminateBiddingIteration.setDataStore(getDataStore());
			registerState(terminateBiddingIteration,
					TERMINATE_BIDDING_ITERATION);
		}

		// PREPARE_AUCTION_CLOSING_MSGS
		{
			Behaviour prepareCloseInform = new PrepareClosingInforms(myAgent);
			prepareCloseInform.setDataStore(getDataStore());
			registerState(prepareCloseInform, PREPARE_AUCTION_CLOSE_INFORM);
		}

		// SEND_AUCTION_CLOSING_MSGS
		{
			Behaviour sendCloseInform = new SendClosingInforms(myAgent);
			sendCloseInform.setDataStore(getDataStore());
			registerState(sendCloseInform, SEND_AUCTION_CLOSE_INFORM);
		}

		// PREPARE_WINNER_REQUEST
		{
			Behaviour sendWinnerRequest = new PrepareWinnerRequest(myAgent);
			sendWinnerRequest.setDataStore(getDataStore());
			registerState(sendWinnerRequest, PREPARE_WINNER_REQUEST);
		}

		// DUMMY_END
		{
			Behaviour dummyEnd = new OneShotBehaviour(myAgent) {
				private static final long serialVersionUID = -4216350173804405598L;

				public void action() {
					log("End english-auction behaviour.");
				}
			};
			registerLastState(dummyEnd, DUMMY_END);
		}
	}

	// --- Methods (abstract) --------------------------------------------------

	/**
	 * Prepares initiation message for each of the auction participants, using
	 * the information from the <code>initiation</code> instance variable.
	 */
	protected abstract List<ACLMessage> prepareInitiations(
			ACLMessage informInitiationMsg);

	/**
	 * Obtains the <code>CFP</code> message content by the agent.
	 * 
	 * @return The <code>CFP</code> message content.
	 */
	protected abstract String getCfpMsgContent();

	// --- Methods (inherited by Behaviour) ------------------------------------

	@Override
	public void onStart() {
		DataStore ds = getDataStore();
		if (initiation == null) {
			initiation = EnglishAuctionInitiatorFactory.instance()
					.prepareInformInitiationMessage(myAgent);
		}
		ds.put(INITIATION_KEY, initiation);
		ds.put(ALL_RECEIVED_PROPOSE_MSGS, new ArrayList<ACLMessage>());
	}

	@Override
	public int onEnd() {
		initiation = null;
		sessions.clear();
		DataStore ds = getDataStore();
		ds.remove(ALL_INFORM_MSGS);
		ds.remove(ALL_CFP_MSGS);
		ds.remove(REPLY_KEY);
		ds.remove(ALL_RECEIVED_PROPOSE_MSGS);
		ds.remove(ALL_PROPOSAL_MSGS);
		ds.remove(ALL_CLOSING_INFOM_MSGS);
		ds.remove(LAST_WINNER_AID_KEY);
		return (ds.get(WINNER_REQUEST) != null) ? WINNER_REQUEST_PREPARED : -1;
	}

	// --- Methods (handlers) --------------------------------------------------

	/**
	 * This method is called every time a <code>not-understood</code> message is
	 * received, which is not out-of-sequence according to the protocol rules.
	 * This default implementation does nothing; programmers might wish to
	 * override the method in case they need to react to this event.
	 * 
	 * @param notUnderstood
	 *            the received not-understood message
	 **/
	protected void handleNotUnderstood(ACLMessage notUnderstood) {
		// do nothing
	}

	/**
	 * This method is called every time a <code>failure</code> message is
	 * received, which is not out-of-sequence according to the protocol rules.
	 * This default implementation does nothing; programmers might wish to
	 * override the method in case they need to react to this event.
	 * 
	 * @param failure
	 *            the received failure message
	 **/
	protected void handleFailure(ACLMessage failure) {
		// do nothing
	}

	/**
	 * This method is called every time a message is received, which is
	 * out-of-sequence according to the protocol rules. This default
	 * implementation does nothing; programmers might wish to override the
	 * method in case they need to react to this event.
	 * 
	 * @param msg
	 *            the received message
	 **/
	protected void handleOutOfSequence(ACLMessage msg) {
		// do nothing
	}

	/**
	 * This method is called every time a <code>propose</code> message is
	 * received, which is not out-of-sequence according to the protocol rules.
	 * This default implementation does nothing; programmers might wish to
	 * override the method in case they need to react to this event.
	 * 
	 * @param propose
	 *            the received propose message
	 * @param acceptances
	 *            the list of ACCEPT/REJECT_PROPOSAL to be sent back. This list
	 *            can be filled step by step redefining this method, or it can
	 *            be filled at once redefining the handleAllResponses method.
	 **/
	protected void handlePropose(ACLMessage propose) {
		// do nothing
	}

	// --- Methods (registers) -------------------------------------------------

	public void registerPrepareInitiations(Behaviour b) {
		registerFirstState(b, PREPARE_INITIATIONS);
		b.setDataStore(getDataStore());
	}

	public void registerNotUnderstoodHandler(Behaviour b) {
		registerState(b, HANDLE_NOT_UNDERSTOOD);
		b.setDataStore(getDataStore());
	}

	public void registerFailureHandler(Behaviour b) {
		registerState(b, HANDLE_FAILURE);
		b.setDataStore(getDataStore());
	}

	public void registerOutOfSequenceHandler(Behaviour b) {
		registerState(b, HANDLE_OUT_OF_SEQ);
		b.setDataStore(getDataStore());
	}

	public void registerProposeHandler(Behaviour b) {
		registerState(b, HANDLE_PROPOSE);
		b.setDataStore(getDataStore());
	}

	// --- Methods -------------------------------------------------------------

	/**
	 * Checks whether a reply is in-sequence and update the appropriate Session
	 */
	protected boolean checkInSequence(ACLMessage reply) {
		boolean ret = false;
		String inReplyTo = reply.getInReplyTo();
		Session s = (Session) sessions.get(inReplyTo);
		if (s != null) {
			int perf = reply.getPerformative();
			if (s.update(perf)) {
				List<ACLMessage> all = (List<ACLMessage>) getDataStore().get(
						ALL_RECEIVED_PROPOSE_MSGS);
				all.add(reply);
				ret = true;
			}
			if (s.isCompleted()) {
				sessions.remove(inReplyTo);
			}
		}
		return ret;
	}

	/**
	 * Sends the initiation INFORM messages to the participants.
	 * 
	 * @param initiations
	 *            List of initiation templates.
	 */
	protected void sendInitiations(List<ACLMessage> initiations) {
		String conversationID = createConvId(initiations);
		int sessionsCntr = 0;
		List<ACLMessage> sentMessages = new ArrayList<ACLMessage>();
		for (ACLMessage initiation : initiations) {
			if (initiation != null) {
				Iterator receivers = initiation.getAllReceiver();
				while (receivers.hasNext()) {
					ACLMessage msgToSend = (ACLMessage) initiation.clone();
					msgToSend.setConversationId(conversationID);
					msgToSend.clearAllReceiver();
					AID r = (AID) receivers.next();
					msgToSend.addReceiver(r);
					ProtocolSession ps = getSession(msgToSend);
					if (ps != null) {
						String sessionKey = ps.getId();
						if (sessionKey == null) {
							sessionKey = "R" + System.currentTimeMillis() + "_"
									+ Integer.toString(sessionsCntr);
						}
						msgToSend.setReplyWith(sessionKey);
						sessions.put(sessionKey, ps);
						sessionsCntr++;
					}
					myAgent.send(msgToSend);
					log("Sent initiation (inform) msg: \n" + msgToSend);
					sentMessages.add(msgToSend);
				}
			}
		}
		getDataStore().put(ALL_INFORM_MSGS, sentMessages);
	}

	/**
	 * Prepares the CFP messages for start of the bidding iteration.
	 */
	protected void prepareCfp() {
		DataStore ds = getDataStore();
		List<ACLMessage> allInitiations = (List<ACLMessage>) ds
				.get(ALL_INFORM_MSGS);
		List<ACLMessage> allCfps = new ArrayList<ACLMessage>();
		if (allInitiations == null || allInitiations.isEmpty()) {
			String cfpMsgContent = getCfpMsgContent();
			for (ACLMessage initiation : allInitiations) {
				ACLMessage cfpMsg = (ACLMessage) initiation.clone();
				cfpMsg.setPerformative(ACLMessage.CFP);
				cfpMsg.setContent(cfpMsgContent);
				allCfps.add(cfpMsg);
			}
		}
		getDataStore().put(ALL_CFP_MSGS, allCfps);
	}

	/**
	 * Sends the CFP messages to all participants in the auction.
	 * 
	 * @param allCfps
	 *            List of all CFP messages.
	 */
	protected void sendCfps(List<ACLMessage> allCfps) {
		long currentTime = System.currentTimeMillis();
		long minTimeout = -1;
		long deadline = -1;

		String conversationID = ((ACLMessage) ((List<ACLMessage>) getDataStore()
				.get(ALL_INFORM_MSGS)).get(0)).getConversationId();
		proposalsTemplate = MessageTemplate.and(PROPOSE_MSG_TEMPLATE,
				MessageTemplate.MatchConversationId(conversationID));
		int cnt = 0; // counter of sessions
		List<ACLMessage> sentMsgs = new ArrayList<ACLMessage>();
		for (ACLMessage cfp : allCfps) {
			if (cfp != null) {
				{
					ProtocolSession ps = getSession(cfp);
					if (ps != null) {
						String sessionKey = ps.getId();
						if (sessionKey == null) {
							sessionKey = "R" + System.currentTimeMillis() + "_"
									+ Integer.toString(cnt);
						}
						cfp.setReplyWith(sessionKey);
						Date date = new Date(System.currentTimeMillis()
								+ TIMEOUT);
						cfp.setReplyByDate(date);
						sessions.put(sessionKey, ps);
						adjustReplyTemplate(cfp);
						cnt++;
					}
					myAgent.send(cfp);
					log("Sent cfp msg: \n" + cfp);
					sentMsgs.add(cfp);
				}
				// Update the timeout (if any) used to wait for replies
				// according to the reply-by field: get the miminum.
				Date d = cfp.getReplyByDate();
				if (d != null) {
					long timeout = d.getTime() - currentTime;
					if (timeout > 0
							&& (timeout < minTimeout || minTimeout <= 0)) {
						minTimeout = timeout;
						deadline = d.getTime();
					}
				}
			}
		}
		getDataStore().put(ALL_CFP_MSGS, sentMsgs);

		// Finally set the MessageTemplate and timeout used in the RECEIVE_REPLY
		// state to accept replies
		replyReceiver.setTemplate(proposalsTemplate);
		replyReceiver.setDeadline(deadline);
	}

	protected void adjustReplyTemplate(ACLMessage msg) {
		// If myAgent is among the receivers (strange case, but can happen)
		// then modify the replyTemplate to avoid intercepting the initiation
		// message msg as if it was a reply
		AID r = (AID) msg.getAllReceiver().next();
		if (myAgent.getAID().equals(r)) {
			proposalsTemplate = MessageTemplate
					.and(proposalsTemplate, MessageTemplate.not(MessageTemplate
							.MatchCustom(msg, true)));
		}
	}

	/**
	 * Create a new conversation identifier to begin a new interaction.
	 * 
	 * @param msgs
	 *            A vector of ACL messages. If the first one has a non-empty
	 *            <code>:conversation-id</code> slot, its value is used, else a
	 *            new conversation identifier is generated.
	 */
	protected String createConvId(List<ACLMessage> msgs) {
		// If the conversation-id of the first message is set --> use it.
		// Otherwise create a default one
		String convId = null;
		if (msgs.size() > 0) {
			ACLMessage msg = msgs.get(0);
			if ((msg == null) || (msg.getConversationId() == null)) {
				convId = "C" + hashCode() + "_" + System.currentTimeMillis();
			} else {
				convId = msg.getConversationId();
			}
		}
		return convId;
	}

	/**
	 * Obtains new <code>ProtocolSession</code> object according to the
	 * performative of the passed <code>msg</code> object.
	 * 
	 * @param msg
	 *            The <code>ACLMessage</code> which is going to be used for
	 *            defining the type of the new session.
	 * @return Returns new <code>ProtocolSession</code> object according to the
	 *         performative of the message. If the performative of the message
	 *         is not supported <code>null</code> is returned.
	 */
	protected ProtocolSession getSession(ACLMessage msg) {
		switch (msg.getPerformative()) {
		case ACLMessage.INFORM:
			if (ENGLISH_AUCTION_STARTS.equals(msg.getContent())) {
				return new Session(Session.STATE_INFORM);
			} else {
				return new Session(Session.STATE_CLOSING);
			}
		case ACLMessage.CFP:
			return new Session(Session.STATE_BIDDING_ITERATION_STARTED);
		case ACLMessage.ACCEPT_PROPOSAL:
		case ACLMessage.REJECT_PROPOSAL:
			return new Session(Session.STATE_BIDDING_ITERATION_CLOSED);
		default:
			return null;
		}
	}

	/**
	 * Prepares the <code>PROPOSAL</code> messages for the result of the bidding
	 * iteration.
	 */
	protected void prepareProposals() {
		DataStore ds = getDataStore();
		List<ACLMessage> allCfps = (List<ACLMessage>) ds.get(ALL_CFP_MSGS);
		List<ACLMessage> allProposes = (List<ACLMessage>) ds
				.get(ALL_RECEIVED_PROPOSE_MSGS);
		List<ACLMessage> allProposals = new ArrayList<ACLMessage>();
		ACLMessage firstPropose = allProposes.get(0);
		String proposalMsgContent = BIDDING_ITERATION_WINNER_STR
				+ firstPropose.getSender().getLocalName()
				+ WITH_BIDDING_PRICE_STR + firstPropose.getContent();
		for (ACLMessage cfp : allCfps) {
			AID receiverAid = null;
			Iterator receiversIterator = cfp.getAllReceiver();
			if (receiversIterator.hasNext()) {
				receiverAid = (AID) receiversIterator.next();
			}
			if (receiverAid == null) {
				throw new IllegalStateException(
						"Invalid proposal msg - receiver: null.");
			}
			ACLMessage proposalMsg = (ACLMessage) cfp.clone();
			if (firstPropose.getSender().equals(receiverAid)) {
				proposalMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ds.put(LAST_WINNER_AID_KEY, receiverAid);
				ds.put(LAST_WON_PRICE_KEY, Double.valueOf(cfp.getContent()));
				log("Last winner: " + receiverAid.getLocalName());
			} else {
				proposalMsg.setPerformative(ACLMessage.REJECT_PROPOSAL);
			}
			proposalMsg.setContent(proposalMsgContent);
			allProposals.add(proposalMsg);
		}
		ds.put(ALL_PROPOSAL_MSGS, allProposals);
	}

	/**
	 * Sends the <code>PROPOSAL</code> messages to all participants in the
	 * auction.
	 * 
	 * @param allProposals
	 *            List of all <code>PROPOSAL</code> messages.
	 */
	protected void sendProposals(List<ACLMessage> allProposals) {
		int sessionsCntr = 0;
		List<ACLMessage> sentMessages = new ArrayList<ACLMessage>();
		for (ACLMessage proposal : allProposals) {
			if (proposal != null) {
				Iterator receivers = proposal.getAllReceiver();
				while (receivers.hasNext()) {
					ACLMessage msgToSend = (ACLMessage) proposal.clone();
					msgToSend.clearAllReceiver();
					AID r = (AID) receivers.next();
					msgToSend.addReceiver(r);
					ProtocolSession ps = getSession(msgToSend);
					if (ps != null) {
						String sessionKey = ps.getId();
						if (sessionKey == null) {
							sessionKey = "R" + System.currentTimeMillis() + "_"
									+ Integer.toString(sessionsCntr);
						}
						msgToSend.setReplyWith(sessionKey);
						sessions.put(sessionKey, ps);
						sessionsCntr++;
					}
					myAgent.send(msgToSend);
					log("Sent proposal msg: \n" + msgToSend);
					sentMessages.add(msgToSend);
				}
			}
		}
		getDataStore().put(ALL_PROPOSAL_MSGS, sentMessages);
	}

	/**
	 * Prepares the <code>INFORM</code> messages for start of the result of the
	 * auction.
	 */
	protected void prepareCloseAuctionMsgs() {
		DataStore ds = getDataStore();
		AID winnerAID = (AID) ds.get(LAST_WINNER_AID_KEY);
		if (winnerAID == null) {
			return;
		}
		System.out.println("The auction winner is NOT null.");
		List<ACLMessage> allInitiations = (List<ACLMessage>) ds
				.get(ALL_INFORM_MSGS);
		System.out.println("All initiations: " + allInitiations);
		List<ACLMessage> allCloseInforms = new ArrayList<ACLMessage>();
		String closeInformMsgContent = ENGLISH_AUCTION_ENDED + WINNER_AID
				+ winnerAID;
		for (ACLMessage initiation : allInitiations) {
			ACLMessage closeInformMsg = (ACLMessage) initiation.clone();
			closeInformMsg.setPerformative(ACLMessage.INFORM);
			closeInformMsg.setContent(closeInformMsgContent);
			allCloseInforms.add(closeInformMsg);
		}
		ds.put(ALL_CLOSING_INFOM_MSGS, allCloseInforms);
	}

	/**
	 * Sends the <code>INFORM</code> messages to all participants in the
	 * auction.
	 * 
	 * @param allProposals
	 *            List of all <code>PROPOSAL</code> messages.
	 */
	protected void sendCloseAuctionMsgs(List<ACLMessage> allCloseInforms) {
		int sessionsCntr = 0;
		List<ACLMessage> sentMessages = new ArrayList<ACLMessage>();
		for (ACLMessage closeInformMsg : allCloseInforms) {
			if (closeInformMsg != null) {
				Iterator receivers = closeInformMsg.getAllReceiver();
				while (receivers.hasNext()) {
					ACLMessage msgToSend = (ACLMessage) closeInformMsg.clone();
					msgToSend.clearAllReceiver();
					AID r = (AID) receivers.next();
					msgToSend.addReceiver(r);
					ProtocolSession ps = getSession(msgToSend);
					if (ps != null) {
						String sessionKey = ps.getId();
						if (sessionKey == null) {
							sessionKey = "R" + System.currentTimeMillis() + "_"
									+ Integer.toString(sessionsCntr);
						}
						msgToSend.setReplyWith(sessionKey);
						sessions.put(sessionKey, ps);
						sessionsCntr++;
					}
					myAgent.send(msgToSend);
					log("Sent close auction inform msg: \n" + msgToSend);
					sentMessages.add(msgToSend);
				}
			}
		}
		getDataStore().put(ALL_CLOSING_INFOM_MSGS, sentMessages);
	}

	/**
	 * Checks the status of the sessions after the reception of the last reply
	 * or the expiration of the timeout.
	 * 
	 * @param reply
	 *            The reply which is analyzed.
	 * @return Returns <code>PROPOSE_RECEIVED</code> when the first reply is
	 *         received or <code>-1</code> otherwise.
	 */
	protected int checkSessions(ACLMessage reply) {
		if (skipNextRespFlag) {
			sessions.clear();
		}
		List<ACLMessage> allProposes = (List<ACLMessage>) getDataStore().get(
				ALL_RECEIVED_PROPOSE_MSGS);
		List<ACLMessage> allCfps = (List<ACLMessage>) getDataStore().get(
				ALL_CFP_MSGS);
		int ret = -1;
		if (allCfps != null && allProposes != null && allProposes.size() > 0) {
			ret = PROPOSE_RECEIVED;
		}
		if (reply != null) {
			if (sessions.size() > 0) {
				// If there are still active sessions we haven't received
				// all responses/result_notifications yet
				ret = -1;
			}
		} else {
			// Timeout has expired or we were interrupted --> clear all
			// remaining sessions
			sessions.clear();
		}
		return ret;
	}

	/**
	 * This method can be called (typically within the handlePropose() method)
	 * to skip all responses that have not been received yet.
	 */
	public void skipNextResponses() {
		skipNextRespFlag = true;
	}

	/**
	 * Return the states that must be reset before they are visited again. Note
	 * that resetting a state before visiting it again is required only if
	 * <ul>
	 * <li>The <code>onStart()</code> method is redefined.</li>
	 * <li>The state has an "internal memory".</li>
	 * </ul>
	 */
	protected String[] getStatesToBeReset() {
		if (statesToBeReset == null) {
			statesToBeReset = new String[] { HANDLE_PROPOSE,
					HANDLE_NOT_UNDERSTOOD, HANDLE_FAILURE, HANDLE_OUT_OF_SEQ };
		}
		return statesToBeReset;
	}

	/**
	 * Resets this behaviour.
	 * 
	 * @param msg
	 *            is the ACLMessage to be sent
	 **/
	public void reset(ACLMessage msg) {
		initiation = msg;
		reinit();
		super.reset();
	}

	/**
	 * Re-initialize the internal state without performing a complete reset.
	 */
	protected void reinit() {
		skipNextRespFlag = false;
		replyReceiver.reset(null, MsgReceiver.INFINITE, getDataStore(),
				REPLY_KEY);
		sessions.clear();
		DataStore ds = getDataStore();
		ds.remove(INITIATION_KEY);
		ds.remove(ALL_INFORM_MSGS);
		ds.remove(ALL_CFP_MSGS);
		ds.remove(REPLY_KEY);
		ds.remove(ALL_RECEIVED_PROPOSE_MSGS);
		ds.remove(ALL_PROPOSAL_MSGS);
		ds.remove(ALL_CLOSING_INFOM_MSGS);
		ds.remove(LAST_WINNER_AID_KEY);

	}

	// --- Inner classes -------------------------------------------------------

	/**
	 * Inner interface Session
	 */
	protected interface ProtocolSession {
		String getId();

		boolean update(int perf);

		int getState();

		boolean isCompleted();
	}

	/**
	 * Inner class Session
	 */
	class Session implements ProtocolSession, Serializable {

		// Session states
		static final int STATE_INFORM = 0;
		static final int STATE_BIDDING_ITERATION_STARTED = 1;
		static final int STATE_BIDDING_ITERATION_CLOSED = 2;
		static final int STATE_CLOSING = 3;

		private int state = STATE_INFORM;
		private int step;

		public Session(int state) {
			this.state = state;
		}

		public String getId() {
			return null;
		}

		/** Return true if received ACLMessage is consistent with the protocol */
		public boolean update(int perf) {
			switch (state) {
			case STATE_INFORM:
				return false; // not expected to receive answer in this state
			case STATE_BIDDING_ITERATION_STARTED:
				switch (perf) {
				case ACLMessage.PROPOSE:
				case ACLMessage.NOT_UNDERSTOOD:
				case ACLMessage.FAILURE:
					return true;
				default:
					return false;
				}
			case STATE_BIDDING_ITERATION_CLOSED:
				return false; // not expected to receive answer in this state
			case STATE_CLOSING:
				return false; // not expected to receive answer in this state
			}
			return false;
		}

		void setState(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}

		public boolean isCompleted() {
			return (state == STATE_CLOSING);
		}
	}

	private void log(String msg) {
		Level logLevel = ProdConsConfiguration.instance().getLogLevel();
		if (LOG.isLoggable(logLevel)) {
			LOG.log(logLevel, EA_INITIATOR + " [" + myAgent.getLocalName()
					+ "] " + msg + "\n");
		}
	}

	private Map getSessions() {
		return sessions;
	}

	// --- Inner Classes -------------------------------------------------------

	private static class PrepareInitiations extends OneShotBehaviour {
		private static final long serialVersionUID = -4216350173804405598L;

		public PrepareInitiations(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			DataStore ds = getDataStore();
			List<ACLMessage> allInitiations = (List<ACLMessage>) ds
					.get(parent.ALL_INFORM_MSGS);
			if (allInitiations == null || allInitiations.isEmpty()) {
				allInitiations = parent.prepareInitiations((ACLMessage) ds
						.get(parent.INITIATION_KEY));
				ds.put(parent.ALL_INFORM_MSGS, allInitiations);
			}
			parent.log("Created initiation (inform) msgs: \n" + allInitiations);
		}
	}

	private static class SendInitiations extends OneShotBehaviour {
		private static final long serialVersionUID = 3487495895818001L;
		private int sentInitiations = 0;

		public SendInitiations(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			List<ACLMessage> allInitiations = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_INFORM_MSGS);
			if (allInitiations != null) {
				parent.sendInitiations(allInitiations);
			}
			sentInitiations = parent.getSessions() != null ? parent
					.getSessions().size() : 0;
			parent
					.log("Sent " + sentInitiations
							+ " initiation (inform) msgs.");
		}

		public int onEnd() {
			return sentInitiations;
		}
	}

	private static class PrepareCfps extends OneShotBehaviour {
		private static final long serialVersionUID = -4216350173804405598L;

		public PrepareCfps(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			DataStore ds = getDataStore();
			List<ACLMessage> allInitiations = (List<ACLMessage>) ds
					.get(parent.ALL_INFORM_MSGS);
			List<ACLMessage> allCfps = new ArrayList<ACLMessage>();
			if (allInitiations != null && !allInitiations.isEmpty()) {
				String cfpMsgContent = parent.getCfpMsgContent();
				int cnt = 0; // counter of sessions
				for (ACLMessage initiation : allInitiations) {
					if (initiation == null) {
						continue;
					}
					ACLMessage cfpMsg = (ACLMessage) initiation.clone();
					cfpMsg.setPerformative(ACLMessage.CFP);
					cfpMsg.setContent(cfpMsgContent);
					allCfps.add(cfpMsg);
				}
			}
			getDataStore().put(parent.ALL_CFP_MSGS, allCfps);
			parent.log("The CFP msgs are: \n" + ds.get(parent.ALL_CFP_MSGS));
		}
	}

	private static class SendCfps extends OneShotBehaviour {
		private static final long serialVersionUID = 3487495895818001L;
		private int sentMsgs = 0;

		public SendCfps(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			List<ACLMessage> allCfps = (List<ACLMessage>) getDataStore().get(
					parent.ALL_CFP_MSGS);
			if (allCfps != null) {
				parent.sendCfps(allCfps);
			}
			allCfps = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_CFP_MSGS);
			sentMsgs = allCfps != null ? allCfps.size() : 0;
			parent.log("Sent " + sentMsgs + " cfp msgs.");
		}

		public int onEnd() {
			return sentMsgs;
		}
	}

	private static class ReplyReceiver extends MsgReceiver {
		private static final long serialVersionUID = 3189146150870994467L;

		public ReplyReceiver(Agent agent, MessageTemplate mt,
				DataStore dataStore, Object msgKey) {
			super(agent, mt, MsgReceiver.INFINITE, dataStore, msgKey);
		}

		@Override
		public void action() {
			super.action();

			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage receivedMsg = (ACLMessage) getDataStore().get(
					receivedMsgKey);
			if (receivedMsg != null) {
				parent.log("Received reply: \n" + receivedMsg);
			} else {
				parent.log("Waiting for reply.");
			}
		}
	}

	private static class SeqChecker extends OneShotBehaviour {
		private static final long serialVersionUID = 6749821436317749075L;
		private int ret;

		public SeqChecker(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage reply = (ACLMessage) getDataStore()
					.get(parent.REPLY_KEY);
			if (parent.checkInSequence(reply)) {
				ret = reply.getPerformative();
			} else {
				ret = -1;
			}
			parent.log("The received msg is " + ((ret == -1) ? "not" : "")
					+ " in sequence.");
		}

		public int onEnd() {
			return ret;
		}
	}

	private static class NotUnderstoodHandler extends OneShotBehaviour {
		private static final long serialVersionUID = 2921293720340880426L;

		public NotUnderstoodHandler(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage notUnderstoodMsg = (ACLMessage) getDataStore().get(
					parent.REPLY_KEY);
			parent.handleNotUnderstood(notUnderstoodMsg);
			parent.log("Handled NotUnderstood msg: " + notUnderstoodMsg);
		}
	}

	private static class FailureHandler extends OneShotBehaviour {
		private static final long serialVersionUID = -4216350173804405598L;

		public FailureHandler(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage failureMsg = (ACLMessage) getDataStore().get(
					parent.REPLY_KEY);
			parent.handleFailure(failureMsg);
			parent.log("Handled NotUnderstood msg: " + failureMsg);
		}
	}

	private static class OutOfSequenceHandler extends OneShotBehaviour {
		private static final long serialVersionUID = -6213277779567504677L;

		public OutOfSequenceHandler(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage outOfSequenceMsg = (ACLMessage) getDataStore().get(
					parent.REPLY_KEY);
			parent.handleOutOfSequence(outOfSequenceMsg);
			parent.log("Handled out of sequence msg: " + outOfSequenceMsg);
		}
	}

	private static class ProposeHandler extends OneShotBehaviour {
		private static final long serialVersionUID = 3487495895819003L;

		public ProposeHandler(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			DataStore ds = getDataStore();
			List<ACLMessage> proposes = (List<ACLMessage>) ds
					.get(parent.ALL_RECEIVED_PROPOSE_MSGS);
			ACLMessage proposeMsg = (ACLMessage) getDataStore().get(
					parent.REPLY_KEY);
			proposes.add(proposeMsg);
			parent.handlePropose(proposeMsg);
			ds.remove(parent.REPLY_KEY);
			parent.log("Added propose msg to the queue of proposes: \n"
					+ proposeMsg);
		}
	}

	private static class SessionsVerifier extends OneShotBehaviour {
		private static final long serialVersionUID = -516773887301538796L;
		private int ret;

		public SessionsVerifier(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			ACLMessage reply = (ACLMessage) getDataStore()
					.get(parent.REPLY_KEY);
			ret = parent.checkSessions(reply);
		}

		public int onEnd() {
			return ret;
		}
	}

	private static class PrepareProposals extends OneShotBehaviour {
		private static final long serialVersionUID = 7016866684098971629L;

		public PrepareProposals(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			parent.prepareProposals();
			List<ACLMessage> proposalsList = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_PROPOSAL_MSGS);
			parent.log("Prepared proposals msgs: \n" + proposalsList);
		}
	}

	private static class SendProposals extends OneShotBehaviour {
		private static final long serialVersionUID = 562863837405444096L;
		private int sentMsgs = 0;

		public SendProposals(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			List<ACLMessage> allProposals = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_PROPOSAL_MSGS);
			if (allProposals != null) {
				parent.sendProposals(allProposals);
			}
			allProposals = (List<ACLMessage>) getDataStore().get(
					parent.ALL_PROPOSAL_MSGS);
			int sentMsgs = allProposals != null ? allProposals.size() : 0;
			parent.log("Sent " + sentMsgs + " proposal msgs.");
			// parent.prepareForNextIteration();
		}

		public int onEnd() {
			return sentMsgs;
		}
	}

	private static class TerminateBiddingIteration extends OneShotBehaviour {
		private static final long serialVersionUID = -1736658624422116216L;

		public TerminateBiddingIteration(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			resetDataStore(parent);
			updateSessions(parent);
			parent.log("The bidding iteration terminated successfully.");
		}

		private void resetDataStore(EnglishAuctionInitiator parent) {
			DataStore ds = getDataStore();
			ds.remove(parent.ALL_CFP_MSGS);
			ds.remove(parent.REPLY_KEY);
			ds.remove(parent.ALL_RECEIVED_PROPOSE_MSGS);
			List<ACLMessage> allAcceptanceMsgs = new ArrayList<ACLMessage>();
			ds.put(parent.ALL_RECEIVED_PROPOSE_MSGS, allAcceptanceMsgs);
			ds.remove(parent.ALL_PROPOSAL_MSGS);
		}

		private void updateSessions(EnglishAuctionInitiator parent) {
			Collection sessionsCollection = parent.getSessions().values();
			Iterator sessionsIterator = sessionsCollection.iterator();
			while (sessionsIterator.hasNext()) {
				Session session = (Session) sessionsIterator.next();
				session.setState(Session.STATE_INFORM);
			}
		}
	}

	private static class PrepareClosingInforms extends OneShotBehaviour {
		private static final long serialVersionUID = 7016866684098971629L;

		public PrepareClosingInforms(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			parent.prepareCloseAuctionMsgs();
			List<ACLMessage> closeInformsList = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_CLOSING_INFOM_MSGS);
			parent.log("Prepared close INFORM msgs: \n" + closeInformsList);
		}
	}

	private static class SendClosingInforms extends OneShotBehaviour {
		private static final long serialVersionUID = -6053431674991597395L;
		private int sentMsgs = 0;

		public SendClosingInforms(Agent agent) {
			super(agent);
		}

		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			List<ACLMessage> allClosingInforms = (List<ACLMessage>) getDataStore()
					.get(parent.ALL_CLOSING_INFOM_MSGS);
			if (allClosingInforms != null) {
				parent.sendCloseAuctionMsgs(allClosingInforms);
			}
			allClosingInforms = (List<ACLMessage>) getDataStore().get(
					parent.ALL_CLOSING_INFOM_MSGS);
			sentMsgs = allClosingInforms != null ? allClosingInforms.size() : 0;
			parent.log("Sent " + sentMsgs + " closing auction inform msgs.");
		}

		public int onEnd() {
			return sentMsgs;
		}
	}

	private static class PrepareWinnerRequest extends OneShotBehaviour {
		private static final long serialVersionUID = -1085756158931322088L;

		public PrepareWinnerRequest(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			DataStore ds = getDataStore();
			AID lastWinnerAid = (AID) ds.get(parent.LAST_WINNER_AID_KEY);
			ACLMessage closingInform = getWinnerClosingInform(parent,
					lastWinnerAid);
			ACLMessage winnerRequest = prepareRequest(closingInform,
					lastWinnerAid);
			ds.put(parent.WINNER_REQUEST, winnerRequest);
			parent.log("Prepared winner request msg: \n"
					+ ds.get(parent.WINNER_REQUEST));
		}

		public ACLMessage prepareRequest(ACLMessage closingInform,
				AID lastWinnerAid) {
			ACLMessage request = (ACLMessage) closingInform.clone();
			request.setPerformative(ACLMessage.REQUEST);
			EnglishAuctionInitiator parent = (EnglishAuctionInitiator) getParent();
			Double lastWonPrice = (Double) getDataStore().get(
					parent.LAST_WON_PRICE_KEY);
			request.setContent(PAYMENT_EXPECTED + lastWonPrice);
			return request;
		}

		public ACLMessage getWinnerClosingInform(
				EnglishAuctionInitiator parent, AID lastWinnerAid) {
			DataStore ds = getDataStore();
			List<ACLMessage> allClosingInforms = (List<ACLMessage>) ds
					.get(parent.ALL_CLOSING_INFOM_MSGS);
			for (ACLMessage closingInform : allClosingInforms) {
				Iterator receiversit = closingInform.getAllReceiver();
				AID receiverAid = (AID) receiversit.next();
				if (receiverAid.equals(lastWinnerAid)) {
					return closingInform;
				}
			}
			return null;
		}
	}
}
