# Introducción #


Definición de los protocolos de negociación entre centros de producción y centros de consumo sobre el precio de la mercancía.

# Contenido #


# Detalles #
El protocolo de comunicación a desarrollar entre productores y consumidores será finalmente del estilo subasta inglesa, en la que el productor ofrece su mercancia a un precio inicial $0 y vende la mercancía al mejor postor.

## Interacciones ##

Según la FIPA (http://www.fipa.org/specs/fipa00031/index.html), el protocolo tiene lugar de la siguiente manera: el subastador (productor en nuestro caso) trata de encontrar el precio de mercado de un bien o producto proponiendo un precio de salida inferior que el del valor de mercado y va aumentando gradualmente el precio. Cada vez que el subastador anuncia el precio, éste espera la señal de compradores que estén dispuestos a pagar el precio propuesto. Tan pronto como un comprador indica que acepta el precio, el subastador hace un nuevo llamamiento de convocatoria de ofertas con un precio ligeramente superior. La subasta continúa hasta que los compradores no están dispuestos a pagar el precio propuesto. El producto se vende al comprador que realizó la última oferta por el precio acordado.

A continuación se muestra de manera detallada el proceso de subasta inglesa

<a href='Hidden comment: 
[http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/Prod-Cons-english-auction.jpg]
'></a>

En la figura puede observarse que al final de la subasta, cuando ninguno de los consumidores realiza una nueva oferta, el productor informa tanto al ganador de la puja como a los otros consumidores. El consumidor que gana la puja puede en última instancia decidir si compra o no el producto, y esto se indica a través del último mensaje que se muestra en la figura anterior, que puede ser de cualquiera de los tres tipos siguientes:

  * REFUSE: el consumidor decide no pagar
  * FAILURE: si ocurre algún problema y el pago no se puede llevar a cabo
  * INFORM: tras realizar el pago se informa al productor

<a href='Hidden comment: 
En la figura puede observarse que hay unos mensajes que hemos marcado como opcionales y otros como obligatorios. Los mensajes obligatorios son considerados por la FIPA pero para nuestro propósito consideramos que podrían ser obviados. En concreto, los mensajes que se consideran opcionales son los mensajes de inicio de subasta y los de aceptación de una oferta. En el caso de los mensajes de inicio de subasta los consideramos opcionales porque se utilizarán sesiones para identificar la frescura de los mensajes. Por otra parte, los mensajes de aceptación / rechazo de propuestas no los consideraremos inicialmente porque según la FIPA estos son de utilidad para el subastador (productor) en el caso de que este compruebe que la oferta hecha por el solicitante (consumidor) es real, es decir, que tiene el dinero suficiente para pagar el producto, situación que no consideraremos en la versión actual.
'></a>


<a href='Hidden comment: 
En un principio se desarrollará un protocolo básico con una única ronda. A continuación se presenta el diagrama de secuencia del protocolo en el que se incluyen los diferentes tipos de mensajes que se intercambiarán entre las distintas partes involucradas.

[http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/old/Prod-Cons-basico.jpg]

En un futuro se plantea hacer una negociación con varias rondas. El productor indica el precio de partida de la subasta y los consumidores harán una oferta incial o simplemente rechazarán la puja. Aquellos consumidores que acepten la oferta entrarán en una nueva ronda en la que el precio de partida será el mejor de los ofrecidos entre los diferentes consumidores interesados en el producto. El proceso continuará hasta que sólo quede un consumidor o bien todos rechacen la nueva oferta, en cuyo caso el productor ofrece el producto al mejor postor de la ronda anterior. Véase el diagrama de secuencia siguiente.

[http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/Prod-Cons-avanzado.jpg]

'></a>

## Modelo del Protocolo ##

El modelo (para ambos - productor y consumidor) consiste de implementación de dos sub-protocolos (implementado como comportamientos):
  * [FIPA English Auction Interaction Protocol](http://www.fipa.org/specs/fipa00031/XC00031F.html)
  * [FIPA Request Interaction Protocol](http://www.fipa.org/specs/fipa00026/SC00026H.html)

El primer protocolo realiza la subasta. El segundo es responsable para ejecutar el pago.

En primer lugar se ejecuta el primer protocolo. Si hay ganador en la subasta, se ejecuta el segundo protocolo. En otro caso el segundo protocolo se omita y el protocolo de `Productor-Consumidor` termine sin ganador.

Cada uno de estos protocolos está implementado como una maquina de estados finitos (usando `jade.core.behaviours.FSMBehaviour`). Cada uno de los estados es un comportamiento (`jade.core.behaviours.Behaviour`). Según de la definición, cada maquina de estados finitos hay una memoria primitiva interna, que se usa para coordinación entre los estados. En nuestro caso, como una memoria de coordinación, se usa un objeto del tipo `jade.core.behaviours.DataStore` (que de hecho es un mapa del tipo `jade.util.leap.HashMap`).

### Modelo del Protocolo Productor-Consumidor ###

Las dos implementaciónes de este protocolo (de la parte del productor y de la parte de consumidor)

#### Parte del Productor ####

Este protocolo está implementado de la clase `ProducerBehaviour` que hereda la clase `FSMBehaviour`. La diagrama de los estados es la siguiente:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-producer.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-producer.png)

Según la especificación del protocolo `English Auction IP`, si hay ganador de la subasta el protocolo tiene que preparar un mensaje `ACLMessage` con performative `REQUEST`, que se va a utilizar como un mensaje de inicialización del protocolo `Request IP`.

Si hay un ganador, el protocolo `English Auction IP` crea el mensaje `REQUEST` y lo guarda en la memoria de coordinación. Después se ejecuta el otro protocolo (comportamiento) `Request IP`. Él usa el mensaje guardado antes, para empezar la comunicación con el ganador sobre el pago. Después la salida del `Request IP`,  `ProducerBehaviour` termina también.

Si no hay mensaje `REQUEST` el protocolo (comportamiento) `ProducerBehaviour` termina directamente.

#### Parte del Consumidor ####

Este protocolo está implementado de la clase `ConsumerBehaviour` que hereda la clase `FSMBehaviour`. La diagrama de los estados es la siguiente:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-consumer.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-prodcons-consumer.png)

El modelo de este lado es similar como el modelo de la parte del productor. Antes la salida del comportamiento `English Auction IP` el agente consumidor sabe si es ganador o no. Si no es ganador el protocolo `ConsumerBehaviour` termina. Si es ganador, se ejecuta el protocolo `Request IP`. Cuando se incializa el protocolo `Request IP` se espera para el mensaje `REQUEST` que tiene que venir del productor. Después empieza la comunicación entre los agentes con que se ejecuta el pago.

**NOTA**: El pago no está implementado en este protocolo. Esta funcionalidad está delegada al agente consumidor porque es especifica.

### Modelo del FIPA English Auction Interaction Protocol ###

En este momento, la especificación del protocolo está con estatus "Experimental" y entonces no hay implementación en la distribución de `Jade` usada. Por eso el protocolo de subasta inglesa se había implementado desde el principio.

#### Parte del Productor ####

La parte principal del protocolo está implementado en la clase `EnglishAuctionInitiator` que hereda la clase `FSMBehaviour`. Aunque realmente el agente usa la clase `EnglishAuctionInitiatorImpl` (que hereda `EnglishAuctionInitiator`), que hace la conexión entre el protocolo y el agente, usando los métodos definidos en el interfaz `Producer`.

La diagrama de los estados es la siguiente:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-initiator.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-initiator.png)

Cada uno de los estados es un comportamiento (que en los más de los casos hereda `jade.core.behaviours.OneShotBehaviour`). De nuevo como una memoria compartida se usa un objeto del tipo `jade.core.behaviours.DataStore`. De hecho este `DataStore` es el uno que se utiliza del `ProducerBehaviour` también.

#### Parte del Consumidor ####

La parte principal del protocolo está implementado en la clase `EnglishAuctionParticipant` que hereda la clase `FSMBehaviour`. Aunque realmente el agente usa la clase `EnglishAuctionParticipantImpl` (que hereda `EnglishAuctionParticipant`), que hace la conexión entre el protocolo y el agente, usando los métodos definidos en el interfaz `Consumer`.

La diagrama de los estados es la siguiente:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-participant.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/state/state-diagram-english-auction-participant.png)

Cada uno de los estados es un comportamiento (que en los más de los casos hereda `jade.core.behaviours.OneShotBehaviour`). De nuevo como una memoria compartida se usa un objeto del tipo `jade.core.behaviours.DataStore`. De hecho este `DataStore` es el uno que se utiliza del `ConsumerBehaviour` también.

### Modelo del FIPA Request Interaction Protocol ###

En este momento, la especificación del protocolo está con estatus "Standard" y entonces hay implementación en la distribución de `Jade` usada. Según el [JADE Programmer's Guide](http://jade.tilab.com/doc/programmersguide.pdf) para implementar este protocolo se tienen que usar las clases `jade.proto.AchieveREInitiator` y `jade.proto.AchieveREResponder`. Por eso habían usados estas clases.

#### Parte del Productor ####

La parte principal del protocolo está implementado de la clase `RequestInteractionProtocolInitiator` que hereda la clase `AchieveREInitiator`. Aunque realmente el agente usa la clase `RequestInteractionProtocolInitiatorImpl` (que hereda `RequestInteractionProtocolInitiator`), que hace la conexión entre el protocolo y el agente, usando los métodos definidos en el interfaz `Producer`.

En el principio, la clase `RequestInteractionProtocolInitiator` obtiene el mensaje `REQUEST` guardado del comportamiento de la subasta inglesa en el `DataStore`.

Al fin de la ejecución, la clase `RequestInteractionProtocolInitiatorImpl` usa los métodos del interfaz `Producer` y notifica el agente productor para la salida del pago.

#### Parte del Consumidor ####

La parte principal del protocolo está implementado de la clase `RequestInteractionProtocolResponder` que hereda la clase `AchieveREResponder`. Aunque realmente el agente usa la clase `RequestInteractionProtocolResponderImpl` (que hereda `RequestInteractionProtocolResponder`), que hace la conexión entre el protocolo y el agente, usando los métodos definidos en el interfaz `Consumer`.

En el principio, la clase `RequestInteractionProtocolResponder` recibe el mensaje `REQUEST` del productor y prepara una respuesta.

La clase `RequestInteractionProtocolResponderImpl` usa los métodos del interfaz `Consumer` y "pregunta" el agente correspondiente si está listo a hacer pago y correspondientemente si la respuesta es positiva, si el pago está con éxito.

## Arquitectura ##

La arquitectura del protocolo `Productor-Consumidor` es la siguiente:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prodcons-protocol.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prodcons-protocol.png)

Todas las clases de este protocolo usan el `logging` estándar de `Java`. Su configuración se puede modificar en la clase `ProdConsConfiguration`.

Los estados internos del protocolo de subasta inglesa (en las ambos partes - del productor y del consumidor) son implementado como `nested classes`.

### Nested Classes del Productor ###

Las nested clases de la parte del productor están implementado en la clase `EnglishAuctionInitiator`. Ellas son las siguientes:
  * `PrepareInitiations` - prepara los mensajes de iniciación del protocolo (con performative `INFORM`).
  * `SendInitiations` - envía los mensajes de iniciación.
  * `PrepareCfps` - prepara los mensajes de iniciación de una iteración de la subasta (con performative `CFP`).
  * `SendCfps` - envía los mensajes de iniciación de la iteración de la subasta.
  * `ReplyReceiver` - responsable de acumular los mensajes recibidos.
  * `SeqChecker` - verifica que los mensajes recibidos son en la secuencia requerida del protocolo.
  * `NotUnderstoodHandler` - maneja mensajes que no están como se espera en el protocolo.
  * `FailureHandler` - maneja mensajes que causan falla.
  * `OutOfSequenceHandler` - maneja mensajes que no están en secuencia.
  * `ProposeHandler` - maneja las confirmaciones del consumidor (mensajes con performative `PROPOSE`).
  * `SessionsVerifier` - verifica que cada consumidor que se pone en contacto es parte de la subasta (es decir, tiene un objeto de sesión que se guarda del productor).
  * `PrepareProposals` - prepara las notificaciones quien consumidor está ganador en la corriente iteración de la subasta (es decir mensajes con performatives `ACCEPT_PROPOSAL` y `REJECT_PROPOSAL`).
  * `SendProposals` - envía las notificaciónes.
  * `TerminateBiddingIteration` - termina la corriente iteración de la subasta.
  * `PrepareClosingInforms` - prepara mensajes para terminación de la subasta (mensajes con performative `INFORM` y contenido quien es el ganador de la subasta).
  * `SendClosingInforms` - envía los mensajes para terminación de la subasta inglesa a los consumidores (mensaje con performative `INFORM`).
  * `PrepareWinnerRequest` - prepara el mensaje con performative `REQUEST` que se va a usar como mensaje de inicialización del protocolo `Request Interaction Protocol`.

### Nested Classes del Consumidor ###

Las nested clases de la parte del consumidor están implementado en la clase `EnglishAuctionParticipant`. Ellas son las siguientes:
  * `InitInformReceiver` - recibe el mensaje con performative `INFORM` con que el productor empieza el protocolo.
  * `NextMsgReceiver` - recibe los mensajes después el primer mensaje `INFORM`.
  * `SeqChecker` - verifica que los mensajes recibidos son en la secuencia requerida del protocolo.
  * `OutOfSeqHandler` - maneja mensajes que no están en secuencia.
  * `NextReplySender` - envía las respuestas de los mensajes recibidos durante la subasta.
  * `CfpHandler` - maneja las ofertas del productor (mensajes con performative `INFORM`.
  * `AcceptProposalHandler` - maneja la confirmación del productor que el consumidor está el ganador en la iteración corriente de la subasta (mensaje con performative `ACCEPT_PROPOSAL`).
  * `RejectProposalHandler` - maneja el rechazo del productor que el consumidor no está ganador en la iteración corriente de la subasta (mensaje con performative `REJECT_PROPOSAL`).
  * `TerminateBiddingIteration` - termina la iteración corriente de la subasta y limpia la memoria de coordinación de la información guardada durante la iteración.
  * `ClosingInformHandler` - maneja el mensaje que cierra la subasta (mensaje con performative `INFORM`).

<a href='Hidden comment: 
La arquitectura que se intente usar por ahora se base en el protocolo de comunicación FIPA-Contract-Net.

[http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/old/Prod-Cons-Class-Diagram.png]

El productor es el iniciador de la conversación y él es el uno que la termina. El consumidor desempeña el papel de respondedor. Por eso, según de la especificación del protocolo FIPA-Contract-Net, el productor tiene que añadir un comportamiento del tipo ProducerProtocolHandler (porque hereda ContractNetInitiator) y el consumidor - el comportamiento ConsumerProtocolHandler (heredando ContractNetResponder).

ProducerProtocolHandler y ConsumerProtocolHandler no procesan los mensajes que reciben de sus mismo. Ellos dedican el procesamiento a otras clases (que heredan el clase Behaviour), como cada clase procesa solo un tipo de mensaje. Los procesadores de los mensajes son los siguiente:

* Del parte del productor (todos heredan la subclase ProducerMsgHandler):
* RefuseMsgHandler  - esa clase procesa ACLMessages con performative REFUSE.
* ProposeMsgHandler  - esa clase procesa ACLMessages con performative PROPOSE.
* Del parte del consumidor (todos heredan la subclase ConsumerMsgHandler):
* CFPMsgHandler - esa clase procesa ACLMessages con performative CFP.
* RejectProposalMsgHandler  - esa clase procesa ACLMessages con performative REJECT_PROPOSAL.
* AcceptProposalMsgHandler  - esa clase procesa ACLMessages con performative ACCEPT_PROPOSAL.
'></a>

## Requisitos ##

### Requisitos Técnicos ###

Este protocolo está implementado usando `Java SDK 1.6` y `Jade 3.7`.

### Requisitos de Uso ###

Se espera que la gente responsable en la implementación de los agentes productor y consumidor, van a realizar los interfaces siguientes:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prod-cons-requirements.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-prod-cons-requirements.png)

### Requisitos de Inscripción de Servicios de los Consumidores ###

Se tiene que saber que comportamiento `ProducerBehaviour` automáticamente encuentra los consumidores y empieza la subasta.

Él acepta que un consumidor es agente que está registrado en el `DFService` servicio con nombre `consumer`. Este nombre está definido en la clase `EnglishAuctionInitiatorFactory` en el constante `SEARCHED_SERVICE_DESC_TYPE`.

**NOTA**: Eso está implementado en esta manera hasta los equipos que desarrollan correspondientemente el agente productor y el agente consumidor definen como se distingue un consumidor de los productores. Después si se elige la clave que tiene que ser diferente, solo se tiene que cambiar el valor del constante `SEARCHED_SERVICE_DESC_TYPE`.

### Requisitos del Productor ###

Se espera que el agente productor va a realizar este interfaz:

```
public interface Producer {

    /**
     * Enumeration que usa en la comunicación entre el agente y el comportamiento
     * (behaviour). Su meta es a notificar el productor para la salida de la subasta.
     */
    public enum AuctionTerminationEvent {
	NO_WINNER,
	WINNER_REFUSE_TO_PAY,
	PAYMENT_FAILURE,
	PAYMENT_OK
    }

    /**
     * Devuelve el precio nuevo ofrecido del productor.
     * 
     * @return Devuelve el precio nuevo ofrecido del productor.
     */
    double getPrice();

    /**
     * Notifica el productor para el evento de salida de la subasta.
     * 
     * @param event
     *         El evento de salida de la subasta.
     */
    void handleTerminateEvent(AuctionTerminationEvent event);
}
```

### Requisitos del Consumidor ###

Se espera que el agente consumidor va a realizar este interfaz:

```
public interface Consumer {

    /**
     * Devuelve true si el precio sugerido por el productor
     * para vender la mercancía es aceptado por el consumidor
     * y false en otro caso.
     * 
     * @return Devuelve true si el precio sugerido por el productor
     *         para vender la mercancía es aceptado por el consumidor
     *         y false en otro caso.
     */
    boolean isPriceAcceptable(double offeredPrice);

    /**
     * Prueba si el consumidor está listo para pagar el precio sugerido.
     * 
     * @param price
     *         El precio que se espera.
     * @return Devuelve true si el consumidor está listo para pagar el
     *         precio y false en otro caso.
     */
    boolean isReadyToPay(double price);

    /**
     * Ejecuta el pago al productor.
     * 
     * @param aid
     *         El AID del productor a quien este consumidor va a pagar.
     * @param price
     *         El precio que se espera.
     * @return Devuelve true si el pago termina con éxito y false en otro caso.
     */
    boolean executePayment(AID aid, double price);
}
```

## Uso ##
Los agentes tienen que crear los comportamientos usando los constructores correspondientemente de las dos clases `ProducerBehaviour` y `ConsumerBehaviour`.

### Uso del Productor ###

El comportamiento del productor se puede usar en esta manera:

```
public class ProducerAgent extends Agent implements Producer {

    // --- Constructor ---------------------------------------------------------

    public ProducerAgent() {
    }

    // --- Methods (Agent) -----------------------------------------------------

    @Override
    protected void setup() {
	// ...
	ProducerBehaviour pb = new ProducerBehaviour(this);
	addBehaviour(pph);
	// ...
    }

    // --- Methods (Producer) --------------------------------------------------

    @Override
    public double getPrice() {
	double newPrice = ...; // su cálculo del precio nuevo aquí
	return newPrice;
    }

    @Override
    public void handleTerminateEvent(AuctionTerminationEvent event) {
	// su tratamiento del evento de salida de la subasta aquí
    }
}
```

La clase `ProducerMockAgent` es un ejemplo tonto de un agente productor.

### Uso del Consumidor ###

El comportamiento del consumidor se puede usar en esta manera:

```
public class ConsumerAgent extends Agent implements Consumer {

    // --- Constructors --------------------------------------------------------

    public ConsumerAgent() {
    }

    // --- Methods (Agent) -----------------------------------------------------

    @Override
    protected void setup() {
	// ...
	ConsumerBehaviour cb = new ConsumerBehaviour(this);
	addBehaviour(cb);
	// ...
    }

    // --- Methods (Consumer) --------------------------------------------------

   @Override
    public boolean isPriceAcceptable(double offeredPrice) {
	boolean isAcceptable = ...; // su implementación aquí
	return isAcceptable;
    }

    @Override
    public boolean isReadyToPay(double price) {
	boolean isReadyToPay = ...; // su implementación aquí
	return isReadyToPay;
    }

    @Override
    public boolean executePayment(AID aid, double price) {
	boolean isPaymentSuccessful = ...; // su implementación del pago aquí
	return isPaymentSuccessful;
    }
}
```

La clase `ConsumerMockAgent` es un ejemplo tonto de un agente productor.

<a href='Hidden comment: 
Entonces, los agentes consumidor y productor, tienen que estar como está enseñado al dibujo siguiente:
[http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/Prod-Cons-Handlers-Agents-Cooperation-Class-Diagram.png]
'></a>

## Ejemplos ##

En la carpeta `examples` hay una demostración como se usa el protocolo `Productor-Consumidor`.

### Clases ###

#### Clases de Agentes Fingidos ####

En el paquete `agentes09.negotiation.ProdConsProtocol.mock.agents` están implementado algunos agentes fingidos:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-examples-mock-agents.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/diagrams/class/class-diagram-examples-mock-agents.png)

Aquí es la descripción breve de las clases de la diagrama:
  * `MockAgent` - un agente fingido que registra en `DFService`.
  * `ProducerMockAgent` - un agente productor fingido que crea y añade a su comportamientos el comportamiento del protocolo `ProducerBehaviour` y también implementa el interfaz `Producer`.
  * `ConsumerMockAgent` - un agente productor fingido que crea y añade a su comportamientos el comportamiento del protocolo `ConsumerBehaviour` y también implementa el interfaz `Consumer`.
  * `OnceBidConsumer` - un agente consumidor fingido que participa en la subasta, pero confirma solo en la primera iteración.
  * `TwiceBidsConsumer` - un agente consumidor fingido que participa en la subasta, pero confirma solo en las primeras dos iteraciones.
  * `ThreeBidsConsumer` - un agente consumidor fingido que participa en la subasta y confirma solo las primeras tres iteraciones de la subasta. Es una clase abstracta.
  * `ThreeBidsConsumerNotReadyToPay` - es un agente consumidor fingido del tipo `ThreeBidsConsumer`, que implementa el método `isReadyToPay(double)` cada vez a devuelve `false`. Eso causa terminación del protocolo y no se ejecuta el paso con el pago.
  * `ThreeBidsConsumerReadyToPayWithPaymentFailure` - es un agente consumidor fingido del tipo `ThreeBidsConsumer`, que implementa el método `isReadyToPay(double)` cada vez a devuelve `true` pero el método `executePayment(AID,double)` cada vez devuelve `false`. Eso dice que una falla se ocurre durante el pago del consumidor.
  * `ThreeBidsConsumerReadyToPayPaymentOK` - es un agente consumidor fingido del tipo `ThreeBidsConsumer`, que implementa los método `isReadyToPay(double)` y `executePayment(AID,double)` cada vez a devuelven `true`. Eso dice que la subasta tiene ganador y él compra la mercancía ganada con éxito.

#### Clases de Ejemplos ####

Los ejemplos están en el paquete `agentes09.negotiation.ProdConsProtocol.examples`. Las clases implementan algunos casos generales:
  * `ExampleProdConsNoConsumers` - en esta clase se enseña el caso cuando no hay consumidores. El productor justamente termina el comportamiento sin enviar ningún mensaje.
  * `ExampleProdConsWinnerRefuseToPay` - en esta clase se crean un productor y tres consumidores. Un consumidor es del tipo `OnceBidConsumer`, el segundo - del tipo `TwiceBidsConsumer` y el tercero - `ThreeBidsConsumerNotReadyToPay`. En este caso el protocolo termina sin compra (con mesaje `REFUSE`).
  * `ExampleProdConsPaymentFails` - en esta clase se crean un productor y tres consumidores. Un consumidor es del tipo `OnceBidConsumer`, el segundo - del tipo `TwiceBidsConsumer` y el tercero - `ThreeBidsConsumerReadyToPayWithPaymentFailure`. En este caso el protocolo termina sin compra (con mesaje `REFUSE`). Eso es una simulación que una falla se ocurre durante el pago del consumidor.
  * `ExampleProdConsOK` - en esta clase se crean un productor y tres consumidores. Un consumidor es del tipo `OnceBidConsumer`, el segundo - del tipo `TwiceBidsConsumer` y el tercero - `ThreeBidsConsumerReadyToPayPaymentOK`. En este caso el protocolo termina sin compra (con mesaje `REFUSE`). Eso es una simulación de terminación del protocolo con éxito, es decir que la subasta tiene ganador y él compra (es decir paga con éxito) la mercancía ganada.

Todas las clases tienen una inicialización de un agente del tipo `jade.tools.sniffer.Sniffer`, que intercepta la comunicación entre los productor y los consumidores y visualiza el cambio de los mensajes entre ellos.

### Salida de los Ejemplos ###

Aquí se está presentando la salida del `Sniffer` en cada uno de los ejemplos (sin lo primero).

#### Ejemplo - El Consumidor Rechaza el Pago ####

Eso está una salida después la ejecución de la clase `ExampleProdConsWinnerRefuseToPay`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-refuse.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-refuse.png)

Se ve que él último mensaje enviado del consumidor ganador es con performative `REFUSE`.

#### Ejemplo - El Pago del Consumidor Falla ####

Eso está una salida después la ejecución de la clase `ExampleProdConsPaymentFails`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-failure.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-failure.png)

Se ve que él último mensaje enviado del consumidor ganador es con performative `FAILURE`.

#### Ejemplo - La Subasta Termine con Éxito ####

Eso está una salida después la ejecución de la clase `ExampleProdConsOK`:

![http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-ok.png](http://fipa-english-auction-interaction-protocol.googlecode.com/svn/resources/examples-sinffer/example-prod-cons-interaction-ok.png)

Se ve que él último mensaje enviado del consumidor ganador es con performative `INFORM`, que significa que el pago está terminado con éxito.

# Trabajos Futuros #

El protocolo `Productor-Consumidor` se puede mejorar en la manera siguiente:
  * Se pueden refactorizar las nested classes del protocolo `FIPA English Auction IP` especialmente estos que preparan mensajes para enviar y estos que envian los mensajes preparados.
  * Se puede pensar como se pueden refactorizar las clases `EnglishAuctionInitiator` y `EnglishAuctionParticipant` para reutlizar la lógica implementada en `Jade`, es decir como se pueden heredar las clases `jade.proto.Initiator` y `jade.proto.Responder` correspondientemente por las clases `EnglishAuctionInitiator` y `EnglishAuctionParticipant`.