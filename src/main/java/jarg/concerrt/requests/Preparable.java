package jarg.concerrt.requests;

/**
 * Defines a method that prepares an RDMA Work Request before
 * sending it to the Network Card for execution.
 */
public interface Preparable {
    /**
     * Prepare a Work Request before sending it to the Network
     * Card.
     */
    void prepareRequest();
}
