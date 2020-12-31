package jarg.rdmarpc.rpc;


import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * Encapsulates RPC messages.
 */
public class RpcPacket {

    private RpcPacketHeaders packetHeaders;             // the headers of the packet
    private WorkRequestData workRequest;                // the RDMA WR associated with this packet


    public RpcPacket(WorkRequestData workRequest){
        this.packetHeaders = new RpcPacketHeaders();
        this.workRequest = workRequest;
        this.packetHeaders.setWorkRequestData(workRequest);
    }

    public RpcPacket(RpcPacketHeaders packetHeaders, WorkRequestData workRequest){
        this.packetHeaders = packetHeaders;
        this.workRequest = workRequest;
        this.packetHeaders.setWorkRequestData(workRequest);
    }

    /* *********************************************************
     *   Write/Read packet data to/from a buffer.
     ********************************************************* */

    /**
     * Writes the packet's information to the network request's buffer.
     * Requires a payload serializer to be provided.
     * @param payloadSerializer will serialize payload data to the network request's buffer.
     */
    public void writeToWorkRequestBuffer(RpcDataSerializer payloadSerializer){
        ByteBuffer packetBuffer = workRequest.getBuffer();
        // write the headers first
        packetHeaders.writeToWorkRequestBuffer();
        // write the payload next
        payloadSerializer.writeToWorkRequestBuffer();
        // prepare buffer for reading
        packetBuffer.flip();
    }

    /**
     * Reads the packet header data from the network request buffer into this packet.
     * Then it invokes a handler object that will decide what to do with this packet.
     */
    public void readHeadersFromBufferAndDispatch(PacketDispatcher dispatcher)
                                                throws RpcDataSerializationException {
        packetHeaders.readFromWorkRequestBuffer();
        dispatcher.dispatchPacket(this);
    }

    /* *********************************************************
     *   Getters/Setters
     ********************************************************* */

    public WorkRequestData getWorkRequest() {
        return workRequest;
    }

    public RpcPacketHeaders getPacketHeaders() {
        return packetHeaders;
    }
}
