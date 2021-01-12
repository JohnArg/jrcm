package jarg.rdmarpc.rpc.packets;

import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;
import jarg.rdmarpc.rpc.serialization.RpcDataSerializer;

/**
 * Abstract class for packets that encapsulate RPC messages.
 */
public abstract class AbstractRpcPacket {

    private WorkRequestProxy workRequestProxy;                // the RDMA WR associated with this packet

    public AbstractRpcPacket(WorkRequestProxy workRequestProxy) {
        this.workRequestProxy = workRequestProxy;
    }

    /**
     * Writes the packet's information to the network request's buffer.
     * Requires a payload serializer to be provided.
     * @param payloadSerializer will serialize payload data to the network request's buffer.
     * @throws RpcDataSerializationException thrown if there is an error during serialization.
     */
    public abstract void writeToWorkRequestBuffer(RpcDataSerializer payloadSerializer)
            throws RpcDataSerializationException;

    /**
     * Reads the packet header data from the network request buffer into this packet.
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
