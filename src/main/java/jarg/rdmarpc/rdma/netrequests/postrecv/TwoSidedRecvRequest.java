package jarg.rdmarpc.rdma.netrequests.postrecv;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvRecvWR;
import jarg.rdmarpc.rdma.netrequests.WorkRequest;

/**
 * A wrapper for an RDMA Two-Sided Receive.
 */
public class TwoSidedRecvRequest extends WorkRequest {
    private IbvRecvWR recvWR;


    public TwoSidedRecvRequest(IbvMr memoryRegion){
        super(memoryRegion);
        // attach scatter/gather list to the work request
        recvWR = new IbvRecvWR();
        recvWR.setSg_list(getSgeList());
    }

    /* *************************************************************
     * Implementing Methods
     * *************************************************************/

    @Override
    public void setRequestId(int workRequestId) {
        recvWR.setWr_id(workRequestId);
    }

    @Override
    public void prepareRequest() {
        getRequestSge().setLkey(getRequestBufferMR().getLkey());
    }

    /* *************************************************************
     * Getters
     * *************************************************************/

    public IbvRecvWR getRecvWR() {
        return recvWR;
    }
}
