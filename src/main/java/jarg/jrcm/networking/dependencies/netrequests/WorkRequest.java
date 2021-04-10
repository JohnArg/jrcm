package jarg.jrcm.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSge;

import java.util.LinkedList;

/**
 * Abstract class representing an RDMA Work Request (WR), that will be
 * posted to the RDMA NIC and will be executed by it. WRs contain
 * information that describe to the RDMA NIC the RDMA operation it has to execute.
 * Implementations of this class are meant to be used by jRCM internally only.
 * Applications can use {@link WorkRequestProxy} objects instead.
 */
public abstract class WorkRequest{
    private IbvMr requestBufferMR;       // the memory region registered to the NIC
    private IbvSge requestSge;           // a scatter/gather element of the Work Request
    private LinkedList<IbvSge> sgeList;  // a list of scatter/gather elements

    public WorkRequest(IbvMr memoryRegion){
        requestBufferMR = memoryRegion;
        requestSge = new IbvSge();
        sgeList = new LinkedList<>();
        sgeList.add(requestSge);
    }

    /* *************************************************************
     * Abstract Methods
     * *************************************************************/

    /**
     * Prepare a Work Request before sending it to the Network
     * Card for execution.
     */
    public abstract void prepareRequest();

    /**
     * Set the WR id of this request.
     * @param workRequestId the WR id that this request will have.
     */
    public abstract void setRequestId(int workRequestId);

    /* *************************************************************
     * Getters/Setters
     * *************************************************************/

    public void setSgeLength(int length){
        requestSge.setLength(length);
    }

    public void setBufferMemoryAddress(long address) { requestSge.setAddr(address); }

    public IbvMr getRequestBufferMR() {
        return requestBufferMR;
    }

    public IbvSge getRequestSge() {
        return requestSge;
    }

    public LinkedList<IbvSge> getSgeList() {
        return sgeList;
    }

}
