package jarg.jrcm.networking.dependencies.netbuffers;

import com.ibm.disni.RdmaEndpoint;
import jarg.jrcm.networking.communicators.RdmaCommunicator;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

/**
 * Manages the network buffers that will contain data to be transmitter or received over
 * the RDMA network.
 */
public interface NetworkBufferManager {

    /**
     * Allocates network buffers that will be used during communications.
     */
    void allocateCommunicationBuffers();

    /**
     * <p>
     * Get the buffer that will be used for communications and will be registered to the RDMA NIC.
     * <b>Important!</b> This buffer must be a <b>direct ByteBuffer</b> in order to be allocated in off heap
     * memory and avoid Garbage Collection.
     * Only one ByteBuffer with large size should be registered for communications, as this is more efficient,
     * than registering multiple smaller ones. Then the large ByteBuffer can be split into smaller
     * slices (or smaller parts) with {@link ByteBuffer#slice()}, that will hold data to be sent or received.
     * These small slices can be used as they were separate communication buffers.
     * </p>
     *
     * <p>
     * Allocating a large piece of memory for communications instead of multiple small ones and
     * registering it to the NIC, is a common technique for more efficient utilization of RDMA NICs.
     * The user application can use smaller pieces of the large allocated memory as the communication
     * buffers.
     * This helps save space in Memory Translation and Memory Protection
     * tables used by the RDMA NIC. The entries of these tables are stored in the NIC's
     * onboard cache too, so the fewer the entries, the more data can the NIC's cache store,
     * leading to more scalable performance.
     * </p>
     *
     * <p>
     * jRCM is built on top of DiSNI, in which memory registration for RDMA communications happens with
     * {@link RdmaEndpoint#registerMemory(ByteBuffer)}.
     * So any {@link RdmaCommunicator} that extends DiSNI's {@link RdmaEndpoint} has to use this method too
     * for memory registrations.
     * Thus, the NetworkBufferManager offers the getBufferToRegister() method, to provide an RdmaCommunicator
     * with access to the ByteBuffer to register to the RDMA NIC.
     * </p>
     */
    ByteBuffer getBufferToRegister();

    /**
     * Get the ByteBuffer associated with the Work Request that has the provided type and
     * id.
     * @param requestType the type of the Work Request.
     * @param workRequestId the id of the Work Request.
     * @return the ByteBuffer associated with the Work Request or null if it doesn't exist.
     */
    ByteBuffer getWorkRequestBuffer(WorkRequestType requestType, int workRequestId);


    /**
     * Get the address of the ByteBuffer that is associated with a Work Request that has
     * the provided type and id.
     * @param requestType the type of the Work Request.
     * @param workRequestId the id of the Work Request.
     * @return the address of the ByteBuffer associated with the Work Request or -1 if it
     * doesn't exist.
     */
    long getWorkRequestBufferAddress(WorkRequestType requestType, int workRequestId);
}
