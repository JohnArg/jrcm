package jarg.concerrt.connections;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.RdmaEndpointGroup;
import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.RdmaCmId;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.concerrt.requests.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BasicEndpoint extends RdmaEndpoint {
    int maxWRs;                                 // max Work Requests supported
    int maxBufferSize;                          // max communications buffer size
    ByteBuffer[] communicationBuffers;          // contain data to be sent or received
    ByteBuffer rdmaEndpointMemoryBuffer;        // large memory block for communications
    IbvMr rdmaEndpointMemoryRegion;             // the above block's memory region
    CompletionHandler[] workRequestHandlers;    // will handle Work Completion events
    IntArrayFIFOQueue freeWrIds;                // available Work Request ids
    Byte blockingMonitor;                       // will be used to implement
                                                // blocking when necessary
    Map<ByteBuffer, Long> bufferMemoryAddressesMap; // <buffers, memory addresses>
    // Supported operations ------------------------------------------------------
    int supportedOperationsFlag;               // which WorkRequestTypes will be used
    TwoSidedSendRequest twoSidedSendRequest;
    TwoSidedRecv twoSidedRecv;
    OneSidedWriteRequest oneSidedWriteRequest;
    OneSidedReadRequest oneSidedReadRequest;


    public BasicEndpoint(RdmaEndpointGroup<? extends BasicEndpoint> group,
                            RdmaCmId idPriv, boolean serverSide,
                            int maxBufferSize, int maxWRs, int supportedOperationsFlag)
                            throws IOException {

        super(group, idPriv, serverSide);

        this.maxWRs = maxWRs;
        this.maxBufferSize = maxBufferSize;
        communicationBuffers = new ByteBuffer[maxWRs];
        workRequestHandlers = new CompletionHandler[maxWRs];
        freeWrIds = new IntArrayFIFOQueue(maxWRs);
        blockingMonitor = 0;
        bufferMemoryAddressesMap = new HashMap<ByteBuffer, Long>();
        this.supportedOperationsFlag = supportedOperationsFlag;

        // prepare objects for the supported operations
        enableSupportedOperations(supportedOperationsFlag);
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
     * Manage Work Request Ids
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
        ByteBuffer buffer;
        if(wrId > -1){
            buffer = communicationBuffers[wrId];
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
     * Manage Work Requests that are sent to the Network Card
     * *************************************************************/

    /**
     * Define which operations should the endpoint support. If this is
     * used during runtime, it can only enable the support for new
     * operations. It won't remove support for already supported
     * operations.
     *
     * @param supportedOperationsFlag bit flag that should have the bits set
     *                                according to the {@link WorkRequestTypes
     *                                WorkRequestTypes} class.
     */
    public void enableSupportedOperations(int supportedOperationsFlag) {
        if((supportedOperationsFlag & 0b1) == 1 ){
            if(twoSidedSendRequest == null){
                twoSidedSendRequest = new TwoSidedSendRequest(rdmaEndpointMemoryRegion);
            }
        }
        if((supportedOperationsFlag & 0b10) == 1 ){
            if(twoSidedRecv == null){
                twoSidedRecv = new TwoSidedRecv(rdmaEndpointMemoryRegion);
            }
        }
        if((supportedOperationsFlag & 0b100) == 1 ){
            if(oneSidedWriteRequest == null){
                oneSidedWriteRequest = new OneSidedWriteRequest(rdmaEndpointMemoryRegion);
            }
        }
        if((supportedOperationsFlag & 0b1000) == 1 ){
            if(oneSidedReadRequest == null){
                oneSidedReadRequest = new OneSidedReadRequest(rdmaEndpointMemoryRegion);
            }
        }
    }

    /**
     * Associate a {@link CompletionHandler} with a Work Request. Once a
     * Work Completion event is dispatched for this Work Request, the
     * corresponding handler can be called to react to the event.
     * @param workRequestId
     * @param handler
     */
    public void registerWrHandler(int workRequestId, CompletionHandler handler){
        workRequestHandlers[workRequestId] = handler;
    }


    /**
     * Post a 'send' RDMA Work Request (WR) to the NIC. A 'send' request can be a
     * <i>two-sided send</i>, a <i>one-sided write</i> or a <i>one-sided read</i> operation.
     * @param workRequestId the id of that the WR will have
     * @param dataLength the length of the data to be transmitted or read. Should not exceed
     *                   the maximum size of communications buffers.
     * @param workRequestType which of the 3 possible operations to use
     * @throws UnsupportedOperationException should throw this if the caller wants to
     * post a <i>two-sided recv</i> with this function.
     */
    public void send(int workRequestId, int dataLength, int workRequestType)
            throws UnsupportedOperationException{
        // Todo
    }

    /**
     * Post a 'receive' RDMA Work Request (WR) to the NIC. This is only used for
     * <i>two-sided recv</i> RDMA operations, which need to be posted in order to
     * receive messages sent with <i>two-sided send</i>. A posted <i>two-sided recv</i>
     * allows receiving a single message, so this function must be called every time
     * a message with <i>two-sided send</i> is expected.
     * @param workRequestId the id of that the WR will have
     * @param dataLength the length in bytes, of the data that will be received.
     *                   Should not exceed the maximum size of communications buffers.
     */
    public void recv(int workRequestId, int dataLength){
        // Todo
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

    public ByteBuffer getCommunicationsBuffer(int workRequestId){
        return communicationBuffers[workRequestId];
    }
}
