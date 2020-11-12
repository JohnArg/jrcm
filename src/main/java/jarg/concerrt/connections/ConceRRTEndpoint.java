package jarg.concerrt.connections;

import com.ibm.disni.RdmaActiveEndpoint;
import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.verbs.*;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.concerrt.requests.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConceRRTEndpoint extends RdmaActiveEndpoint {
    /*
    Note :
    Two types of RDMA requests can be posted to the Network Card :
    1) send requests : two-sided send, one-sided operations
    2) receive (or recv) requests : two-sided recv

    Each of the two types of requests will use its own Work Request ids, communication buffers
    or Work Completion handlers. The Endpoint will support maximum maxWR Work Request ids for
    send requests and the same amount for receive requests. The maxWR number indicates how
    many requests of that type can the Network Card receive before its internal queue for
    this type of request gets full.
     */
    int maxWRs;                                 // max Work Requests supported
    int maxBufferSize;                          // max communications buffer size
    ByteBuffer[] sendBuffers;                   // contain data to be sent
    ByteBuffer[] receiveBuffers;                // contain data to be received
    ByteBuffer rdmaEndpointMemoryBuffer;        // large memory block for communications
    IbvMr rdmaEndpointMemoryRegion;             // the above block's memory region
    boolean twoSidedReceiveEnabled;             // whether we use two-sided recv operations
    CompletionHandler[] sendWorkCompletionHandlers; // will handle Work Completions for send events
    CompletionHandler[] recvWorkCompletionHandlers; // will handle Work Completions for recv events
    IntArrayFIFOQueue freeSendWrIds;            // available Work Request ids for send requests
    IntArrayFIFOQueue freeRecvWrIds;            // available Work Request ids for recv requests
    Map<ByteBuffer, Long> sendBufferAddressesMap; // <send buffers, memory addresses>
    Map<ByteBuffer, Long> recvBufferAddressesMap; // <recv buffers, memory addresses>
    // Supported operations ------------------------------------------------------
    int supportedOperationsFlag;                // which WorkRequestTypes will be used
    // Keep stored SVCs (Stateful Verb Calls => see jVerbs from IBM) for each request type here.
    // These SVCs can be reused when posting Work Requests to the NIC. This is faster
    // than creating new ones everytime.
    SVCPostSend[] twoSidedSendSVCs;
    SVCPostSend[] oneSidedWriteSVCs;
    SVCPostSend[] oneSidedReadSVCs;
    SVCPostRecv[] twoSidedRecvSVCs;
    // Used for synchronization when necessary -----------------------------------
    Byte sendBlockingMonitor;
    Byte recvBlockingMonitor;

    public ConceRRTEndpoint(RdmaActiveEndpointGroup<? extends ConceRRTEndpoint> group,
                            RdmaCmId idPriv, boolean serverSide,
                            int maxBufferSize, int maxWRs, int supportedOperationsFlag)
                            throws IOException {

        super(group, idPriv, serverSide);

        this.maxWRs = maxWRs;
        this.maxBufferSize = maxBufferSize;
        sendBuffers = new ByteBuffer[maxWRs];
        twoSidedReceiveEnabled = false;
        sendWorkCompletionHandlers = new CompletionHandler[maxWRs];
        freeSendWrIds = new IntArrayFIFOQueue(maxWRs);
        sendBlockingMonitor = 0;
        sendBufferAddressesMap = new HashMap<ByteBuffer, Long>();
        this.supportedOperationsFlag = supportedOperationsFlag;
        // if we are going to use two-sided operations, we need extra initializations
        if((supportedOperationsFlag & 0b1) == 1){
            receiveBuffers = new ByteBuffer[maxWRs];
            recvBufferAddressesMap = new HashMap<ByteBuffer, Long>();
            twoSidedReceiveEnabled = true;
            recvWorkCompletionHandlers = new CompletionHandler[maxWRs];
            freeRecvWrIds = new IntArrayFIFOQueue(maxWRs);
            recvBlockingMonitor = 0;
        }
        // prepare objects for the supported operations
        enableSupportedOperations(supportedOperationsFlag);
    }

    /* *************************************************************
     * Inner classes
     * *************************************************************/

    /**
     * Identifies a request as a 'send' request or
     * a 'receive' request. Only the RDMA <i>two-sided recv</i> is
     * considered a 'receive' request. The <i>two-sided send</i>
     * and the <i>one-sided</i> operations are considered of 'send'
     * type.
     */
    public enum PostedRequestType{
        SEND,
        RECEIVE
    }

    /* *************************************************************
     * Overridden Methods
     * *************************************************************/

    /**
     * Allocate and register large memory area once, for all communication buffers.
     * @throws IOException
     */
    @Override
    public void init() throws IOException{
        int bufferArrayBytes = maxBufferSize * maxWRs;
        // If we use two-sided communication, we'll need separate receive buffers
        if(twoSidedReceiveEnabled){
            // keep enough space for received message buffers too
            rdmaEndpointMemoryBuffer = ByteBuffer.allocateDirect(bufferArrayBytes * 2);
            // give equal space to send and receive buffers
            int currentLimit = maxBufferSize;
            for(int i=0; i < maxWRs; i++){
                rdmaEndpointMemoryBuffer.limit(currentLimit);
                sendBuffers[i] = rdmaEndpointMemoryBuffer.slice();
                rdmaEndpointMemoryBuffer.position(currentLimit);
                currentLimit += maxBufferSize;
                // keep the memory address of the buffer for communications
                long address = ((sun.nio.ch.DirectBuffer) sendBuffers[i])
                        .address();
                sendBufferAddressesMap.put(sendBuffers[i], address);
            }
            for(int i=0; i < maxWRs; i++){
                rdmaEndpointMemoryBuffer.limit(currentLimit);
                receiveBuffers[i] = rdmaEndpointMemoryBuffer.slice();
                rdmaEndpointMemoryBuffer.position(currentLimit);
                currentLimit += maxBufferSize;
                // keep the memory address of the buffer for communications
                long address = ((sun.nio.ch.DirectBuffer) receiveBuffers[i])
                        .address();
                recvBufferAddressesMap.put(receiveBuffers[i], address);
            }
        } else{ // otherwise we only need a shared buffer for the 'send' RDMA operations
            rdmaEndpointMemoryBuffer = ByteBuffer.allocateDirect(bufferArrayBytes);
            int currentLimit = maxBufferSize;
            for(int i=0; i < maxWRs; i++){
                rdmaEndpointMemoryBuffer.limit(currentLimit);
                sendBuffers[i] = rdmaEndpointMemoryBuffer.slice();
                rdmaEndpointMemoryBuffer.position(currentLimit);
                currentLimit += maxBufferSize;
                // keep the memory address of the buffer for communications
                long address = ((sun.nio.ch.DirectBuffer) sendBuffers[i])
                        .address();
                sendBufferAddressesMap.put(sendBuffers[i], address);
            }
        }
        // Register the large allocated memory to the Network Card too
        rdmaEndpointMemoryRegion = registerMemory(rdmaEndpointMemoryBuffer)
                                    .execute().free().getMr();
    }

    @Override
    public void dispatchCqEvent(IbvWC wc) throws IOException {
        // Compare to opcodes from com.ibm.disni.verbs.IbvWC.IbvWcOpcode
        // Then call the appropriate handlers for the events and free the Work Request id for reuse
        int workRequestId = (int) wc.getWr_id();
        // Completion for 'receive' operation
        if(wc.getOpcode() == 128){
            recvWorkCompletionHandlers[workRequestId].handleCompletionEvent(wc);
            freeUpWrID(workRequestId, PostedRequestType.RECEIVE);
        }
        // Completion for 'send' operation
        else if((wc.getOpcode() == 0) || (wc.getOpcode() == 1) ||
                (wc.getOpcode() == 2)) {
            sendWorkCompletionHandlers[workRequestId].handleCompletionEvent(wc);
            freeUpWrID(workRequestId, PostedRequestType.SEND);
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
     * @param type a {@link PostedRequestType PostedRequestType} type
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request.
     */
    public synchronized WorkRequestData getWorkRequestBlocking(PostedRequestType type){
        WorkRequestData data = null;
        int wrId = -1;
        // if there are no available Work Request ids, block until there are
        if(type == PostedRequestType.SEND){
            synchronized (sendBlockingMonitor){
                while(freeSendWrIds.isEmpty()){
                    try {
                        sendBlockingMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                wrId = freeSendWrIds.dequeueInt();
            }
            ByteBuffer buffer = sendBuffers[wrId];
            data = new WorkRequestData(wrId, buffer);
        }else if (type == PostedRequestType.RECEIVE) {
            synchronized (recvBlockingMonitor){
                while(freeRecvWrIds.isEmpty()){
                    try {
                        recvBlockingMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                wrId = freeRecvWrIds.dequeueInt();
            }
            ByteBuffer buffer = receiveBuffers[wrId];
            data = new WorkRequestData(wrId, buffer);
        }
        return data;
    }

    /**
     * It will return a {@link WorkRequestData WorkRequestData} object
     * that contains a Work Request id and the corresponding buffer.
     * If there is no available Work Request id at the moment, the
     * returned object will be null.
     *
     * @param type a {@link PostedRequestType PostedRequestType} type
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request or null on failure.
     */
    public synchronized WorkRequestData getWorkRequestNow(PostedRequestType type){
        WorkRequestData data = null;
        int wrId = -1;

        if(type == PostedRequestType.SEND) {
            // try to get a WR id
            if (!freeSendWrIds.isEmpty()) {
                wrId = freeSendWrIds.dequeueInt();
            }
            // if a WR id was available
            ByteBuffer buffer;
            if (wrId > -1) {
                buffer = sendBuffers[wrId];
                data = new WorkRequestData(wrId, buffer);
            }
        }else if (type == PostedRequestType.RECEIVE) {
            // try to get a WR id
            if (!freeRecvWrIds.isEmpty()) {
                wrId = freeRecvWrIds.dequeueInt();
            }
            // if a WR id was available
            ByteBuffer buffer;
            if (wrId > -1) {
                buffer = receiveBuffers[wrId];
                data = new WorkRequestData(wrId, buffer);
            }
        }
        return data;
    }

    /**
     * Returns a used Work Request id to a queue of available
     * Work Request ids. The returned id can then be reused by
     * other Work Requests.
     * @param type a {@link PostedRequestType PostedRequestType} type
     * @param workRequestId the Work Request id that is freed.
     */
    public synchronized void freeUpWrID(int workRequestId, PostedRequestType type){
        if(type == PostedRequestType.SEND) {
            freeSendWrIds.enqueue(workRequestId);
            // if the queue was empty, notify any blocked threads
            if(freeSendWrIds.size() == 1){
                synchronized (sendBlockingMonitor){
                    sendBlockingMonitor.notifyAll();
                }
            }
        }else if (type == PostedRequestType.RECEIVE) {
            freeRecvWrIds.enqueue(workRequestId);
            // if the queue was empty, notify any blocked threads
            if(freeRecvWrIds.size() == 1){
                synchronized (recvBlockingMonitor){
                    recvBlockingMonitor.notifyAll();
                }
            }
        }
    }

    /* *************************************************************
     * Manage Work Requests that are sent to the Network Card
     * *************************************************************/

    /**
     * Initializes state required to perform <i>two-sided</i> RDMA
     * operations.
     */
    void initializeTwoSidedOperations() throws IOException {
        TwoSidedSendRequest twoSidedSendRequest;
        TwoSidedRecvRequest twoSidedRecvRequest;
        twoSidedSendSVCs = new SVCPostSend[maxWRs];
        twoSidedRecvSVCs = new SVCPostRecv[maxWRs];
        List<IbvSendWR> sendRequests;
        List<IbvRecvWR> recvRequests;

        for(int i=0; i < maxWRs; i++){
            // We need to store an SVC for one request at a time, so
            // we need new lists each time
            sendRequests = new ArrayList<>(maxWRs);
            recvRequests = new ArrayList<>(maxWRs);

            twoSidedSendRequest = new TwoSidedSendRequest(rdmaEndpointMemoryRegion);
            twoSidedSendRequest.prepareRequest();
            twoSidedSendRequest.setRequestId(i);
            twoSidedSendRequest.setSgeLength(maxBufferSize);
            twoSidedSendRequest.setBufferMemoryAddress(
                    sendBufferAddressesMap.get(sendBuffers[i]));
            sendRequests.add(twoSidedSendRequest.getSendWR());

            twoSidedRecvRequest = new TwoSidedRecvRequest(rdmaEndpointMemoryRegion);
            twoSidedRecvRequest.prepareRequest();
            twoSidedRecvRequest.setRequestId(i);
            twoSidedRecvRequest.setSgeLength(maxBufferSize);
            twoSidedRecvRequest.setBufferMemoryAddress(
                    recvBufferAddressesMap.get(receiveBuffers[i]));
            recvRequests.add(twoSidedRecvRequest.getRecvWR());
            // create and store SVCs
            twoSidedSendSVCs[i] = postSend(sendRequests);
            twoSidedRecvSVCs[i] = postRecv(recvRequests);
        }
    }

    /**
     * Initializes state required to perform <i>one-sided write</i> RDMA
     * operations.
     */
    void initializeOneSidedWriteOperations() throws IOException {
        OneSidedWriteRequest oneSidedWriteRequest;
        oneSidedWriteSVCs = new SVCPostSend[maxWRs];
        // pre-create SVCs
        for(int i=0; i < maxWRs; i++){
            // We need to store an SVC for one request at a time, so
            // we need a new list each time
            List<IbvSendWR> sendRequests = new ArrayList<>(maxWRs);

            oneSidedWriteRequest = new OneSidedWriteRequest(rdmaEndpointMemoryRegion);
            oneSidedWriteRequest.prepareRequest();
            oneSidedWriteRequest.setRequestId(i);
            oneSidedWriteRequest.setSgeLength(maxBufferSize);
            oneSidedWriteRequest.setBufferMemoryAddress(
                    sendBufferAddressesMap.get(sendBuffers[i]));
            sendRequests.add(oneSidedWriteRequest.getSendWR());
            // create and store SVCs
            oneSidedWriteSVCs[i] = postSend(sendRequests);
        }
    }

    /**
     * Initializes state required to perform <i>one-sided read</i> RDMA
     * operations.
     */
    void initializeOneSidedReadOperations() throws IOException {
        OneSidedReadRequest oneSidedReadRequest;
        oneSidedReadSVCs = new SVCPostSend[maxWRs];
        // pre-create SVCs
        for(int i=0; i < maxWRs; i++){
            // We need to store an SVC for one request at a time, so
            // we need a new list each time
            List<IbvSendWR> sendRequests = new ArrayList<>(maxWRs);

            oneSidedReadRequest = new OneSidedReadRequest(rdmaEndpointMemoryRegion);
            oneSidedReadRequest.prepareRequest();
            oneSidedReadRequest.setRequestId(i);
            oneSidedReadRequest.setSgeLength(maxBufferSize);
            oneSidedReadRequest.setBufferMemoryAddress(
                    sendBufferAddressesMap.get(sendBuffers[i]));
            sendRequests.add(oneSidedReadRequest.getSendWR());
            // create and store SVCs
            oneSidedReadSVCs[i] = postSend(sendRequests);
        }
    }

    /**
     * Defines which operations should the endpoint support and makes
     * the required initializations.
     *
     * @param supportedOperationsFlag bit flag that should have the bits set
     *                                according to the {@link WorkRequestTypes
     *                                WorkRequestTypes} class.
     */
    void enableSupportedOperations(int supportedOperationsFlag) throws IOException {
        // support two-sided operations
        if((supportedOperationsFlag & 0b1) == 1 ){
            initializeTwoSidedOperations();
        }
        // support one-sided write
        if((supportedOperationsFlag & 0b10) == 1 ){
            initializeOneSidedWriteOperations();
        }
        // support one-sided read
        if((supportedOperationsFlag & 0b100) == 1 ){
            initializeOneSidedReadOperations();
        }
    }

    /**
     * Associate a {@link CompletionHandler} with a Work Request. Once a
     * Work Completion event is dispatched for this Work Request, the
     * corresponding handler can be called to react to the event.
     *
     * @param type a {@link PostedRequestType PostedRequestType} type
     * @param workRequestId
     * @param handler
     */
    public void registerWrHandler(PostedRequestType type, int workRequestId, CompletionHandler handler){
        if(type == PostedRequestType.SEND){
            sendWorkCompletionHandlers[workRequestId] = handler;
        }else{
            recvWorkCompletionHandlers[workRequestId] = handler;
        }
    }

    /**
     * Post a 'send' RDMA Work Request (WR) to the NIC. A 'send' request can be a
     * <i>two-sided send</i>, a <i>one-sided write</i> or a <i>one-sided read</i> operation.
     * @param workRequestId the id of that the WR will have
     * @param dataLength the length of the data to be transmitted or read. Should not exceed
     *                   the maximum size of communications buffers.
     * @param workRequestType which of the 3 possible operations to use.
     * @param completionHandler the completion handler to register. It's handling method will be
     *                          called when the operation completes.
     */
    public void send(int workRequestId, int dataLength, int workRequestType,
                     CompletionHandler completionHandler) throws IOException, UnsupportedOperationException {
        sendWorkCompletionHandlers[workRequestId] = completionHandler;
        // Send data with two-sided operations
        if(workRequestType == WorkRequestTypes.TWO_SIDED_SIGNALED){
            twoSidedSendSVCs[workRequestId].getWrMod(0).
                    getSgeMod(0).setLength(dataLength);
            twoSidedSendSVCs[workRequestId].execute();
        }else if(workRequestType == WorkRequestTypes.ONE_SIDED_WRITE_SIGNALED){
            oneSidedWriteSVCs[workRequestId].getWrMod(0).
                    getSgeMod(0).setLength(dataLength);
            oneSidedWriteSVCs[workRequestId].execute();
        }else if(workRequestType == WorkRequestTypes.ONE_SIDED_READ_SIGNALED){
            oneSidedReadSVCs[workRequestId].getWrMod(0).
                    getSgeMod(0).setLength(dataLength);
            oneSidedReadSVCs[workRequestId].execute();
        }else{
            throw new UnsupportedOperationException("The request type is invalid.");
        }
    }

    /**
     * Post a 'receive' RDMA Work Request (WR) to the NIC. This is only used for
     * <i>two-sided recv</i> RDMA operations, which need to be posted in order to
     * receive messages sent with <i>two-sided send</i>. A posted <i>two-sided recv</i>
     * allows receiving a single message, so this function must be called every time
     * a message with <i>two-sided send</i> is expected.
     * @param workRequestId the id of that the WR will have
     * @param completionHandler the completion handler to register. It's handling method will be
     *                          called when the operation completes.
     */
    public void recv(int workRequestId, CompletionHandler completionHandler)
                    throws IOException{
        recvWorkCompletionHandlers[workRequestId] = completionHandler;
        twoSidedRecvSVCs[workRequestId].execute();
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
}
