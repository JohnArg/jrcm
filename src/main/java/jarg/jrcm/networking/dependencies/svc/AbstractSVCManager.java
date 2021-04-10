package jarg.jrcm.networking.dependencies.svc;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.verbs.IbvMr;
import jarg.jrcm.networking.dependencies.netbuffers.NetworkBufferManager;

/**
 * Contains common fields and methods for classes implementing {@link SVCManager}.
 */
public abstract class AbstractSVCManager implements SVCManager {

    private int maxBufferSize;                          // the maximum size of communication buffers
    private int maxWorkRequests;                        // the maximum number of work requests for
                                                            // either postSend or postRecv (same number)
    // dependencies ----------------------------------
    private RdmaEndpoint rdmaEndpoint;
    private NetworkBufferManager bufferManager;
    private IbvMr registeredMemoryRegion;               // This implementation expects one large memory
                                                            // region to be registered to the NIC

    public AbstractSVCManager(int maxBufferSize, int maxWorkRequests) {
        this.maxBufferSize = maxBufferSize;
        this.maxWorkRequests = maxWorkRequests;
    }


    /* ***************************************************************
     *   Getters/Setters
     * ***************************************************************/

    public RdmaEndpoint getRdmaEndpoint() {
        return rdmaEndpoint;
    }

    public void setRdmaEndpoint(RdmaEndpoint rdmaEndpoint) {
        this.rdmaEndpoint = rdmaEndpoint;
    }

    public NetworkBufferManager getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(NetworkBufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public IbvMr getRegisteredMemoryRegion() {
        return registeredMemoryRegion;
    }

    public void setRegisteredMemoryRegion(IbvMr registeredMemoryRegion) {
        this.registeredMemoryRegion = registeredMemoryRegion;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxWorkRequests() {
        return maxWorkRequests;
    }

    public void setMaxWorkRequests(int maxWorkRequests) {
        this.maxWorkRequests = maxWorkRequests;
    }
}
