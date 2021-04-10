package jarg.jrcm.networking.dependencies.netrequests.impl.postsend;


import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;

/**
 * A wrapper for RDMA One-Sided Requests. The user has to specify
 * whether this is a One-sided READ or a One-sided WRITE request.
 * The request will be prepared according to the above user input.
 */
public class OneSidedRequest extends PostSendRequest{

    private OneSidedRequestType requestType;
    private RemoteLocation remoteLocation;

    public OneSidedRequest(IbvMr memoryRegion, OneSidedRequestType requestType) {
        super(memoryRegion);
        this.requestType = requestType;
    }

    public OneSidedRequest(IbvMr memoryRegion, OneSidedRequestType requestType, RemoteLocation remoteLocation) {
        super(memoryRegion);
        this.requestType = requestType;
        this.remoteLocation = remoteLocation;
    }

    /**
     * Specifies the type of an RDMA One-sided Request.
     */
    public enum OneSidedRequestType{
        READ,
        WRITE
    }

    @Override
    public void prepareRequest() {
        // define the type of Work Request
        if(requestType.equals(OneSidedRequestType.READ)){
            sendWR.setOpcode(IbvSendWR.IBV_WR_RDMA_READ);
        }else{
            sendWR.setOpcode(IbvSendWR.IBV_WR_RDMA_WRITE);
        }
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        getRequestSge().setLkey(getRequestBufferMR().getLkey());
        // operate on the remote memory address with these details
        sendWR.getRdma().setRemote_addr(remoteLocation.remoteMemoryAddress);
        sendWR.getRdma().setRkey(remoteLocation.remoteLKey);
    }
}
