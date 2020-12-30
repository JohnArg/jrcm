package jarg.rdmarpc.server.discovery.invocators.serializers;

import jarg.rdmarpc.server.rdma.connections.WorkRequestData;
import jarg.rdmarpc.server.rpc.RpcDataSerializer;
import jarg.rdmarpc.server.rpc.exception.RpcDataSerializationException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A (de)serializer of a List of {@link InetSocketAddress InetSocketAddresses} that
 * uses a Work Request buffer to write the data to or read the data from.
 */
public class InetSocketAddressListSerializer implements RpcDataSerializer {

    private static final long serialVersionId = 1L;

    private List<InetSocketAddress> addresses;
    private WorkRequestData workRequestData;

    public InetSocketAddressListSerializer() {
        addresses = new ArrayList<>();
    }

    @Override
    public WorkRequestData getWorkRequestData() {
        return workRequestData;
    }

    @Override
    public void setWorkRequestData(WorkRequestData workRequestData) {
        this.workRequestData = workRequestData;
    }

    /**
     * Serializes a list of {@link InetSocketAddress InetSocketAddresses}
     * to a Work Request buffer.
     */
    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = workRequestData.getBuffer();
        // first write the serial version
        buffer.putLong(serialVersionId);
        // then write the list's size
        buffer.putInt(addresses.size());
        // then for every address, put the ip bytes and port
        for(InetSocketAddress address : addresses){
            byte[] addressBytes = address.getAddress().getAddress();
            // specify the number of bytes of the address
            buffer.putInt(addressBytes.length);
            // now put the address bytes
            buffer.put(addressBytes);
            // and finally put the port number
            buffer.putInt(address.getPort());
        }
    }

    /**
     * Deserializes a list of {@link InetSocketAddress InetSocketAddresses}
     * from a Work Request buffer.
     * @throws RpcDataSerializationException
     */
    @Override
    public void readFromWorkRequestBuffer() throws RpcDataSerializationException {
        ByteBuffer buffer = workRequestData.getBuffer();
        // check the serial version id
        long receivedSerialVersionId = buffer.getLong();
        if(receivedSerialVersionId != serialVersionId){
            throw new RpcDataSerializationException("Serial versions do not match. Local version : "+
                    serialVersionId + ", remote version : " + receivedSerialVersionId + ".");
        }
        // read a list of addresses from the buffer
        int listSize = buffer.getInt();
        int addressBytesSize;

        for(int i=0; i<listSize; i++){
            // read the address bytes
            addressBytesSize = buffer.getInt();
            byte[] addressBytes = new byte[addressBytesSize];
            buffer.get(addressBytes);
            try {
                // create a new InetAddress from the bytes
                InetAddress ipAddress = InetAddress.getByAddress(addressBytes);
                // get the port too
                int port = buffer.getInt();
                // add the new address
                addresses.add(new InetSocketAddress(ipAddress, port));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    /* *********************************************************
     *   Getters/Setters
     ********************************************************* */

    public List<InetSocketAddress> getAddresses() {
        return addresses;
    }
}
