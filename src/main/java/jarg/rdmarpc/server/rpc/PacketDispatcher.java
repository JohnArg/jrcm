package jarg.rdmarpc.server.rpc;

/**
 * Dispatches an {@link RpcPacket} to the appropriate handler that
 * will deserialize its payload.
 */
public interface PacketDispatcher {

    /**
     * Uses an {@link RpcPacket RpcPacket's} headers to figure out which
     * handler object to call to deserialize the payload.
     * @param packet the packet to manage.
     */
    void dispatchPacket(RpcPacket packet);
}
