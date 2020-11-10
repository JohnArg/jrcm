package jarg.concerrt.requests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * Defines a {@link SendRequest SendRequest} that uses
 * two-sided signaled send.
 */
public class TwoSidedSendRequest extends SendRequest implements Preparable{

    public TwoSidedSendRequest(IbvMr memoryRegion){
        super(memoryRegion);
    }

    @Override
    public void prepareRequest() {
        // define the type of Work Request
        sendWR.setOpcode(IbvSendWR.IBV_WR_SEND);
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        // details about the data buffer to be sent to the remote side
        requestSge.setAddr(requestBufferMR.getAddr());
        requestSge.setLength(requestBufferMR.getLength());
        requestSge.setLkey(requestBufferMR.getLkey());
    }
}
