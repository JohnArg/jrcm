package jarg.rdmarpc.discovery.serializers;

import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rpc.AbstractDataSerializer;
import jarg.rdmarpc.rpc.RpcDataSerializer;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * A (de)serializer of booleans that
 * uses a Work Request buffer to write the data to or read the data from.
 */
public class BooleanSerializer extends AbstractDataSerializer {

    private static final long serialVersionId = 1L;

    private boolean flag;

    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        buffer.putLong(serialVersionId);
        if (flag) {
            buffer.put((byte) 1);
        } else {
            buffer.put((byte) 0);
        }
    }

    @Override
    public void readFromWorkRequestBuffer() throws RpcDataSerializationException {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        // check the serial version id
        long receivedSerialVersionId = buffer.getLong();
        throwIfSerialVersionInvalid(serialVersionId, receivedSerialVersionId);
        byte value = buffer.get();
        flag = (value == 1);
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
