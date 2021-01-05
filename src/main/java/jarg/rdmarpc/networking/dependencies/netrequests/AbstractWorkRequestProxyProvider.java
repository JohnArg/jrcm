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

    @Override
    public WorkRequestProxy getWorkRequestProxyForWc(IbvWC workCompletionEvent){
        WorkRequestProxy proxy;
        int workRequestId = (int) workCompletionEvent.getWr_id();
        int operationCode = workCompletionEvent.getOpcode();
        WorkRequestType workRequestType;
        PostedRequestType postedRequestType;

        // identify request type
        switch(operationCode){
            case 128:
                workRequestType = TWO_SIDED_RECV;
                postedRequestType = RECEIVE;
                break;
            case 0 :
                workRequestType = TWO_SIDED_SEND_SIGNALED;
                postedRequestType = SEND;
                break;
            case 1:
                workRequestType = ONE_SIDED_WRITE_SIGNALED;
                postedRequestType = SEND;
                break;
            case 2 :
                workRequestType = ONE_SIDED_READ_SIGNALED;
                postedRequestType = SEND;
                break;
            default:
                return null;
        }
        // get request buffer
        ByteBuffer requestBuffer = bufferManager.getWorkRequestBuffer(workRequestType, workRequestId);
        // time to construct the proxy
        proxy = new WorkRequestProxy(workRequestId, postedRequestType, workRequestType, requestBuffer, endpoint);
        return proxy;
    }
}

//    Compare to opcodes from com.ibm.disni.verbs.IbvWC.IbvWcOpcode
// Then call the appropriate handlers for the events and free the Work Request id for reuse
//if (wc.getStatus() == 5){
//        throw new IOException("Unkown operation! wc.status " + wc.getStatus());
//        } else if (wc.getStatus() != 0){
//        throw new IOException("Faulty operation! wc.status " + wc.getStatus());
//        }else{
//        int workRequestId = (int) wc.getWr_id();
//        // Completion for two-side receive operation
//        if(wc.getOpcode() == 128){
//        completionHandler.handleTwoSidedReceive(wc, this,
//        receiveBuffers[(int) wc.getWr_id()]);
//        }
//        // Completion for two-side send operation
//        else if(wc.getOpcode() == 0){
//        completionHandler.handleTwoSidedSend(wc, this);
//        }
//        // Completion for one-sided write operation
//        else if(wc.getOpcode() == 1){
//        completionHandler.handleOneSidedWrite(wc, this);
//        }
//        // Completion for one-sided read operation
//        else if(wc.getOpcode() == 2){
//        completionHandler.handleOneSidedRead(wc, this);
//        }