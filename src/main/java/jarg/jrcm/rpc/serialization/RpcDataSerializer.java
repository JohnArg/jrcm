package jarg.jrcm.rpc.serialization;

import jarg.jrcm.rpc.exception.RpcDataSerializationException;

/**
 * Serializes data to be sent or deserializes received data.
 */
public interface RpcDataSerializer {

    /**
     * Serializes this object to a ByteBuffer.
     * @throws RpcDataSerializationException if the data could not be serialized
     * successfully.
     */
    void writeToWorkRequestBuffer() throws RpcDataSerializationException;

    /**
     * Deserializes this object from a ByteBuffer.
     * @throws RpcDataSerializationException if the data could not be deserialized
     * successfully.
     */
    void readFromWorkRequestBuffer() throws RpcDataSerializationException;
}
