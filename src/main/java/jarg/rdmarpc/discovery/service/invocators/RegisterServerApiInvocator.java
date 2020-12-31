package jarg.rdmarpc.discovery.service.invocators;

import jarg.rdmarpc.rpc.SendResponseTask;
import jarg.rdmarpc.rpc.AbstractThreadPoolInvocator;
import jarg.rdmarpc.rpc.RpcPacket;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;
import jarg.rdmarpc.discovery.RdmaDiscoveryApi;
import jarg.rdmarpc.discovery.serializers.InetSocketAddressListSerializer;
import jarg.rdmarpc.rdma.connections.WorkRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Reads information from an {@link RpcPacket}, deserializes parameters and invokes
 * {@link RdmaDiscoveryApi#registerServer(InetSocketAddress)}. It then sends back a response to
 * the caller.
 */
public class RegisterServerApiInvocator extends AbstractThreadPoolInvocator {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServerApiInvocator.class);

    private RdmaDiscoveryApi serviceApi;

    public RegisterServerApiInvocator(ExecutorService workersExecutor, RdmaDiscoveryApi serviceApi) {
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
            Set<InetSocketAddress> previousMembers = serviceApi.registerServer(addresses.get(0));
            // pass the response to the serializer
            serializer.setAddresses(previousMembers);
            // send the response to the caller in another task
            SendResponseTask responseTask = new SendResponseTask(packet, serializer, false);
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
