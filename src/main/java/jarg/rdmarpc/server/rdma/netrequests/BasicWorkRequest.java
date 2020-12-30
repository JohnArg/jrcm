package jarg.rdmarpc.server.rdma.netrequests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSge;

import java.util.LinkedList;

/**
 * Abstract class defining an RDMA Work Request (WR), that will be
 * sent to the Network Card and will be executed by it.
 */
public abstract class BasicWorkRequest {
    IbvMr requestBufferMR;       // the memory region registered to the NIC
    IbvSge requestSge;           // a scatter/gather element of the Work Request
    LinkedList<IbvSge> sgeList;  // disni requires this to be a LinkedList

    public BasicWorkRequest(IbvMr memoryRegion){
        requestBufferMR = memoryRegion;
        requestSge = new IbvSge();
        sgeList = new LinkedList<>();
        sgeList.add(requestSge);
    }

    /* *************************************************************
     * Non-abstract Methods
     * *************************************************************/

    public void setSgeLength(int length){
        requestSge.setLength(length);
    }

    /* *************************************************************
     * Abstract Methods
     * *************************************************************/

    public abstract void setRequestId(int workRequestId);

    public abstract void setBufferMemoryAddress(long address);

}
