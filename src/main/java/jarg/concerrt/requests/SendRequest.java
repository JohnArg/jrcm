package jarg.concerrt.requests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * Defines a "send" Work Request for the NIC
 */
public class SendRequest extends BasicWorkRequest{
    IbvSendWR sendWR;
    int opCode;
    int flags;

    public SendRequest(IbvMr memoryRegion){
        super(memoryRegion);
        // attach scatter/gather list to the work request
        sendWR.setSg_list(sgeList);
    }

    @Override
    public void setRequestId(int workRequestId) {
        sendWR.setWr_id(workRequestId);
    }

    // Getters ===================================

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
