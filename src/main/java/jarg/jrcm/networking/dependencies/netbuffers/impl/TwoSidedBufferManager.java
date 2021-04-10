package jarg.jrcm.networking.dependencies.netbuffers.impl;

import jarg.jrcm.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;
import java.nio.ByteBuffer;


/**
 * Manages the network data buffers for <i>two-sided</i> RDMA operations.
 */
public class TwoSidedBufferManager implements NetworkBufferManager {

    private ByteBuffer[] sendBuffers;                   // contain data to be sent
    private ByteBuffer[] receiveBuffers;                // contain data to be received
    private long[] sendBufferAddresses;                 // memory addresses of send buffers
    private long[] receiveBufferAddresses;                 // memory addresses of recv buffers
    private ByteBuffer registeredMemoryBuffer;          // large memory block for communications
    private int maxBufferSize;                          // the maximum size of communication buffers
    private int maxWorkRequests;                        // the maximum number of work requests for
                                                            // either postSend or postRecv (same number)


    public TwoSidedBufferManager(int maxBufferSize, int maxWorkRequests) {
        this.maxBufferSize = maxBufferSize;
        this.maxWorkRequests = maxWorkRequests;
        sendBuffers = new ByteBuffer[maxWorkRequests];
        sendBufferAddresses = new long[maxWorkRequests];
        receiveBuffers = new ByteBuffer[maxWorkRequests];
        receiveBufferAddresses = new long[maxWorkRequests];
    }

    @Override
    public void allocateCommunicationBuffers() {
         /* Both send and receive data buffers will be views of sub-parts of
         a large buffer. The reason is to register only one memory region (the large buffer) to
         the Network Card (NIC). This saves space in Memory Translation and Memory Protection
         tables maintained in the Network Card, which are cached by the NIC.
         So the fewer the entries of the aforementioned tables, the more data can the NIC's
         cache store, leading to more scalable performance. */
        int bufferArrayBytes = maxBufferSize * maxWorkRequests * 2;
        registeredMemoryBuffer = ByteBuffer.allocateDirect(bufferArrayBytes);
        // give equal space to send and receive buffers
        int currentLimit = maxBufferSize;
        for(int i=0; i < maxWorkRequests; i++){
            registeredMemoryBuffer.limit(currentLimit);
            sendBuffers[i] = registeredMemoryBuffer.slice();
            registeredMemoryBuffer.position(currentLimit);
            currentLimit += maxBufferSize;
            // keep the memory address of the buffer for communications
            long address = ((sun.nio.ch.DirectBuffer) sendBuffers[i])
                    .address();
            sendBufferAddresses[i] = address;
        }
        for(int i=0; i < maxWorkRequests; i++){
            registeredMemoryBuffer.limit(currentLimit);
            receiveBuffers[i] = registeredMemoryBuffer.slice();
            registeredMemoryBuffer.position(currentLimit);
            currentLimit += maxBufferSize;
            // keep the memory address of the buffer for communications
            long address = ((sun.nio.ch.DirectBuffer) receiveBuffers[i])
                    .address();
            receiveBufferAddresses[i] = address;
        }
    }

    @Override
    public ByteBuffer getBufferToRegister(){
        return registeredMemoryBuffer;
    }

    @Override
    public ByteBuffer getWorkRequestBuffer(WorkRequestType requestType, int workRequestId) {
        if((workRequestId < 0) || (workRequestId >= maxWorkRequests)){
            return null;
        }
        switch (requestType) {
            case TWO_SIDED_SEND_SIGNALED:
                return sendBuffers[workRequestId];
            case TWO_SIDED_RECV:
                return receiveBuffers[workRequestId];
        }
        return null;
    }

    @Override
    public long getWorkRequestBufferAddress(WorkRequestType requestType, int workRequestId) {
        if((workRequestId < 0) || (workRequestId >= maxWorkRequests)){
            return -1;
        }
        switch (requestType) {
            case TWO_SIDED_SEND_SIGNALED:
                return sendBufferAddresses[workRequestId];
            case TWO_SIDED_RECV:
                return receiveBufferAddresses[workRequestId];
        }
        return -1;
    }

}
