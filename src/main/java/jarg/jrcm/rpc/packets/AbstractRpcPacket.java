package jarg.jrcm.rpc.packets;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.rpc.exception.RpcDataSerializationException;
import jarg.jrcm.rpc.serialization.RpcDataSerializer;

/**
 * Abstract class for packets that encapsulate RPC messages.
 * An AbstractRpcPacket is associated with the {@link WorkRequestProxy}
 * that will be used by the application to transmit or read the AbstractRpcPacket's
 * data.
 */
public abstract class AbstractRpcPacket {

    protected WorkRequestProxy workRequestProxy;                // the RDMA WR associated with this packet

    public AbstractRpcPacket(WorkRequestProxy workRequestProxy) {
        this.workRequestProxy = workRequestProxy;
    }

    /**
     * Serializes the packet's data to the ByteBuffer of the WorkRequestProxy associated with
     * this packet.
     * Requires an {@link RpcDataSerializer} to serialize the payload.
     * @param payloadSerializer will serialize the packet's data to the associated WorkRequestProxy's
     *                          ByteBuffer.
     * @throws RpcDataSerializationException thrown if there is an error during serialization.
     */
    public abstract void writeToWorkRequestBuffer(RpcDataSerializer payloadSerializer)
            throws RpcDataSerializationException;

    /**
     * Deserializes the packet header data from the the ByteBuffer of the WorkRequestProxy associated with
     * this packet.
     * @throws RpcDataSerializationException thrown if there is an error during deserialization.
     */
    public abstract void readHeadersFromWorkRequestBuffer()
            throws RpcDataSerializationException;


    /* *********************************************************
     *   Getters/Setters
     ********************************************************* */

    public WorkRequestProxy getWorkRequestProxy() {
        return workRequestProxy;
    }
}
