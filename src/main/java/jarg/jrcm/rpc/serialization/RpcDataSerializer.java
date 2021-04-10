package jarg.jrcm.rpc.serialization;

import jarg.jrcm.rpc.exception.RpcDataSerializationException;

/**
 * (De)Serializes data that will be sent/received.
 */
public interface RpcDataSerializer {

    /**
     * Serialize this object to the associated Work Request's data buffer.
     * @throws RpcDataSerializationException if the data could not be serialized
     * successfully.
     */
    void writeToWorkRequestBuffer() throws RpcDataSerializationException;

    /**
     * Deserialize this object from the associated Work Request's data buffer.
     * @throws RpcDataSerializationException if the data could not be deserialized
     * successfully.
     */
    void readFromWorkRequestBuffer() throws RpcDataSerializationException;
}
