(implementation by Nikolay Vasilev and Rubén Ríos)

**NOTE:** This document still is in status `draft` which means that there could be parts which are not translated properly or which need rewriting. But with little bit of patience, soon this will be changed. We keep working on it. All suggestions, improvements and fixes found by the users of our code (or this short manual) are very welcomed! :)

# The Story #

This project was created after the implementation of concrete task, which was given as final project for the subject "Multi-agent Systems Programming", in the Ms. Sc. program "Software Engineering and Artificial Intelligence" (2008-2009) at University of Málaga, Spain.

# Motivation #

The reason which forced us to create this project was that at the moment of the implementation of our task (June 2009), we needed JADE-based implementation of the FIPA English Auction Interaction Protocol. Unfortunately nowhere in Internet we found such one (at least open source and free one). There was only the experimental protocol **specification** available on the official FIPA site. For that reason we have developed our own implementation of the protocol. We decided to publish it, in order to help to other fans of the Multi-agent System development who use JADE as programming language and who need this implementation.

(We have to note, that the original version of this document was written in Spanish, but we decided to translate it in English as well. Correspondingly the original Spanish version is going to be kept in this wiki, but the updates on the document first would appear here, and later there)

# Purpose #

During the implementation of our course-task, we tried to decouple as much as possible the implementation of the protocol from our specific-task domain logic in order the latter, to be able to evolve independently and to not affect (neither to be affected) by the domain logic to which it is applied. The idea is if there are further changes in the FIPA specification, they to be applied to our implementation without affecting the rest of the logic.

# Concrete Task #

The concrete task, which was the reason for the implementation of the FIPA specification is: there are given number of producers of some goods, and correspondingly given number of consumers. It is needed to be developed negotiation protocol between the producers and the consumers for the price of the goods.

# Content #


# Concrete Task-Solution Details #

The communication protocol between the producer and the consumers is chosen to be of the type English Auction, in which the producer offers its production on a given initial price `$0` and sells it to consumers, who correspondingly starts to bid up the price. At the end the consumer, who has offered highest price, win the bidding and receive the goods.

# Protocol Model #

The model (for the both sides - producer and consumer) consists of implementation of two sub-protocols (realized as behaviours):
  * [FIPA English Auction Interaction Protocol](http://www.fipa.org/specs/fipa00031/XC00031F.html)
  * [FIPA Request Interaction Protocol](http://www.fipa.org/specs/fipa00026/SC00026H.html)

The first protocol implements the auction. The second is responsible for the execution of the payment.

In the beginning is executed the first protocol. If there is winner in the auction, it is executed the second protocol. Otherwise, the second protocol is omitted and the protocol `Producer-Consumer` finishes without winner.

Each of these protocols are implemented as a Finite State Machine (using the class `jade.core.behaviours.FSMBehaviour`). Each of the states is a behaviour (`jade.core.behaviours.Behaviour`). According to the definition, each finite state machine has primitive internal memory, which is used for coordination between the states. In our case, as memory used for coordination between the internal states, is used an object of the type `jade.core.behaviours.DataStore` (which de facto is a map of the type `jade.util.leap.HashMap`).

## Producer-Consumer Protocol Model ##

Both implementations of the protocol (from the point of view of the producer, and correspondingly - of the consumer side) have similar structure.

### Producer Side ###

This protocol is implemented by the class `ProducerBehaviour`, which inherit the class `FSMBehaviour`. The diagram of the states is the following:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-producer.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-producer.png)

According to the `English Auction IP` protocol specification, if there is winner in the auction the protocol have to prepare an `ACLMessage` message with performative  `REQUEST`, which will be used as a initialization message by the `Request IP` protocol.

If there is a winner, the `English Auction IP` protocol creates the `REQUEST` message and stores it in the coordination memory. Afterwards is executed the other protocol (behaviour) - `Request IP`. It uses the previously stored message to start with the winner the communication related to the payment. After that the termination of the `Request IP`,  from the  `ProducerBehaviour` terminates as well.

If there is no `REQUEST` message the `ProducerBehaviour` protocol (behaviour) terminates directly.

This is the declaration of the `enum`:
```
    public enum AuctionTerminationEvent {
	NO_WINNER,
	WINNER_REFUSE_TO_PAY,
	PAYMENT_FAILURE,
	PAYMENT_OK
    }
```
It is used in the communication between the agent and the behaviour. Its aim is to let the producer know how the auction has finished.

Also this class declare several abstract methods which must be implemented by the agent, which wants to use that behaviour. The methods are the following one:
  * `public double getPrice()` - each time invoked, this method returns a new price for the auction iteration.
  * `public void handleTerminateEvent(AuctionTerminationEvent)` - notifies the producer for the event of exit from the auction.

### Consumer Side ###

This protocol implements the class `ConsumerBehaviour`, which inherits the class `FSMBehaviour`. The state diagram is the following:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-consumer.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-consumer.png)

The model of the consumer side is similar to the one of producer side. Before the termination of the behaviour `English Auction IP` the agent "knows" whether it is winner or not. If it is not winner, the protocol `ConsumerBehaviour` terminates. If the agent is winner is executed the protocol `Request IP`. During the initialization of the protocol `Request IP` is expected a `REQUEST` message which should arrive from the producer. Afterwards starts the communication between the agents between which will be performed the payment.

**NOTE**: The payment is not implemented in this protocol. This functionality is delegated to the consumer agent because it is quite specific for our implementation.

This class declares several abstract methods which must be implemented by the agent which wants to use this behaviour. The methods are the following:
  * `public boolean isPriceAcceptable(double)` - Returns `true` if the price proposed by the producer in order to sell the goods is accepted by the consumer and `false` otherwise.
  * `public boolean isReadyToPay(double)` - Checks whether the consumer is ready for the proposed payment or not.
  * `public boolean executePayment(AID,double)` - Executes the payment to the producer and returns `true` if the payment is successful and `false` otherwise.

## Model of the FIPA English Auction Interaction Protocol ##

At this moment, the specification of the protocol is in status "Experimental" and so far there is no implementation in the used `Jade` distribution. For that reason the English auction protocol had to be implemented from scratch.

### Producer Side ###

The main part of the protocol is implemented in the class `EnglishAuctionInitiator` which inherits from the class `FSMBehaviour`. Although at the end the agent uses the class `EnglishAuctionInitiatorImpl` (which inherits from `EnglishAuctionInitiator`), which makes the connection between the protocol and the agent, using the methods defined in the class `ProducerBehaviour`.

The state diagram is the following:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-initiator.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-initiator.png)

Each of the states is a behaviour (which in most of the cases inherit from `jade.core.behaviours.OneShotBehaviour`). Again as shared memory is used an object of type `jade.core.behaviours.DataStore`. In fact this `DataStore` is used by `ProducerBehaviour` as well.

The producer performs a `broadcast` to all agents registered in `DFService` independently of the name of the registered service.

### Consumer Side ###

The main part of the protocol is implemented in the class `EnglishAuctionParticipant` which inherits from the class `FSMBehaviour`. Finally, the agent uses the class `EnglishAuctionParticipantImpl` (which inherits from `EnglishAuctionParticipant`), which connects the protocol and the agent, using the methods provided by the class `ConsumerBehaviour`.

The state diagram is the following:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-participant.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-participant.png)

Each of the states is a behaviour (which in most of the cases inherits from `jade.core.behaviours.OneShotBehaviour`). Again, as a shared memory is used the object of type `jade.core.behaviours.DataStore`. This memory is used by the `ConsumerBehaviour` as well.

## Model of the FIPA Request Interaction Protocol ##

At this moment, the specification of the protocol is with status "Standard", thus there is implementation of in the used `Jade` distribution. According to [JADE Programmer's Guide](http://jade.tilab.com/doc/programmersguide.pdf), in order to implement this protocol are needed to be used the classes `jade.proto.AchieveREInitiator` and `jade.proto.AchieveREResponder`. For that reason were used these classes.

### Producer Side ###

The main part of the protocol is implemented by the class `RequestInteractionProtocolInitiator` which inherits from the class `AchieveREInitiator`. In fact the agent uses the class `RequestInteractionProtocolInitiatorImpl` (which inherits from `RequestInteractionProtocolInitiator`), which is the connection between the protocol and the agent, using the methods defined in the class `ProducerBehaviour`.

In the beginning, the class `RequestInteractionProtocolInitiator` obtains the `REQUEST` message from the `DataStore`, stored by the English auction behaviour.

At the end of the execution, the class `RequestInteractionProtocolInitiatorImpl` uses the methods by the class `ProducerBehaviour` and notifies the producer agent for the exit of the payment.

### Consumer Side ###

The main part of the protocol is implemented by the class `RequestInteractionProtocolResponder` which inherits from the class `AchieveREResponder`. In fact the agent uses the class `RequestInteractionProtocolResponderImpl` (which inherits from `RequestInteractionProtocolResponder`), which is the connection between the protocol and the agent, using the methods defined in the class `ConsumerBehaviour`.

At the beginning, the class `RequestInteractionProtocolResponder` receives the `REQUEST` message by the producer and prepare the response.

The class `RequestInteractionProtocolResponderImpl` uses the methods of the class `ConsumerBehaviour` and "asks" the corresponding agent whether is ready to pay. Respectively if the answer is positive, the payment terminates successfully.

# Architecture #

The architecture of the protocol `Producer-Consumer` is the following:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prodcons-protocol.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prodcons-protocol.png)

All the classes from this protocol use the standard `Java logging`. Its configuration could be modified in the class `ProdConsConfiguration`.

The internal states of the English auction protocol (in the both sides - producer and consumer) are implemented as `nested classes`.

## Nested Classes of the Producer ##

The nested classes by the producer side are implemented in the class `EnglishAuctionInitiator`. They are as follows:
  * `PrepareInitiations` - prepares the initiation messages of the protocol (with performative `INFORM`).
  * `SendInitiations` - sends the initiation messages.
  * `PrepareCfps` - prepares the initiation messages for each iteration of the auction (with performative `CFP`).
  * `SendCfps` - sends the initiation messages of the initiation of an auction iteration.
  * `ReplyReceiver` - responsible for collection of the received messages.
  * `SeqChecker` - verifies that the received messages are in the expected by the protocol sequence.
  * `NotUnderstoodHandler` - manage the messages which are not expected by the protocol.
  * `FailureHandler` - manages the messages causing failure.
  * `OutOfSequenceHandler` - manages the messages which are not in sequence.
  * `ProposeHandler` - manages the confirmations by the consumer (messages with performative `PROPOSE`).
  * `SessionsVerifier` - verifies that each consumer which is contacted participates in the auction (for the purpose, there is an session object which is kept by the producer).
  * `PrepareProposals` - prepares the notifications which the consumer receives whether it is winner in the current iteration of the auction (i.e. messages with performatives `ACCEPT_PROPOSAL` and `REJECT_PROPOSAL`).
  * `SendProposals` - sends the notifications.
  * `TerminateBiddingIteration` - terminates the current iteration of the auction.
  * `PrepareClosingInforms` - prepares the messages for termination of the auction (messages with performative `INFORM` and which is the winner of the auction).
  * `SendClosingInforms` - sends the messages for termination of the English auction to the consumers (message with performative `INFORM`).
  * `PrepareWinnerRequest` - prepares the message with performative `REQUEST` which will be used as initialization message by the protocol `Request Interaction Protocol`.

## Nested Classes of the Consumer ##

The nested classes by the side of the consumer are implemented in the class `EnglishAuctionParticipant`. They are the following:
  * `InitInformReceiver` - receives the message with performative `INFORM` with which the producer starts the protocol.
  * `NextMsgReceiver` - receives the messages after the first `INFORM` message.
  * `SeqChecker` - verifies that the received messages are in sequence expected by the protocol.
  * `OutOfSeqHandler` - manages the messages which are out of sequence.
  * `NextReplySender` - sends the responses of the messages received during the auction.
  * `CfpHandler` - manages the offers by the producer (messages with performative `INFORM`.
  * `AcceptProposalHandler` - manages the confirmation of the producer which the producer in the current iteration of the auction (message with performative `ACCEPT_PROPOSAL`).
  * `RejectProposalHandler` - manages the rejection of the producer to the consumers which are not winners in the current iteration of the auction (message with performative `REJECT_PROPOSAL`).
  * `TerminateBiddingIteration` - terminates the current iteration of the auction and cleans the coordination memory from the temporary information stored during the iteration.
  * `ClosingInformHandler` - manages the message which closes the auction (message with performative `INFORM`).

# Requirements #

## Technical Requirements ##

This protocol is implemented using `Java SDK 1.6` and `Jade 3.7`.

## Consumer's Requirements ##

In order to participate in an auction, **the consumer must register at least one service in `DFService`**.

# Usage #

The agents have to create the behaviours using the respective constructors of the classes `ProducerBehaviour` and `ConsumerBehaviour`. The producer and consumer have to create an anonymous class (or just a class which inherits from the needed behaviour) implementing the abstract methods defined respectively in `ProducerBehaviour` and `ConsumerBehaviour`.

## Usage by the Producer ##

The producer's behaviour can be used in the following manner:

```
public class ProducerAgent extends Agent {

    // --- Constructor ---------------------------------------------------------

    public ProducerAgent() {
    }

    // --- Methods (Agent) -----------------------------------------------------

    @Override
    protected void setup() {
	// ...
	pb = new ProducerBehaviour(this) {

            @Override
            public double getPrice() {
                double newPrice = ...; // your calculation of the new price here
                return newPrice;
            }

            @Override
            public void handleTerminateEvent(AuctionTerminationEvent event) {
                // your handling of the exit event from the auction here
            }
        };
        addBehaviour(pb);
	// ...
    }
}
```

The class `ProducerMockAgent` is an simple example of a producer agent.

## Usage by the Consumer ##

The consumer's behaviour can be used in the following manner:

```
public class ConsumerAgent extends Agent {

    // --- Constructors --------------------------------------------------------

    public ConsumerAgent() {
    }

    // --- Methods (Agent) -----------------------------------------------------

    @Override
    protected void setup() {
	// ...
	ConsumerBehaviour cb = new ConsumerBehaviour(this) {

            @Override
            public boolean isPriceAcceptable(double offeredPrice) {
                boolean isAcceptable = ...; // your implementation here
                return isAcceptable;
            }

            @Override
            public boolean isReadyToPay(double price) {
                boolean isReadyToPay = ...; // your implementation here
                return isReadyToPay;
            }

            @Override
            public boolean executePayment(AID aid, double price) {
                boolean isPaymentSuccessful = ...; // your implementation of the payment here
                return isPaymentSuccessful;
            }
        };
	addBehaviour(cb);
	// ...
    }
}
```

The class `ConsumerMockAgent` is an simple example of a consumer agent.

# Examples #

In the folder `examples` there is a demonstration how is used the protocol `Producer-Consumer`.

## Clases ##

### Fake Agent Classes ###

In the package `agentes09.negotiation.ProdConsProtocol.mock.agents` are implemented several fake agents:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-examples-mock-agents.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-examples-mock-agents.png)

Here there is short description of the classes from the diagram:
  * `MockAgent` - a fake agent which registers itself in `DFService`.
  * `ProducerMockAgent` - a fake producer agent which creates and adds among its behaviours the behaviour of the protocol `ProducerBehaviour`.
  * `ConsumerMockAgent` - a fake consumer agent, which creates and adds to its behaviours the behaviour of the protocol `ConsumerBehaviour`.
  * `OnceBidConsumer` - a fake consumer agent which participates in the auction, but confirms only in the first iteration.
  * `TwiceBidsConsumer` - a fake consumer agent which participates in the auction, but confirms only the first two iterations.
  * `ThreeBidsConsumer` - a fake consumer agent which participates in the auction and the confirms only the first three times of the auction. It is an abstract class.
  * `ThreeBidsConsumerNotReadyToPay` - a fake consumer agent of type `ThreeBidsConsumer`, which implements the method `isReadyToPay(double)` to return always `false`. This causes the termination of the protocol and the payment step is never performed.
  * `ThreeBidsConsumerReadyToPayWithPaymentFailure` - a fake consumer agent of type `ThreeBidsConsumer`, which implements the method `isReadyToPay(double)` to return always `true` but the method `executePayment(AID,double)` always to return `false`. In other words, a failure occurs during the payment by the consumer.
  * `ThreeBidsConsumerReadyToPayPaymentOK` - a fake consumer agent of type `ThreeBidsConsumer` which implements the methods `isReadyToPay(double)` and `executePayment(AID,double)` always to return `true`. This means that there is a winner in the auction the purchase is successful.
  * `DoNothingAgent` - a fake agent which only "overhear" the messages and prints them in the command line. Its aim is to demonstrate that the provider sends messages not only to the consumers but to all of the agents (i.e. that the producers makes a `broadcast`).
  * `LazyAgent` - a fake fictitious agent which only "overhear" for messages.
  * `SleepyAgent` - a fake fictitious agent which only "overhear" for messages.
  * `NotServicesRegisteredAgent` - a fake fictitious agent which does not register any service in `DFService` and for that reason later is not discovered by the producer agent. The aim of this agent is to demonstrate that if an agent does not register any service, it does not participate in the auction.

### Example Classes ###

The examples are in the package `agentes09.negotiation.ProdConsProtocol.examples`. The classes implement several general cases:
  * `ExampleProdCons` - an abstract class which prepares and tests the agents container.
  * `ExampleProdConsNoConsumers` - in this class is shown the case when there are no consumers. Just the producer terminates the behaviour without sending any message.
  * `ExampleProdConsWinnerRefuseToPay` - in this class are created a producer and three consumers. One consumer is of type `OnceBidConsumer`, other is of type `TwiceBidsConsumer` and the third - `ThreeBidsConsumerNotReadyToPay`. In this case the protocol terminates without purchase (with message `REFUSE`).
  * `ExampleProdConsPaymentFails` - in this class are created a producer and thee consumers. One consumer is of type `OnceBidConsumer`, other is of type `TwiceBidsConsumer` and the third - `ThreeBidsConsumerNotReadyToPay`. In this case the protocol terminates without purchase (with message `REFUSE`). This a simulation of a failure that occurs during the payment by the consumer.
  * `ExampleProdConsOK` - in this class are created a producer and thee consumers. One consumer is of type `OnceBidConsumer`, other is of type `TwiceBidsConsumer` and the third - `ThreeBidsConsumerNotReadyToPay`. This is a simulation of termination of the protocol successfully, i.e. there is a winner in the auction and it purchases (i.e. successfully pays) the won good.

All these classes are initialized by an agent of type `jade.tools.sniffer.Sniffer`, which intercepts the communication between the producer and consumers and visualize the message exchange between the agents.

## Examples Output ##

Here is presented the output of the agent `Sniffer` in each of the examples (except the first one).

In each of the outputs can be seen that the agents `lazy-agent` and `sleepy-agent`
(as they are not consumers, but they have registered services in `DFService`) receive the messages from the auction. On the other hand, the agent named `no-registered` does not receive any message from the auction, because it has not registered any services in `DFService`.

### Example - The Consumer Rejects the Payment ###

This is an output after the execution of the class `ExampleProdConsWinnerRefuseToPay`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-refuse.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-refuse.png)

It can be see seen that the last message sent by the winner consumer is with performative  `REFUSE`.

### Example - Failure of the Consumer Payment ###

This is an output from the execution of the class `ExampleProdConsPaymentFails`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-failure.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-failure.png)

It can be seen that the last message sent by the consumer-winner is with performative `FAILURE`.

### Example - The Auction Terminates Successfully ###

This is an output from the execution of the class `ExampleProdConsOK`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-ok.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-ok.png)

It can be seen that the last message sent by the consumer-winner is with performative `INFORM`, which means that the payment is terminated successfully.

# Future Works #

The `Producer-Consumer` protocol could be improved in the following ways:
  * Could be separated the coordination memory which at the moment is used by the `Producer-Consumer` protocol (the classes `ProducerBehaviour` and `ConsumerBehaviour`) and its sub-protocols `FIPA English Auction IP` (`EnglishAuctionInitiator` and `EnglishAuctionParticipant`) and `FIPA Request IP` (`RequestInteractionProtocolInitiator` and `RequestInteractionProtocolResponder`). In other words, at the moment for example on the producer's side, the coordination memory of the behaviuor `ProducerBehaviour` which is used for communication between the sub-behaviours `EnglishAuctionInitiator` and `RequestInteractionProtocolInitiator`, is used as well for the coordination between the internal states (behaviours) of the `EnglishAuctionInitiator` y `RequestInteractionProtocolInitiator`. As a solution, it is possible in each of the classes `EnglishAuctionInitiator` and `RequestInteractionProtocolInitiator` to be created an object of type `jade.core.behaviours.DataStore`, which will be used for coordination of the internal states of these sub-protocols. In that way the coordination memory of `ProducerBehaviour` will contain only information related to the execution of the `ProducerBehaviour`. The situation of the classes which implements the consumer's side - `ConsumerBehaviour`, `EnglishAuctionParticipant` and `RequestInteractionProtocolResponder` is equivalent.

  * It is possible to be refactored the nested classes of the `FIPA English Auction IP` protocol, especially the one which prepare the messages for sending and the one which sends the aforementioned messages.

  * It is interesting to refactor the classes `EnglishAuctionInitiator` and `EnglishAuctionParticipant` to reuse the logic implemented in `Jade`, i.e. how is possible to be inherited the classes `jade.proto.Initiator` and `jade.proto.Responder` respectively by the classes `EnglishAuctionInitiator` and `EnglishAuctionParticipant`.