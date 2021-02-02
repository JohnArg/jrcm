package jarg.rdmarpc.networking.dependencies.netrequests.impl;

import com.ibm.disni.RdmaActiveEndpoint;
import com.ibm.disni.RdmaEndpoint;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequest;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.TWO_SIDED_RECV;

/**
 * An {@link WorkRequestProxyProvider} that maintains an internal queue of available postSend requests.
 * PostRecv requests need not be managed in a queue, as they are all pre-posted before communications and reused.
 */
public class QueuedProxyProvider extends AbstractWorkRequestProxyProvider{
    private final Logger logger = LoggerFactory.getLogger(QueuedProxyProvider.class);

    private final IntArrayFIFOQueue freePostSendWrIds;  // available Work Request ids for the postSend queue
    private int maxWorkRequests;
    private WorkRequestProxy[] workRequestProxies;      // pre-created, cached and reused WR proxies

    // Use to inject this as a dependency. Requires setting this object's dependencies with setters later.
    public QueuedProxyProvider(int maxWorkRequests){
        super();
        this.maxWorkRequests = maxWorkRequests;
        this.freePostSendWrIds = new IntArrayFIFOQueue(maxWorkRequests);
        workRequestProxies = new WorkRequestProxy[maxWorkRequests];
        for(int i = 0; i < maxWorkRequests; i++){
            freePostSendWrIds.enqueue(i);
            workRequestProxies[i] = new WorkRequestProxy();
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
        ByteBuffer buffer = getBufferManager().getWorkRequestBuffer(requestType, workRequestId);
        WorkRequestProxy proxy = workRequestProxies[workRequestId];
        proxy.setId(workRequestId).setPostType(PostedRequestType.SEND).setWorkRequestType(requestType)
                .setBuffer(buffer).setEndpoint(getEndpoint());
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
            buffer =  getBufferManager().getWorkRequestBuffer(requestType, workRequestId);
            proxy = workRequestProxies[workRequestId];
            proxy.setId(workRequestId).setPostType(PostedRequestType.SEND).setWorkRequestType(requestType)
                    .setBuffer(buffer).setEndpoint(getEndpoint());
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
            RdmaEndpoint endpoint = (RdmaEndpoint) getEndpoint();
            if((!endpoint.isClosed()) && endpoint.getQp().isOpen()) {
                getEndpoint().postNetOperationToNIC(workRequestProxy);
            }
        }
    }
}
