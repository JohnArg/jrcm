package jarg.rdmarpc.networking.dependencies.netbuffers.impl;

import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Manages the network data buffers for <i>one-sided</i> RDMA operations.
 */
public class OneSidedBufferManager implements NetworkBufferManager {

    private ByteBuffer[] networkBuffers;                // contain data to be written/read over the network
                                                            // since both one-sided WRITE and READ are
                                                            // postSend-type operations, they share these buffers
    private long[] networkBufferAddresses;              // memory addresses of data buffers
    private ByteBuffer registeredMemoryBuffer;          // large memory block for communications
    private int maxBufferSize;                          // the maximum size of communication buffers
    private int maxWorkRequests;                        // the maximum number of work requests for
                                                            // either postSend or postRecv (same number)


    public OneSidedBufferManager(int maxBufferSize, int maxWorkRequests) {
        this.maxBufferSize = maxBufferSize;
        this.maxWorkRequests = maxWorkRequests;
        networkBuffers = new ByteBuffer[maxWorkRequests];
        networkBufferAddresses = new long[maxWorkRequests];
    }

    @Override
    public void allocateCommunicationBuffers() {
        int bufferArrayBytes = maxBufferSize * maxWorkRequests;
        registeredMemoryBuffer = ByteBuffer.allocateDirect(bufferArrayBytes);
        int currentLimit = maxBufferSize;
        for(int i=0; i < maxWorkRequests; i++){
            registeredMemoryBuffer.limit(currentLimit);
            networkBuffers[i] = registeredMemoryBuffer.slice();
            registeredMemoryBuffer.position(currentLimit);
            currentLimit += maxBufferSize;
            // keep the memory address of the buffer for communications
            long address = ((sun.nio.ch.DirectBuffer) networkBuffers[i])
                    .address();
            networkBufferAddresses[i] = address;
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
        return networkBuffers[workRequestId];
    }

    @Override
    public long getWorkRequestBufferAddress(WorkRequestType requestType, int workRequestId) {
        if((workRequestId < 0) || (workRequestId >= maxWorkRequests)){
            return -1;
        }
        return networkBufferAddresses[workRequestId];
    }
}
