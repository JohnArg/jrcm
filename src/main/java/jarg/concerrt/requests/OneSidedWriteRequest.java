package jarg.concerrt.requests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * Defines a {@link SendRequest SendRequest} that uses
 * one-sided write.
 */
public class OneSidedWriteRequest extends SendRequest implements Preparable {
    public RemoteLocation remoteLocation;

    public OneSidedWriteRequest(IbvMr memoryRegion) {
        super(memoryRegion);
        remoteLocation = new RemoteLocation();
    }

    public OneSidedWriteRequest(IbvMr memoryRegion, RemoteLocation remoteLocation){
        super(memoryRegion);
        this.remoteLocation = remoteLocation;
    }

    @Override
    public void prepareRequest() {
        // define the type of Work Request
        sendWR.setOpcode(IbvSendWR.IBV_WR_RDMA_WRITE);
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        // details about the data buffer to be sent to the remote side
        requestSge.setAddr(requestBufferMR.getAddr());
        requestSge.setLength(requestBufferMR.getLength());
        requestSge.setLkey(requestBufferMR.getLkey());
        // remote memory location details
        // write data to the remote address with these details
        sendWR.getRdma().setRemote_addr(remoteLocation.remoteMemoryAddress);
        sendWR.getRdma().setRkey(remoteLocation.remoteLKey);
    }


}