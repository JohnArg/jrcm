package jarg.rdmarpc.server.discovery;

import jarg.rdmarpc.server.rpc.*;
import jarg.rdmarpc.server.rpc.exception.RpcDataSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with {@link RpcPacket RpcPackets} received by the {@link RdmaDiscoveryService}.
 */
public class DiscoveryPacketDispatcher implements PacketDispatcher {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryPacketDispatcher.class);

    // These deserializers correspond to the RdmaDiscoveryAPI methods
    // There is one for each type of method and they will be injected in the constructor
    private RpcOperationInvocator registerServerApiInvocator;
    private RpcOperationInvocator unregisterServerApiInvocator;
    private RpcOperationInvocator getRegisteredServersApiInvocator;
    private RpcOperationInvocator getServerPortByIpApiInvocator;

    public DiscoveryPacketDispatcher(RpcOperationInvocator registerServerApiInvocator,
                                     RpcOperationInvocator unregisterServerApiInvocator,
                                     RpcOperationInvocator getRegisteredServersApiInvocator,
                                     RpcOperationInvocator getServerPortByIpApiInvocator) {
        this.registerServerApiInvocator = registerServerApiInvocator;
        this.unregisterServerApiInvocator = unregisterServerApiInvocator;
        this.getRegisteredServersApiInvocator = getRegisteredServersApiInvocator;
        this.getServerPortByIpApiInvocator = getServerPortByIpApiInvocator;
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
