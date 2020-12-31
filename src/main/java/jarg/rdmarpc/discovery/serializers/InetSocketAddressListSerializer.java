package jarg.rdmarpc.discovery.serializers;

import jarg.rdmarpc.rpc.AbstractDataSerializer;
import jarg.rdmarpc.rpc.RpcDataSerializer;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;
import jarg.rdmarpc.rdma.connections.WorkRequestData;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A (de)serializer of a List of {@link InetSocketAddress InetSocketAddresses} that
 * uses a Work Request buffer to write the data to or read the data from.
 */
public class InetSocketAddressListSerializer extends AbstractDataSerializer {

    private static final long serialVersionId = 1L;

    private List<InetSocketAddress> addresses;

    public InetSocketAddressListSerializer() {
        addresses = new ArrayList<>();
    }

    /**
     * Serializes a list of {@link InetSocketAddress InetSocketAddresses}
     * to a Work Request buffer.
     */
    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
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
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        // check the serial version id
        long receivedSerialVersionId = buffer.getLong();
        throwIfSerialVersionInvalid(serialVersionId, receivedSerialVersionId);
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

    public void setAddresses(Set<InetSocketAddress> addressesSet){
        addresses = new ArrayList<>(addressesSet);
    }

}
