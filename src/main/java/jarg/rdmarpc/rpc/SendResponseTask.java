package jarg.rdmarpc.rpc;

import jarg.rdmarpc.rdma.connections.RdmaRpcEndpoint;
import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rdma.netrequests.WorkRequestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SendResponseTask implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(SendResponseTask.class);

    private RpcPacket receivedPacket;
    private AbstractDataSerializer serializer;
    private boolean isErrorResponse;

    public SendResponseTask(RpcPacket receivedPacket, AbstractDataSerializer serializer, boolean isErrorResponse) {
        this.receivedPacket = receivedPacket;
        this.serializer = serializer;
        this.isErrorResponse = isErrorResponse;
    }

    @Override
    public void run() {
        // First, get the necessary information from the received packet
        RdmaRpcEndpoint endpoint = receivedPacket.getWorkRequest().getEndpoint();
        RpcPacketHeaders receivedPacketHeaders = receivedPacket.getPacketHeaders();
        int invokedOperationType = receivedPacketHeaders.getOperationType();
        long invokedOperationId = receivedPacketHeaders.getOperationID();
        // Now get a new WR from the RDMA endpoint and create a new packet for it
        WorkRequestData workRequestData = endpoint.getWorkRequestBlocking();
        RpcPacket packet = new RpcPacket(workRequestData);
        RpcPacketHeaders packetHeaders = packet.getPacketHeaders();
        // Prepare new packet headers
        if(isErrorResponse){
            packetHeaders.setMessageType(RpcMessageType.ERROR);
        }else {
            packetHeaders.setMessageType(RpcMessageType.RESPONSE);
        }
        packetHeaders.setOperationType(invokedOperationType)
                .setOperationID(invokedOperationId)
                .setPacketNumber(0);
        // Serialize the packet headers
        packetHeaders.writeToWorkRequestBuffer();
        // Serialize any payload if necessary
        if((!isErrorResponse) && (serializer != null)){
            /* serialize response to WR buffer. The response was already set to the
            serializer before calling this task. */
            serializer.setWorkRequestData(workRequestData);
            serializer.writeToWorkRequestBuffer();
        }
        // Time to send across the network
        try {
            endpoint.send(workRequestData.getId(), workRequestData.getBuffer().limit(),
                    WorkRequestTypes.TWO_SIDED_SIGNALED);
        } catch (IOException e) {
           logger.error("Cannot send response.", e);
        }
    }
}
