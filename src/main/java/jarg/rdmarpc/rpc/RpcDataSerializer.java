package jarg.rdmarpc.rpc;

import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;
import jarg.rdmarpc.rdma.connections.WorkRequestData;

/**
 * (De)Serializes data that will be sent/received.
 */
public interface RpcDataSerializer {

    /**
     * Serialize this object to the associated Work Request's data buffer.
     */
    void writeToWorkRequestBuffer();

    /**
     * Deserialize this object from the associated Work Request's data buffer.
     * @throws RpcDataSerializationException if the data could be deserialized
     * successfully.
     */
    void readFromWorkRequestBuffer() throws RpcDataSerializationException;
}
