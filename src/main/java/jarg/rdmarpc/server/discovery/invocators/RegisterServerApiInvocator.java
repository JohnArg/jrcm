package jarg.rdmarpc.server.discovery.invocators;

import jarg.rdmarpc.server.discovery.invocators.serializers.InetSocketAddressListSerializer;
import jarg.rdmarpc.server.rdma.connections.WorkRequestData;
import jarg.rdmarpc.server.rpc.RpcMessageType;
import jarg.rdmarpc.server.rpc.RpcOperationInvocator;
import jarg.rdmarpc.server.rpc.RpcPacket;
import jarg.rdmarpc.server.rpc.exception.RpcDataSerializationException;

public class RegisterServerApiInvocator implements RpcOperationInvocator {

    private InetSocketAddressListSerializer serializer;

    public RegisterServerApiInvocator() {
        serializer = new InetSocketAddressListSerializer();
    }

    @Override
    public void invokeOperation(RpcPacket packet) {
        WorkRequestData workRequestData = packet.getWorkRequest();
        serializer.setWorkRequestData(workRequestData);

        if(packet.getPacketHeaders().getMessageType() == RpcMessageType.REQUEST){
            serializer.writeToWorkRequestBuffer();
            // Todo - free WR id, we have the objects we need
            // Todo - manage request
        }else if (packet.getPacketHeaders().getMessageType() == RpcMessageType.RESPONSE){
            try {
                serializer.readFromWorkRequestBuffer();
                // Todo - free WR id, we have the objects we need
                // Todo - manage request
            } catch (RpcDataSerializationException e) {
                // Todo - free WR id, we have the objects we need
                // Todo - manage error
                e.printStackTrace();
            }
        }

    }
}
