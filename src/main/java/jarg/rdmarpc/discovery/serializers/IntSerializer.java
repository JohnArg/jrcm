package jarg.rdmarpc.discovery.serializers;

import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rpc.AbstractDataSerializer;
import jarg.rdmarpc.rpc.RpcDataSerializer;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * A (de)serializer of integers that
 * uses a Work Request buffer to write the data to or read the data from.
 */
public class IntSerializer extends AbstractDataSerializer {

    private static final long serialVersionId = 1L;

    private int value;

    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        buffer.putLong(serialVersionId);
        buffer.putInt(value);
    }

    @Override
    public void readFromWorkRequestBuffer() throws RpcDataSerializationException {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        // check the serial version id
        long receivedSerialVersionId = buffer.getLong();
        throwIfSerialVersionInvalid(serialVersionId, receivedSerialVersionId);
        value = buffer.getInt();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
