package jarg.jrcm.rpc.packets;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;

/**
 * A generic factory for RPC packets.
 * @param <P> the type of the packet.
 * @param <M> the message type (e.g. request, response, error)
 * @param <O> the type of the operation to perform - identifies RPC function from the API
 */
@FunctionalInterface
public interface GenericRpcPacketFactory <P, M, O>{

    /**
     * Generates an RPC packet.
     * @param workRequestProxy associates a packet with a Work Request to the NIC and hence, with a
     *                         network buffer that will contain its data.
     * @param messageType the type of the message (e.g. request, response, error).
     * @param operationType what RPC function is this packet for?
     * @return the generated RPC packet.
     */
    P generatePacket(WorkRequestProxy workRequestProxy, M messageType, O operationType);
}
