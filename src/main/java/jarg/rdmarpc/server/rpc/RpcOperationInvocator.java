package jarg.rdmarpc.server.rpc;

/**
 * Extracts parameters of an RPC operation (request or response) from an {@link RpcPacket} and
 * invokes that operation.
 */
public interface RpcOperationInvocator {

    /**
     * Invoke an RPC operation, passing it the appropriate parameters.
     */
    void invokeOperation(RpcPacket packet);
}
