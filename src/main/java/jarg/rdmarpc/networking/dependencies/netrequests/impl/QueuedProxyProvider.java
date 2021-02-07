package jarg.rdmarpc.networking.dependencies.netrequests.impl;

import com.ibm.disni.verbs.IbvWC;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType.RECEIVE;
import static jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType.SEND;
import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.TWO_SIDED_RECV;

/**
 * An {@link WorkRequestProxyProvider} that maintains an internal queue of available postSend requests.
 * PostRecv requests need not be managed in a queue, as they are all pre-posted before communications and reused.
 */
public class QueuedProxyProvider implements WorkRequestProxyProvider{
    private final Logger logger = LoggerFactory.getLogger(QueuedProxyProvider.class);

    private NetworkBufferManager bufferManager;         // the manager of network data buffers
    private RdmaCommunicator rdmaCommunicator;          // the communicator associated with this provider
    private final IntArrayFIFOQueue freePostSendWrIds;  // available Work Request ids for the postSend queue
    private int maxWorkRequests;
    private WorkRequestProxy[] postSendWRProxies;      // pre-created, cached and reused WR proxies
    private WorkRequestProxy[] postRecvWRProxies;

    // Use to inject this as a dependency. Requires setting this object's dependencies with setters later.
    public QueuedProxyProvider(int maxWorkRequests){
        super();
        this.maxWorkRequests = maxWorkRequests;
        this.freePostSendWrIds = new IntArrayFIFOQueue(maxWorkRequests);
        postSendWRProxies = new WorkRequestProxy[maxWorkRequests];
        postRecvWRProxies = new WorkRequestProxy[maxWorkRequests];
        for(int i = 0; i < maxWorkRequests; i++){
            freePostSendWrIds.enqueue(i);
            postSendWRProxies[i] = new WorkRequestProxy();
            postSendWRProxies[i].setId(i).setPostType(SEND);
            postRecvWRProxies[i] = new WorkRequestProxy();
            postRecvWRProxies[i].setId(i).setPostType(RECEIVE).setWorkRequestType(TWO_SIDED_RECV);
        }
    };

    @Override
    public WorkRequestProxy getPostSendRequestBlocking(WorkRequestType requestType) {
        // prevent errors
        if((maxWorkRequests == 0) || (requestType == null)
                || (getBufferManager() == null) || (requestType == TWO_SIDED_RECV)){
            return null;
        }
        int workRequestId;
        // if there are no available Work Request ids, block until there are
        synchronized (freePostSendWrIds){
            while(freePostSendWrIds.isEmpty()){
                try {
                    freePostSendWrIds.wait();
                } catch (InterruptedException e) {
                   logger.error("Proxy provider interrupted on blocking call.", e);
                }
            }
            workRequestId = freePostSendWrIds.dequeueInt();
        }
        ByteBuffer buffer = bufferManager.getWorkRequestBuffer(requestType, workRequestId);
        WorkRequestProxy proxy = postSendWRProxies[workRequestId];
        proxy.setWorkRequestType(requestType).setBuffer(buffer);
        return proxy;
    }

    @Override
    public WorkRequestProxy getPostSendRequestNow(WorkRequestType requestType) {
        // prevent errors
        if((maxWorkRequests == 0) || (requestType == null)
                || (getBufferManager() == null) || (requestType == TWO_SIDED_RECV)){
            return null;
        }
        WorkRequestProxy proxy = null;
        int workRequestId = -1;

        synchronized (freePostSendWrIds){
            // try to get a WR id
            if (!freePostSendWrIds.isEmpty()) {
                workRequestId = freePostSendWrIds.dequeueInt();
            }
        }
        // if a WR id was available
        ByteBuffer buffer;
        if (workRequestId > -1) {
            buffer =  bufferManager.getWorkRequestBuffer(requestType, workRequestId);
            proxy = postSendWRProxies[workRequestId];
            proxy.setWorkRequestType(requestType).setBuffer(buffer);
        }
        return proxy;
    }

    @Override
    public void releaseWorkRequest(WorkRequestProxy workRequestProxy) {
        // prevent errors
        if((maxWorkRequests == 0) || (workRequestProxy == null) || (getBufferManager() == null)){
            return;
        }
        if(workRequestProxy.getPostType().equals(PostedRequestType.SEND)) {
            synchronized (freePostSendWrIds){
                workRequestProxy.getBuffer().clear();   // clear previous data
                freePostSendWrIds.enqueue(workRequestProxy.getId());
                // if the queue was empty, notify any blocked threads
                if(freePostSendWrIds.size() == 1){
                    freePostSendWrIds.notifyAll();
                }
            }
        }else if (workRequestProxy.getPostType().equals(PostedRequestType.RECEIVE)) {
            // now that this WR id is free for reuse, we can repost
            // that 'receive' request immediately to accept new data
            workRequestProxy.getBuffer().clear();   // clear previous data
            rdmaCommunicator.postNetOperationToNIC(workRequestProxy);
        }
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
        if(workRequestType.equals(TWO_SIDED_RECV)){
            proxy = postRecvWRProxies[workRequestId];
        }else{
            proxy = postSendWRProxies[workRequestId];
        }
        return proxy;
    }

    /* ***************************************************************
     *   Getters/Setters
     * ***************************************************************/
    @Override
    public NetworkBufferManager getBufferManager() {
        return bufferManager;
    }

    @Override
    public void setBufferManager(NetworkBufferManager bufferManager) {
        this.bufferManager = bufferManager;
        for(int i=0; i<maxWorkRequests; i++){
            postRecvWRProxies[i].setBuffer(bufferManager.getWorkRequestBuffer(TWO_SIDED_RECV, i));
        }
    }

    @Override
    public RdmaCommunicator getCommunicator() {
        return rdmaCommunicator;
    }

    @Override
    public void setCommunicator(RdmaCommunicator communicator) {
        this.rdmaCommunicator = communicator;
        for(int i=0; i<maxWorkRequests; i++){
            postSendWRProxies[i].setRdmaCommunicator(communicator);
            postRecvWRProxies[i].setRdmaCommunicator(communicator);
        }
    }
}
