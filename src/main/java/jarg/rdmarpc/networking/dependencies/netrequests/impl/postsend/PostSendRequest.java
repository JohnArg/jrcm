package jarg.rdmarpc.networking.dependencies.netrequests.impl.postsend;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequest;

/**
 * An abstract wrapper for RDMA requests send via <code>postSend()</code>.
 */
public abstract class PostSendRequest extends WorkRequest {
    IbvSendWR sendWR;
    int opCode;
    int flags;

    public PostSendRequest(IbvMr memoryRegion){
        super(memoryRegion);
        // attach scatter/gather list to the work request
        sendWR = new IbvSendWR();
        sendWR.setSg_list(getSgeList());
    }

    /* *************************************************************
     * Implementing Abstract Methods
     * *************************************************************/

    @Override
    public void setRequestId(int workRequestId) {
        sendWR.setWr_id(workRequestId);
    }

    /* *************************************************************
     * Getters
     * *************************************************************/

    public IbvSendWR getSendWR() {
        return sendWR;
    }

    public int getOpCode() {
        return opCode;
    }

    public int getFlags() {
        return flags;
    }
}
