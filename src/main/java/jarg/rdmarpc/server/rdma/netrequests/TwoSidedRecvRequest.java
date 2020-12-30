package jarg.rdmarpc.server.rdma.netrequests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvRecvWR;

public class TwoSidedRecvRequest extends BasicWorkRequest implements Preparable{
    private IbvRecvWR recvWR;


    public TwoSidedRecvRequest(IbvMr memoryRegion){
        super(memoryRegion);
        // attach scatter/gather list to the work request
        recvWR = new IbvRecvWR();
        recvWR.setSg_list(sgeList);
    }

    /* *************************************************************
     * Implementing Methods
     * *************************************************************/

    @Override
    public void setRequestId(int workRequestId) {
        recvWR.setWr_id(workRequestId);
    }

    @Override
    public void setBufferMemoryAddress(long address) { requestSge.setAddr(address); }

    @Override
    public void prepareRequest() {
        // details about the data buffer that will hold the data
        requestSge.setLkey(requestBufferMR.getLkey());
    }

    /* *************************************************************
     * Getters
     * *************************************************************/

    public IbvRecvWR getRecvWR() {
        return recvWR;
    }
}
