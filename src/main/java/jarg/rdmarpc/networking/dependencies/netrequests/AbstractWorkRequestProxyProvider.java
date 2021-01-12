package jarg.rdmarpc.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvWC;
import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

import static jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType.*;
import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.*;

/**
 * Abstract class that defines dependencies needed to implement a {@link WorkRequestProxyProvider}.
 */
public abstract class AbstractWorkRequestProxyProvider implements WorkRequestProxyProvider{

    // dependencies (injected strategies) ------------------------------------------------------------
    private NetworkBufferManager bufferManager;         // the manager of network data buffers
    private RdmaCommunicator endpoint;                  // the endpoint associated with this provider

    public AbstractWorkRequestProxyProvider() {
    }

    @Override
    public WorkRequestProxy getWorkRequestProxyForWc(IbvWC workCompletionEvent){
        if(workCompletionEvent == null){
            return null;
        }
        // extract info from event
        WorkRequestProxy proxy;
        int workRequestId = (int) workCompletionEvent.getWr_id();
        int operationCode = workCompletionEvent.getOpcode();
        // identify request type
        WorkRequestType workRequestType;
        workRequestType = getWorkRequestTypeForWcOperationCode(operationCode);
        if(workRequestType == null){
            return null;
        }
        // identify posted request type
        PostedRequestType postedRequestType;
        if(workRequestType.equals(TWO_SIDED_RECV)){
            postedRequestType = RECEIVE;
        }else{
            postedRequestType = SEND;
        }
        // get request buffer
        ByteBuffer requestBuffer = bufferManager.getWorkRequestBuffer(workRequestType, workRequestId);
        // time to construct the proxy
        proxy = new WorkRequestProxy(workRequestId, postedRequestType, workRequestType, requestBuffer, endpoint);
        return proxy;
    }

    /* ***************************************************************
     *   Getters/Setters
     * ***************************************************************/

    public NetworkBufferManager getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(NetworkBufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public RdmaCommunicator getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(RdmaCommunicator endpoint) {
        this.endpoint = endpoint;
    }
}