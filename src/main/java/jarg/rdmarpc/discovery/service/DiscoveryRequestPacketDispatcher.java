package jarg.rdmarpc.discovery.service;

import jarg.rdmarpc.discovery.DiscoveryOperationType;
import jarg.rdmarpc.discovery.RdmaDiscoveryApi;
import jarg.rdmarpc.discovery.service.invocators.RegisterServerApiInvocator;
import jarg.rdmarpc.discovery.service.invocators.UnregisterServerApiInvocator;
import jarg.rdmarpc.rpc.PacketDispatcher;
import jarg.rdmarpc.rpc.RpcOperationInvocator;
import jarg.rdmarpc.rpc.RpcPacket;
import jarg.rdmarpc.discovery.service.invocators.GetRegisteredServersApiInvocator;
import jarg.rdmarpc.discovery.service.invocators.GetServerPortByIpApiInvocator;
import jarg.rdmarpc.rpc.RpcPacketHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Used after deserializing the headers of a received ${@link RpcPacket}.
 * It reads those headers to identify the type of received request and call the appropriate
 * ${@link RpcOperationInvocator}.
 * Then, the invocator will call the service's API that corresponds to the received request.
 */
public class DiscoveryRequestPacketDispatcher implements PacketDispatcher {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryRequestPacketDispatcher.class);

    private RpcOperationInvocator registerServerApiInvocator;
    private RpcOperationInvocator unregisterServerApiInvocator;
    private RpcOperationInvocator getRegisteredServersApiInvocator;
    private RpcOperationInvocator getServerPortByIpApiInvocator;

    public DiscoveryRequestPacketDispatcher(RdmaDiscoveryApi rdmaDiscoveryApi, ExecutorService workersExecutor) {
        this.registerServerApiInvocator = new RegisterServerApiInvocator(workersExecutor, rdmaDiscoveryApi);
        this.unregisterServerApiInvocator = new UnregisterServerApiInvocator(workersExecutor, rdmaDiscoveryApi);
        this.getRegisteredServersApiInvocator = new GetRegisteredServersApiInvocator(workersExecutor, rdmaDiscoveryApi);
        this.getServerPortByIpApiInvocator = new GetServerPortByIpApiInvocator(workersExecutor, rdmaDiscoveryApi);
    }

    @Override
    public void dispatchPacket(RpcPacket packet) {
        RpcPacketHeaders headers = packet.getPacketHeaders();
        switch(headers.getOperationType()){
            case DiscoveryOperationType.REGISTER_SERVER:
                registerServerApiInvocator.invokeOperation(packet);
                break;
            case DiscoveryOperationType.UNREGISTER_SERVER:
                unregisterServerApiInvocator.invokeOperation(packet);
                break;
            case DiscoveryOperationType.GET_SERVERS:
                getRegisteredServersApiInvocator.invokeOperation(packet);
                break;
            case DiscoveryOperationType.GET_SERVER_PORT:
                getServerPortByIpApiInvocator.invokeOperation(packet);
                break;
            default:
        }
    }
}
