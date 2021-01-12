package jarg.rdmarpc.networking.dependencies.netrequests.impl;

import com.ibm.disni.verbs.IbvWC;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netbuffers.impl.OneSidedBufferManager;
import jarg.rdmarpc.networking.dependencies.netbuffers.impl.TwoSidedBufferManager;
import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.*;
import static org.junit.jupiter.api.Assertions.*;

import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

@DisplayName("Testing AbstractWorkRequestProxyProvider implemented methods.")
public class AbstractWorkRequestProxyProviderTest {
    private static final int maxWorkRequests = 5;
    private static final int maxBufferSize = 4;
    private static AbstractWorkRequestProxyProvider proxyProvider;

    @BeforeAll
    private static void beforeAll(){
        proxyProvider = new AbstractWorkRequestProxyProvider() {
            @Override
            public WorkRequestProxy getPostSendRequestBlocking(WorkRequestType requestType) {
                return null;
            }

            @Override
            public WorkRequestProxy getPostSendRequestNow(WorkRequestType requestType) {
                return null;
            }

            @Override
            public void releaseWorkRequest(WorkRequestProxy workRequestProxy) {
            }
        };
    }

    private static Stream<Arguments> bufferManagerGenerator(){
        return Stream.of(
                Arguments.of(new TwoSidedBufferManager(maxBufferSize, maxWorkRequests), TWO_SIDED_SEND_SIGNALED),
                Arguments.of(new TwoSidedBufferManager(maxBufferSize, maxWorkRequests), TWO_SIDED_RECV),
                Arguments.of(new OneSidedBufferManager(maxBufferSize, maxWorkRequests), ONE_SIDED_WRITE_SIGNALED),
                Arguments.of(new OneSidedBufferManager(maxBufferSize, maxWorkRequests), ONE_SIDED_READ_SIGNALED)
        );
    }

    @ParameterizedTest
    @Tag("ProxyProvision")
    @DisplayName("Testing correct proxy provision for Work Completion events.")
    @MethodSource("bufferManagerGenerator")
    public void getWorkRequestProxyForWcTest(NetworkBufferManager bufferManager, WorkRequestType requestType){
        // initialize buffers
        bufferManager.allocateCommunicationBuffers();
        proxyProvider.setBufferManager(bufferManager);
        // initialize a WC event - it's the event that supposedly happened
        IbvWC wcEvent = new IbvWC();
        int wrId = 0;
        wcEvent.setWr_id(wrId);
        wcEvent.setOpcode(proxyProvider.getWcOperationCodeForWorkRequest(requestType));
        // get a WR proxy for this event
        WorkRequestProxy proxy = proxyProvider.getWorkRequestProxyForWc(wcEvent);
        // now check if everything is correct
        assertNull(proxy.getEndpoint());
        assertEquals(wrId, proxy.getId());
        assertEquals(requestType, proxy.getWorkRequestType());
        if(requestType.equals(TWO_SIDED_RECV)){
            assertEquals(PostedRequestType.RECEIVE, proxy.getPostType());
        }else{
            assertEquals(PostedRequestType.SEND, proxy.getPostType());
        }
        ByteBuffer expectedBuffer = bufferManager.getWorkRequestBuffer(requestType, wrId);
        ByteBuffer actualBuffer = proxy.getBuffer();
        assertNotNull(expectedBuffer);
        assertNotNull(actualBuffer);
        assertEquals(expectedBuffer, actualBuffer);
    }
}
