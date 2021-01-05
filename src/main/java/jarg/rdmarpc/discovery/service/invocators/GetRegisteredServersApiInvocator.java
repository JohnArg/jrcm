package jarg.rdmarpc.discovery.service.invocators;

import jarg.rdmarpc.discovery.serializers.InetSocketAddressListSerializer;
import jarg.rdmarpc.rpc.AbstractThreadPoolInvocator;
import jarg.rdmarpc.discovery.RdmaDiscoveryApi;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.rpc.RpcPacket;
import jarg.rdmarpc.discovery.service.SendResponseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Reads information from an {@link RpcPacket}, deserializes parameters and invokes
 * {@link RdmaDiscoveryApi#getRegisteredServers()}. It then sends back a response to
 * the caller.
 */
public class GetRegisteredServersApiInvocator extends AbstractThreadPoolInvocator {

    private static final Logger logger = LoggerFactory.getLogger(GetRegisteredServersApiInvocator.class);

    private RdmaDiscoveryApi serviceApi;

    public GetRegisteredServersApiInvocator(ExecutorService workersExecutor, RdmaDiscoveryApi serviceApi) {
        super(workersExecutor);
        this.serviceApi = serviceApi;
    }

    @Override
    public void invokeOperationTask(RpcPacket packet) {
        // Get the packet's work request data
        WorkRequestProxy workRequestData = packet.getWorkRequest();
        // Free WR id, we have the objects we need
        workRequestData.getEndpoint().getWorkRequestProxyProvider().releaseWorkRequest(workRequestData);
        // invoke the service's API
        Set<InetSocketAddress> previousMembers = serviceApi.getRegisteredServers();
        // get a serializer for the response and set the response to it
        InetSocketAddressListSerializer responseSerializer = new InetSocketAddressListSerializer();
        responseSerializer.setAddresses(previousMembers);
        // send the response to the caller in another task
        SendResponseTask responseTask = new SendResponseTask(packet, responseSerializer, false);
        getWorkersExecutor().submit(responseTask);
    }
}
