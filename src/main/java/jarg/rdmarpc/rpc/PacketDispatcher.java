package jarg.rdmarpc.rpc;

/**
 * Dispatches an {@link RpcPacket} to the appropriate handler.
 */
public interface PacketDispatcher {

    /**
     * Uses an {@link RpcPacket RpcPacket's} headers to figure out which
     * handler object to call to deal with the packet.
     * @param packet the packet to manage.
     */
    void dispatchPacket(RpcPacket packet);
}
