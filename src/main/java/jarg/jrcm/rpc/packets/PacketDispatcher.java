package jarg.jrcm.rpc.packets;

/**
 * Dispatches an {@link AbstractRpcPacket} to the appropriate handler.
 */
@FunctionalInterface
public interface PacketDispatcher<T extends AbstractRpcPacket> {

    /**
     * Uses an {@link AbstractRpcPacket AbstractRpcPacket's} headers to figure out which
     * handler object to call, that will deal with the packet.
     * @param packet the packet to manage.
     */
    void dispatchPacket(T packet);
}
