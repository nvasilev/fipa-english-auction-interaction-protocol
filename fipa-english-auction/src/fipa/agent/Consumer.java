package fipa.agent;

import jade.core.AID;

/**
 * Fictive interface, created with only testing purpose. It is going to exist
 * until real interface is supported by the responsible team.
 * 
 * @author Nikolay Vasilev
 */
public interface Consumer {

    /**
     * Returns <code>true</code> if the price offered by the producer for
     * selling the stock is accepted by the consumer and <code>false</code>
     * otherwise.
     * 
     * @return Returns <code>true</code> if the price offered by the producer
     *         for selling the stock is accepted by the consumer and
     *         <code>false</code> otherwise.
     */
    boolean isPriceAcceptable(double offeredPrice);

    /**
     * Checks if the consumer is ready to pay the asked price.
     * 
     * @param price
     *            The price that is expected to be paid.
     * @return Returns <code>true</code> if the consumer is ready to pay the
     *         price, and <code>false</code> otherwise.
     */
    boolean isReadyToPay(double price);

    /**
     * Executes the payment to the producer.
     * 
     * @param aid
     *            The AID of the producer to whom this consumer is goign to pay.
     * @param price
     *            The price which must be paid to the producer.
     * @return Returns <code>true</code> if the payment finished successfully
     *         and <code>false</code> otherwise.
     */
    boolean executePayment(AID aid, double price);
}
