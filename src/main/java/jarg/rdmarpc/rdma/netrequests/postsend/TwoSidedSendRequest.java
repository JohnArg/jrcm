package jarg.rdmarpc.rdma.netrequests.postsend;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * A wrapper for an RDMA Two-Sided Send.
 */
public class TwoSidedSendRequest extends PostSendRequest {

    public TwoSidedSendRequest(IbvMr memoryRegion) {
        super(memoryRegion);
    }


    @Override
    public void prepareRequest() {
        sendWR.setOpcode(IbvSendWR.IBV_WR_SEND);
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        getRequestSge().setLkey(getRequestBufferMR().getLkey());
    }

}
