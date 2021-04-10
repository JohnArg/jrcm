package jarg.jrcm.networking.dependencies.netbuffers.impl;

import jarg.jrcm.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testing Two-sided Communication Buffer Management")
public class BufferManagerTest {

    private static final int maxBufferSize = 5;
    private static final int maxWorkRequests = 5;

    private static Stream<Arguments> bufferManagerGenerator(){
        ArrayList<WorkRequestType> twoSidedTypes = new ArrayList<>(2);
        ArrayList<WorkRequestType> oneSidedTypes = new ArrayList<>(2);

        twoSidedTypes.add(TWO_SIDED_SEND_SIGNALED);
        twoSidedTypes.add(TWO_SIDED_RECV);
        oneSidedTypes.add(ONE_SIDED_WRITE_SIGNALED);
        oneSidedTypes.add(ONE_SIDED_READ_SIGNALED);

        return Stream.of(
                Arguments.of(new TwoSidedBufferManager(maxBufferSize, maxWorkRequests), twoSidedTypes),
                Arguments.of(new OneSidedBufferManager(maxBufferSize, maxWorkRequests), oneSidedTypes));
    }

    @ParameterizedTest
    @Tag("MemoryAllocations")
    @DisplayName("Testing correct address assignment to allocated network request buffers")
    @MethodSource("bufferManagerGenerator")
    public void allocatedAddressesOffsetsTest(NetworkBufferManager bufferManager,
                                              List<WorkRequestType> workRequestTypes){
        // allocate buffers
        bufferManager.allocateCommunicationBuffers();
        // check correct offsets
        for(WorkRequestType requestType : workRequestTypes){
            long prevAddress = bufferManager.getWorkRequestBufferAddress(requestType, 0);
            for(int wrId=1; wrId < maxWorkRequests; wrId++){
                long address = bufferManager.getWorkRequestBufferAddress(requestType, wrId);
                long expectedAddress = prevAddress + maxBufferSize;
                assertEquals(expectedAddress, address);
                prevAddress += maxBufferSize;
            }
        }
    }

    @ParameterizedTest
    @Tag("MemoryAllocations")
    @DisplayName("Testing that stored buffer addresses are correct.")
    @MethodSource("bufferManagerGenerator")
    public void correctBufferAddressAssociationTest(NetworkBufferManager bufferManager,
                                                    List<WorkRequestType> workRequestTypes){
        // allocate buffers
        bufferManager.allocateCommunicationBuffers();
        // check correct offsets
        for(WorkRequestType requestType : workRequestTypes){
            ByteBuffer requestBuffer = bufferManager.getWorkRequestBuffer(requestType, 0);
            long bufferAddress = ((sun.nio.ch.DirectBuffer) requestBuffer).address();
            long storedAddress = bufferManager.getWorkRequestBufferAddress(requestType, 0);
            assertEquals(bufferAddress, storedAddress);
        }
    }

}
