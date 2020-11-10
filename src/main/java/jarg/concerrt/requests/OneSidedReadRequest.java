package jarg.concerrt.requests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * Defines a {@link SendRequest SendRequest} that uses
 * one-sided read.
 */
public class OneSidedReadRequest extends SendRequest implements Preparable {
    public RemoteLocation remoteLocation;

    public OneSidedReadRequest(IbvMr memoryRegion) {
        super(memoryRegion);
        remoteLocation = new RemoteLocation();
    }

    public OneSidedReadRequest(IbvMr memoryRegion, RemoteLocation remoteLocation){
        super(memoryRegion);
        this.remoteLocation = remoteLocation;
    }

    @Override
    public void prepareRequest() {
        // define the type of Work Request
        sendWR.setOpcode(IbvSendWR.IBV_WR_RDMA_READ);
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        // details about the data buffer that will receive the data
        requestSge.setLkey(requestBufferMR.getLkey());
        // remote memory location details
        // read data from the remote address with these details
        sendWR.getRdma().setRemote_addr(remoteLocation.remoteMemoryAddress);
        sendWR.getRdma().setRkey(remoteLocation.remoteLKey);
    }
}
