package jarg.rdmarpc.rpc.request;

/**
 * Generates unique request Ids, in order to associate requests
 * with responses.
 */
public interface RequestIdGenerator<T> {

    /**
     * Retrieve a request id that will associate an RPC request
     * with a response.
     * @return the generated id.
     */
    T generateRequestId();
}
