package jarg.concerrt.connections;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.RdmaEndpointGroup;
import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.RdmaCmId;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class BasicEndpoint extends RdmaEndpoint {
    int maxWRs;                                 // max Work Requests supported
    int maxBufferSize;                          // max communications buffer size
    ByteBuffer[] communicationBuffers;          // contain data to be sent or received
    ByteBuffer rdmaEndpointMemoryBuffer;        // large memory block for communications
    IbvMr rdmaEndpointMemoryRegion;             // the above block's memory region
    CompletionHandler[] workRequestHandlers;    // will handle Work Completion events
    IntArrayFIFOQueue freeWrIds;                // available Work Request ids
    private Byte blockingMonitor;               // will be used to implement
                                                // blocking when necessary
    Map<ByteBuffer, Long> bufferMemoryAddressesMap; // <buffers, memory addresses>


    protected BasicEndpoint(RdmaEndpointGroup<? extends BasicEndpoint> group,
                            RdmaCmId idPriv, boolean serverSide,
                            int maxBufferSize, int maxWRs)
                            throws IOException {
        super(group, idPriv, serverSide);

        this.maxWRs = maxWRs;
        this.maxBufferSize = maxBufferSize;
        communicationBuffers = new ByteBuffer[maxWRs];
        workRequestHandlers = new CompletionHandler[maxWRs];
        freeWrIds = new IntArrayFIFOQueue(maxWRs);
        bufferMemoryAddressesMap = new HashMap<ByteBuffer, Long>();
    }

    /* *************************************************************
     * Overridden Methods
     * *************************************************************/

    @Override
    public void init() throws IOException{
        /*-------- Memory allocation/registration for communications ---------------*/
        // Allocate a large memory area once, for all communication buffers
        rdmaEndpointMemoryBuffer = ByteBuffer.allocateDirect(maxBufferSize * maxWRs);
        // Register this to the Network Card too
        rdmaEndpointMemoryRegion = registerMemory(rdmaEndpointMemoryBuffer)
                                    .execute().free().getMr();
        // We have as many communication buffers at the maximum available WRs (maxWRs)
        // Give each communication buffer a subset of the large memory area,
        // as large as maxBufferSize.
        int currentLimit = maxBufferSize;
        for(int i=0; i < maxWRs; i++){
            rdmaEndpointMemoryBuffer.limit(currentLimit);
            communicationBuffers[i] = rdmaEndpointMemoryBuffer.slice();
            rdmaEndpointMemoryBuffer.position(currentLimit);
            currentLimit += maxBufferSize;
            // also keep the memory address of the buffer for communications
            long address = ((sun.nio.ch.DirectBuffer) communicationBuffers[i])
                    .address();
            bufferMemoryAddressesMap.put(communicationBuffers[i], address);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        super.close();
        // deregister the memory for communications
        deregisterMemory(rdmaEndpointMemoryRegion);
    }

    /* *************************************************************
     * Extra Methods
     * *************************************************************/

    /**
     * It will return a {@link WorkRequestData WorkRequestData} object
     * that contains a Work Request id and the corresponding buffer.
     * If there is no available Work Request id at the moment, the
     * method will block until there is one.
     *
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request.
     */
    public synchronized WorkRequestData getWorkRequestBlocking(){
        int wrId = -1;
        // if there are no available Work Request ids, block
        // until there are
        synchronized (blockingMonitor){
            while(freeWrIds.isEmpty()){
                try {
                    blockingMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            wrId = freeWrIds.dequeueInt();
        }
        // got a WR id
        ByteBuffer buffer = communicationBuffers[wrId];
        WorkRequestData data = new WorkRequestData(wrId, buffer);
        return data;
    }

    /**
     * It will return a {@link WorkRequestData WorkRequestData} object
     * that contains a Work Request id and the corresponding buffer.
     * If there is no available Work Request id at the moment, the
     * returned object will be null.
     *
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request or null on failure.
     */
    public synchronized WorkRequestData getWorkRequestNow(){
        WorkRequestData data = null;
        int wrId = -1;
        // try to get a WR id
        if(!freeWrIds.isEmpty()){
            wrId = freeWrIds.dequeueInt();
        }
        // if a WR id was available
        if(wrId > -1){
            ByteBuffer buffer = communicationBuffers[wrId];
            data = new WorkRequestData(wrId, buffer);
        }
        return data;
    }

    /**
     * Returns a used Work Request id to a queue of available
     * Work Request ids. The returned id can then be reused by
     * other Work Requests.
     * @param workRequestId the Work Request id that is freed.
     */
    public synchronized void freeUpWrID(int workRequestId){
        freeWrIds.enqueue(workRequestId);
        // if the queue was empty, notify any blocked threads
        if(freeWrIds.size() == 1){
            synchronized (blockingMonitor){
                blockingMonitor.notifyAll();
            }
        }
    }

    /* *************************************************************
     * Getters
     * *************************************************************/

    public int getMaxWRs() {
        return maxWRs;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public CompletionHandler[] getWorkRequestHandlers() {
        return workRequestHandlers;
    }
}
