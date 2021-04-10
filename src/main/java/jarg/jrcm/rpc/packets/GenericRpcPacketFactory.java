package jarg.jrcm.rpc.packets;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;

/**
 * A generic factory for RPC packets.
 * @param <P> the type of the packet.
 * @param <M> the type of the message type (could be request, response or error)
 * @param <O> the type of the RPC method that the packet is created for.
 */
@FunctionalInterface
public interface GenericRpcPacketFactory <P, M, O>{

    /**
     * Generates an RPC packet.
     * @param workRequestProxy associates a packet with a {@link WorkRequestProxy} and hence, with a
     *                         network buffer that will contain its data.
     * @param messageType the type of the message (could be request, response or error).
     * @param operationType determines the type of the RPC method that the packet is created for.
     * @return the generated RPC packet.
     */
    P generatePacket(WorkRequestProxy workRequestProxy, M messageType, O operationType);
}
