package jarg.rdmarpc.discovery.service.invocators;

import jarg.rdmarpc.discovery.RdmaDiscoveryApi;
import jarg.rdmarpc.discovery.serializers.InetSocketAddressListSerializer;
import jarg.rdmarpc.discovery.serializers.IntSerializer;
import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rpc.AbstractThreadPoolInvocator;
import jarg.rdmarpc.rpc.RpcPacket;
import jarg.rdmarpc.rpc.SendResponseTask;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Reads information from an {@link RpcPacket}, deserializes parameters and invokes
 * {@link RdmaDiscoveryApi#getServerPortByIp(InetAddress)}. It then sends back a response to
 * the caller.
 */
public class GetServerPortByIpApiInvocator extends AbstractThreadPoolInvocator {

    private static final Logger logger = LoggerFactory.getLogger(GetServerPortByIpApiInvocator.class);

    private RdmaDiscoveryApi serviceApi;

    public GetServerPortByIpApiInvocator(ExecutorService workersExecutor, RdmaDiscoveryApi serviceApi) {
        super(workersExecutor);
        this.serviceApi = serviceApi;
    }

    @Override
    public void invokeOperationTask(RpcPacket packet) {
        // Pass the packet's work request data to the serializer
        WorkRequestData workRequestData = packet.getWorkRequest();
        InetSocketAddressListSerializer serializer = new InetSocketAddressListSerializer();
        serializer.setWorkRequestData(workRequestData);

        try {
            // deserialize request parameters from the received packet
            serializer.readFromWorkRequestBuffer();
            List<InetSocketAddress> addresses = serializer.getAddresses();
            // Free WR id, we have the objects we need
            workRequestData.getEndpoint().freeUpWrID(workRequestData);
            // invoke the service's API
            int port = serviceApi.getServerPortByIp(addresses.get(0).getAddress());
            // get a serializer for the response and set the response to it
            IntSerializer responseSerializer = new IntSerializer();
            responseSerializer.setValue(port);
            // send the response to the caller in another task
            SendResponseTask responseTask = new SendResponseTask(packet, responseSerializer, false);
            getWorkersExecutor().submit(responseTask);
        } catch (RpcDataSerializationException e) {
            // Free WR id
            workRequestData.getEndpoint().freeUpWrID(workRequestData);
            // send the response to the caller in another task
            SendResponseTask responseTask = new SendResponseTask(packet, null, true);
            getWorkersExecutor().submit(responseTask);
            logger.error("Unable to invoke service API", e);
        }
    }
}
