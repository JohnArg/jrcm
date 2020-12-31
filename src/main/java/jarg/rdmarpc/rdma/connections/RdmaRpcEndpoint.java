package jarg.rdmarpc.rdma.connections;

import com.ibm.disni.RdmaActiveEndpoint;
import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.verbs.*;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jarg.rdmarpc.rdma.netrequests.*;
import jarg.rdmarpc.rdma.netrequests.postrecv.TwoSidedRecvRequest;
import jarg.rdmarpc.rdma.netrequests.postsend.TwoSidedSendRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This endpoint establishes a channel of RDMA communications with remote machines.
 * The endpoint has the following jobs:
 * <ul>
 *     <li>It transmits and receives data using the RDMA technology.</li>
 *     <li>It performs all necessary preparations before staring RDMA communications,
 *          including memory allocations, memory registration to the Network Interface Card
 *          (NIC) and SVC creation <i>(see IBM's jverbs)</i>.
 *     </li>
 *     <li>In order to transmit and receive data, RDMA Work Requests (WRs) are sent to the
 *          NIC. The endpoint creates, stores, submits to the NIC and manages the lifecycle
 *          of such WRs. Therefore it also has to be able to identify the WRs and associate
 *          them with events, data buffers and SVCs.
 *     </li>
 * </ul>
 */
public class RdmaRpcEndpoint extends RdmaActiveEndpoint {
    /*
    An RdmaEndpoint is associated with a Queue Pair (QP), which is a pair of internal queues
    that accept work requests (WR) for the network card (NIC). One of these queues stores
    requests submitted through a postSend() function and the other queue stores requests
    submitted through a postRecv() function. Therefore the endpoints must manage requests
    towards two separate queues.
     */

    // Connection parameters ---------------------------------------------------
    int maxWRs;                                 // max Work Requests supported - both queues of
                                                    // a QP get the same size
    int maxBufferSize;                          // max communications buffer size
    // Work Requests ------------------------------------------------------------
    IntArrayFIFOQueue freePostSendWrIds;        // available Work Request ids for send requests
    // Request Buffer Management ------------------------------------------------
    ByteBuffer[] sendBuffers;                   // contain data to be sent
    ByteBuffer[] receiveBuffers;                // contain data to be received
    long[] sendBufferAddresses;                 // addresses of send buffers
    long[] recvBufferAddresses;                 // addresses of recv buffers
    ByteBuffer rdmaEndpointMemoryBuffer;        // large memory block for communications
                                                    // will be registered to network card
    IbvMr rdmaEndpointMemoryRegion;             // the above block's memory region

    // Supported operations -----------------------------------------------------
    int supportedOperationsFlag;                // which WorkRequestTypes will be used
    boolean twoSidedReceiveEnabled;             // whether we use two-sided recv operations
    // Keep stored SVCs (Stateful Verb Calls => see jVerbs from IBM) for each request type here.
    // These SVCs can be reused when posting Work Requests to the NIC. This is faster
    // than creating new ones everytime.
    SVCPostSend[] twoSidedSendSVCs;
    SVCPostSend[] oneSidedWriteSVCs;
    SVCPostSend[] oneSidedReadSVCs;
    SVCPostRecv[] twoSidedRecvSVCs;
    WorkCompletionHandler completionHandler;    // will handle completion events


    public RdmaRpcEndpoint(RdmaActiveEndpointGroup<? extends RdmaRpcEndpoint> group,
                           RdmaCmId idPriv, boolean serverSide,
                           int maxBufferSize, int maxWRs, int supportedOperationsFlag,
                           WorkCompletionHandler completionHandler)
                            throws IOException {

        super(group, idPriv, serverSide);

        this.maxWRs = maxWRs;
        this.maxBufferSize = maxBufferSize;
        sendBuffers = new ByteBuffer[maxWRs];
        twoSidedReceiveEnabled = false;
        freePostSendWrIds = new IntArrayFIFOQueue(maxWRs);
        for(int i=0; i<maxWRs; i++){
            freePostSendWrIds.enqueue(i);
        }
        sendBufferAddresses = new long[maxWRs];
        this.supportedOperationsFlag = supportedOperationsFlag;
        this.completionHandler = completionHandler;
        // if we are going to use two-sided operations, we need extra initializations
        if((supportedOperationsFlag & 0b1) == 1){
            receiveBuffers = new ByteBuffer[maxWRs];
            recvBufferAddresses = new long[maxWRs];
            twoSidedReceiveEnabled = true;
        }
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
     * @throws IOException failing to register memory will throw this Exception.
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
                sendBufferAddresses[i] = address;
            }
            for(int i=0; i < maxWRs; i++){
                rdmaEndpointMemoryBuffer.limit(currentLimit);
                receiveBuffers[i] = rdmaEndpointMemoryBuffer.slice();
                rdmaEndpointMemoryBuffer.position(currentLimit);
                currentLimit += maxBufferSize;
                // keep the memory address of the buffer for communications
                long address = ((sun.nio.ch.DirectBuffer) receiveBuffers[i])
                        .address();
                recvBufferAddresses[i] = address;
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
                sendBufferAddresses[i] = address;
            }
        }
        // Register the large allocated memory to the Network Card too
        rdmaEndpointMemoryRegion = registerMemory(rdmaEndpointMemoryBuffer)
                                    .execute().free().getMr();
        // prepare objects for the supported operations
        enableSupportedOperations(supportedOperationsFlag);
    }

    /**
     * When a Work Completion event is ready, call the internal {@link WorkCompletionHandler}'s
     * method that corresponds to this event type.
     * @param wc the Work Completion event dispatched to this Endpoint.
     * @throws IOException
     */
    @Override
    public void dispatchCqEvent(IbvWC wc) throws IOException {
        // Compare to opcodes from com.ibm.disni.verbs.IbvWC.IbvWcOpcode
        // Then call the appropriate handlers for the events and free the Work Request id for reuse

        if (wc.getStatus() == 5){
            throw new IOException("Unkown operation! wc.status " + wc.getStatus());
        } else if (wc.getStatus() != 0){
            throw new IOException("Faulty operation! wc.status " + wc.getStatus());
        }else{
            int workRequestId = (int) wc.getWr_id();
            // Completion for two-side receive operation
            if(wc.getOpcode() == 128){
                completionHandler.handleTwoSidedReceive(wc, this,
                        receiveBuffers[(int) wc.getWr_id()]);
            }
            // Completion for two-side send operation
            else if(wc.getOpcode() == 0){
                completionHandler.handleTwoSidedSend(wc, this);
            }
            // Completion for one-sided write operation
            else if(wc.getOpcode() == 1){
                completionHandler.handleOneSidedWrite(wc, this);
            }
            // Completion for one-sided read operation
            else if(wc.getOpcode() == 2){
                completionHandler.handleOneSidedRead(wc, this);
            }
        }
    }

    /**
     * Clean up resources when closing this Endpoint.
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void close() throws IOException, InterruptedException {
        super.close();
        // deregister the memory for communications
        deregisterMemory(rdmaEndpointMemoryRegion);
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
                    sendBufferAddresses[i]);
            sendRequests.add(twoSidedSendRequest.getSendWR());

            twoSidedRecvRequest = new TwoSidedRecvRequest(rdmaEndpointMemoryRegion);
            twoSidedRecvRequest.prepareRequest();
            twoSidedRecvRequest.setRequestId(i);
            twoSidedRecvRequest.setSgeLength(maxBufferSize);
            twoSidedRecvRequest.setBufferMemoryAddress(
                    recvBufferAddresses[i]);
            recvRequests.add(twoSidedRecvRequest.getRecvWR());
            // create and store SVCs
            twoSidedSendSVCs[i] = postSend(sendRequests);
            twoSidedRecvSVCs[i] = postRecv(recvRequests);

            // post receive operations now that the endpoint is created,
            // so that it can receive messages immediately after creation.
            twoSidedRecvSVCs[i].execute();
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
                    sendBufferAddresses[i]);
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
                    sendBufferAddresses[i]);
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
        if((supportedOperationsFlag & 0b10) == 2 ){
            initializeOneSidedWriteOperations();
        }
        // support one-sided read
        if((supportedOperationsFlag & 0b100) == 4 ){
            initializeOneSidedReadOperations();
        }
    }

    /**
     * Post a 'send' RDMA Work Request (WR) to the NIC. A 'send' request can be a
     * <i>two-sided send</i>, a <i>one-sided write</i> or a <i>one-sided read</i> operation.
     * @param workRequestId the id of that the WR will have
     * @param dataLength the length of the data to be transmitted or read. Should not exceed
     *                   the maximum size of communications buffers.
     * @param workRequestType which of the 3 possible operations to use.
     */
    public void send(int workRequestId, int dataLength, int workRequestType)
            throws IOException, UnsupportedOperationException {
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
     */
    public void recv(int workRequestId) throws IOException{
        twoSidedRecvSVCs[workRequestId].execute();
    }

    /* *************************************************************
     * Manage Work Request Ids
     * *************************************************************/

    /**
     * Used before posting a 'send' operation to the NIC.
     * It will return a {@link WorkRequestData WorkRequestData} object
     * that contains a Work Request id and the corresponding buffer.
     * If there is no available Work Request id at the moment, the
     * method will block until there is one.
     *
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request.
     */
    public WorkRequestData getWorkRequestBlocking(){
        WorkRequestData data = null;
        int wrId = -1;
        // if there are no available Work Request ids, block until there are
        synchronized (freePostSendWrIds){
            while(freePostSendWrIds.isEmpty()){
                try {
                    freePostSendWrIds.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            wrId = freePostSendWrIds.dequeueInt();
        }
        ByteBuffer buffer = sendBuffers[wrId];
        data = new WorkRequestData(wrId, PostedRequestType.SEND, buffer, this);
        return data;
    }

    /**
     * Used before posting a 'send' operation to the NIC.
     * It will return a {@link WorkRequestData WorkRequestData} object
     * that contains a Work Request id and the corresponding buffer.
     * If there is no available Work Request id at the moment, the
     * returned object will be null.
     *
     * @return the {@link WorkRequestData WorkRequestData} object that
     * contains information about the Work Request or null on failure.
     */
    public WorkRequestData getWorkRequestNow(){
        WorkRequestData data = null;
        int wrId = -1;

        synchronized (freePostSendWrIds){
            // try to get a WR id
            if (!freePostSendWrIds.isEmpty()) {
                wrId = freePostSendWrIds.dequeueInt();
            }
        }
        // if a WR id was available
        ByteBuffer buffer;
        if (wrId > -1) {
            buffer = sendBuffers[wrId];
            data = new WorkRequestData(wrId, PostedRequestType.SEND, buffer, this);
        }
        return data;
    }

    /**
     * Returns a used Work Request id to a queue of available
     * Work Request ids. The returned id can then be reused by
     * other Work Requests.
     * @param workRequestData the Work Request data that specifies which
     *                        request to free up.
     */
    public void freeUpWrID(WorkRequestData workRequestData){
        if(workRequestData.getRequestType() == PostedRequestType.SEND) {
            synchronized (freePostSendWrIds){
                freePostSendWrIds.enqueue(workRequestData.getId());
                // if the queue was empty, notify any blocked threads
                if(freePostSendWrIds.size() == 1){
                    freePostSendWrIds.notifyAll();
                }
            }
        }else if (workRequestData.getRequestType() == PostedRequestType.RECEIVE) {
            // now that this WR id is free for reuse, we can repost
            // that 'receive' immediately to accept new data
            try {
                twoSidedRecvSVCs[workRequestData.getId()].execute();
            } catch (IOException e) {
                e.printStackTrace();
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

    public ByteBuffer getReceiveBuffer(IbvWC wc){
        int wrId = (int) wc.getWr_id();
        return receiveBuffers[wrId];
    }
}
