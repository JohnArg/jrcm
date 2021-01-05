package jarg.rdmarpc.discovery.service;

import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.rpc.AbstractDataSerializer;
import jarg.rdmarpc.rpc.RpcMessageType;
import jarg.rdmarpc.rpc.RpcPacket;
import jarg.rdmarpc.rpc.RpcPacketHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.TWO_SIDED_SEND_SIGNALED;


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
        RdmaCommunicator endpoint = receivedPacket.getWorkRequest().getEndpoint();
        RpcPacketHeaders receivedPacketHeaders = receivedPacket.getPacketHeaders();
        int invokedOperationType = receivedPacketHeaders.getOperationType();
        long invokedOperationId = receivedPacketHeaders.getOperationID();
        // Now get a new WR from the RDMA endpoint and create a new packet for it
        WorkRequestProxyProvider proxyProvider = endpoint.getWorkRequestProxyProvider();
        WorkRequestProxy workRequestProxy = proxyProvider.getPostSendRequestBlocking(TWO_SIDED_SEND_SIGNALED);
        RpcPacket packet = new RpcPacket(workRequestProxy);
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
            serializer.setWorkRequestData(workRequestProxy);
            serializer.writeToWorkRequestBuffer();
        }
        // Time to send across the network
        endpoint.postNetOperationToNIC(workRequestProxy);
    }
}
