package jarg.jrcm.rpc.request;

/**
 * Generates unique request ids, in order to associate requests
 * with responses.
 */
@FunctionalInterface
public interface RequestIdGenerator<T> {

    /**
     * Retrieve a request id that will associate an RPC request
     * with a response.
     * @return the generated id.
     */
    T generateRequestId();
}
