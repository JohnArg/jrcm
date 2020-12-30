package jarg.rdmarpc.server.rpc;

import jarg.rdmarpc.server.rdma.connections.WorkRequestData;
import jarg.rdmarpc.server.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * (De)Serializes data that will be sent/received.
 */
public interface RpcDataSerializer {

    /**
     * Get the RDMA Work Request data associated with this serializer.
     * @return the Work Request data
     */
    WorkRequestData getWorkRequestData();

    /**
     * Pass a {@link WorkRequestData} to this serializer.
     * @param workRequestData the data to pass in.
     */
    void setWorkRequestData(WorkRequestData workRequestData);

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
