package jarg.jrcm.networking.dependencies.netbuffers;

import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

/**
 * Manages the buffers that will contain data to be transmitter or received over
 * the RDMA network.
 */
public interface NetworkBufferManager {

    /**
     * Allocates buffers that will be used during communications.
     */
    void allocateCommunicationBuffers();

    /**
     * <p>
     * Get the buffer that will be used for communications and will be registered to the NIC.
     * It is more efficient to allocate a large buffer for communications and register this
     * to the NIC, than multiple smaller ones. Then we can split the large buffer into smaller
     * slices (or smaller parts), that will hold data to be sent or received. These small
     * slices will be used as they were separate communication buffers.
     * </p>
     * <p>
     * This saves space in Memory Translation and Memory Protection
     * tables used by the Network Card. The entries of these tables are stored in an
     * onboard NIC cache too, so the fewer the entries of the aforementioned tables,
     * the more data can the NIC's cache store, leading to more scalable performance.
     * </p>
     */
    ByteBuffer getBufferToRegister();

    /**
     * Get the buffer associated with the Work Request that has the provided type and
     * id.
     * @param requestType the type of the Work Request.
     * @param workRequestId the id of the Work Request.
     * @return the buffer associated with the Work Request or null if it doesn't exist.
     */
    ByteBuffer getWorkRequestBuffer(WorkRequestType requestType, int workRequestId);


    /**
     * Get the address of the buffer, that is associated with the Work Request that has
     * the provided type and id.
     * @param requestType the type of the Work Request.
     * @param workRequestId the id of the Work Request.
     * @return the address of the buffer associated with the Work Request or -1 if it
     * doesn't exist.
     */
    long getWorkRequestBufferAddress(WorkRequestType requestType, int workRequestId);
}
