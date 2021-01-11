package jarg.rdmarpc.networking.dependencies.netrequests.impl;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * An {@link WorkRequestProxyProvider} that maintains an internal queue of available postSend requests.
 * PostRecv requests need not be managed in a queue, as they are all pre-posted before communications and reused.
 */
public class QueuedProxyProvider extends AbstractWorkRequestProxyProvider{
    private final Logger logger = LoggerFactory.getLogger(QueuedProxyProvider.class);
    private final IntArrayFIFOQueue freePostSendWrIds;  // available Work Request ids for the postSend queue

    // Use to inject this as a dependency. Requires setting this object's dependencies with setters later.
    public QueuedProxyProvider(int maxWorkRequests){
        super();
        this.freePostSendWrIds = new IntArrayFIFOQueue(maxWorkRequests);
        for(int i = 0; i < maxWorkRequests; i++){
            freePostSendWrIds.enqueue(i);
        }
    };

    @Override
    public WorkRequestProxy getPostSendRequestBlocking(WorkRequestType requestType) {
        WorkRequestProxy proxy = null;
        int workRequestId = -1;
        // if there are no available Work Request ids, block until there are
        synchronized (freePostSendWrIds){
            while(freePostSendWrIds.isEmpty()){
                try {
                    freePostSendWrIds.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            workRequestId = freePostSendWrIds.dequeueInt();
        }
        ByteBuffer buffer = getBufferManager().getWorkRequestBuffer(requestType, workRequestId);
        proxy = new WorkRequestProxy(workRequestId, PostedRequestType.SEND, requestType, buffer, getEndpoint());
        return proxy;
    }

    @Override
    public WorkRequestProxy getPostSendRequestNow(WorkRequestType requestType) {
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
            proxy = new WorkRequestProxy(workRequestId, PostedRequestType.SEND, requestType, buffer, getEndpoint());
        }
        return proxy;
    }

    @Override
    public void releaseWorkRequest(WorkRequestProxy workRequestProxy) {
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
            getEndpoint().postNetOperationToNIC(workRequestProxy);
        }
    }
}
