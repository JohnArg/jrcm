package jarg.jrcm.rpc.invocation;

import jarg.jrcm.rpc.packets.AbstractRpcPacket;

/**
 * Extracts parameters of an RPC operation (request or response) from an {@link AbstractRpcPacket} and
 * invokes that operation.
 */
public interface RpcOperationInvocator {

    /**
     * Invoke an RPC operation, passing it the appropriate parameters.
     */
    void invokeOperation(AbstractRpcPacket packet);
}
