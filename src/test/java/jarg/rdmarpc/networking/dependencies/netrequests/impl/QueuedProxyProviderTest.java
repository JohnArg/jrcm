package jarg.rdmarpc.networking.dependencies.netrequests.impl;

import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netbuffers.impl.OneSidedBufferManager;
import jarg.rdmarpc.networking.dependencies.netbuffers.impl.TwoSidedBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import static jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType.*;
import static org.junit.jupiter.api.Assertions.*;

import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@DisplayName("Tests for QueuedProxyProvider")
public class QueuedProxyProviderTest {

    private static final int maxBufferSize = 5;
    private static final int maxWorkRequests = 3;

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
    @DisplayName("Test getting a Work Request proxy with null parameters")
    @MethodSource("bufferManagerGenerator")
    public void getPostSendRequestNowNullTest(NetworkBufferManager bufferManager, WorkRequestType requestType){
        // test with always 0-sized queue
        QueuedProxyProvider emptyProvider = new QueuedProxyProvider(0);
        assertNull(emptyProvider.getPostSendRequestBlocking(requestType));
        assertNull(emptyProvider.getPostSendRequestNow(requestType));
        assertDoesNotThrow(()->{emptyProvider.releaseWorkRequest(new WorkRequestProxy(0, null,
                null, null, null));});

        // test with non-0-sized queue --------------------------
        QueuedProxyProvider proxyProvider = new QueuedProxyProvider(maxWorkRequests);
        // test with null buffer manager
        assertNull(proxyProvider.getPostSendRequestBlocking(requestType));
        assertNull(proxyProvider.getPostSendRequestNow(requestType));
        assertDoesNotThrow(()->{emptyProvider.releaseWorkRequest(new WorkRequestProxy(0, null,
                null, null, null));});
        // test with buffer manager
        proxyProvider.setBufferManager(bufferManager);
        // try with null parameter
        assertNull(proxyProvider.getPostSendRequestBlocking(null));
        assertNull(proxyProvider.getPostSendRequestNow(null));
        assertDoesNotThrow(()->{emptyProvider.releaseWorkRequest(new WorkRequestProxy(0, null,
                null, null, null));});
    }

    @ParameterizedTest
    @Tag("ProxyProvision")
    @DisplayName("Test getting a Work Request proxy with non-null parameters")
    @MethodSource("bufferManagerGenerator")
    public void getPostSendRequestNowTest(NetworkBufferManager bufferManager, WorkRequestType requestType){
        QueuedProxyProvider proxyProvider = new QueuedProxyProvider(maxWorkRequests);
        proxyProvider.setBufferManager(bufferManager);
        bufferManager.allocateCommunicationBuffers();
        // Test non-blocking calls to avoid blocking on empty queue
        WorkRequestProxy proxy = proxyProvider.getPostSendRequestNow(requestType);
        if(requestType.equals(TWO_SIDED_RECV)){     // we shouldn't get something here
            assertNull(proxy);
        }else{
            // we have already got a request above, that's why the loop begins with 1
            for(int wrId=1; wrId < maxWorkRequests; wrId++){
                // check that the request we got is correct
                proxy = proxyProvider.getPostSendRequestNow(requestType);
                assertNotNull(proxy);
                assertEquals(wrId, proxy.getId());
                ByteBuffer expectedBuffer = bufferManager.getWorkRequestBuffer(requestType, wrId);
                ByteBuffer actualBuffer = proxy.getBuffer();
                assertNotNull(expectedBuffer);
                assertNotNull(actualBuffer);
                assertEquals(expectedBuffer, actualBuffer);
                assertNull(proxy.getEndpoint());
                assertEquals(requestType, proxy.getWorkRequestType());
                assertEquals(PostedRequestType.SEND, proxy.getPostType());
            }
            assertNull(proxyProvider.getPostSendRequestNow(requestType));
            proxyProvider.releaseWorkRequest(proxy);
        }
    }

    @ParameterizedTest
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("ProxyProvision")
    @DisplayName("Test getting a Work Request proxy with blocking parameters")
    @MethodSource("bufferManagerGenerator")
    public void getPostSendRequestBlockingTest(NetworkBufferManager bufferManager, WorkRequestType requestType){
        if(requestType.equals(TWO_SIDED_RECV)){ // this case is tested elsewhere
            return;
        }
        // test with only one element
        QueuedProxyProvider proxyProvider = new QueuedProxyProvider(1);
        proxyProvider.setBufferManager(bufferManager);
        bufferManager.allocateCommunicationBuffers();
        for(int wrId=0; wrId<3; wrId++){
            WorkRequestProxy proxy = proxyProvider.getPostSendRequestBlocking(requestType);
            assertNotNull(proxy);
            assertEquals(0, proxy.getId());
            ByteBuffer expectedBuffer = bufferManager.getWorkRequestBuffer(requestType, 0);
            ByteBuffer actualBuffer = proxy.getBuffer();
            assertNotNull(expectedBuffer);
            assertNotNull(actualBuffer);
            assertEquals(expectedBuffer, actualBuffer);
            assertNull(proxy.getEndpoint());
            assertEquals(requestType, proxy.getWorkRequestType());
            assertEquals(PostedRequestType.SEND, proxy.getPostType());
            proxyProvider.releaseWorkRequest(proxy);
        }
        // test with multiple elements
        proxyProvider = new QueuedProxyProvider(maxWorkRequests);
        proxyProvider.setBufferManager(bufferManager);
        WorkRequestProxy proxy = null;
        for(int wrId=0; wrId<maxWorkRequests; wrId++) {
            proxy = proxyProvider.getPostSendRequestBlocking(requestType);
            assertNotNull(proxy);
            assertEquals(wrId, proxy.getId());
            ByteBuffer expectedBuffer = bufferManager.getWorkRequestBuffer(requestType, wrId);
            ByteBuffer actualBuffer = proxy.getBuffer();
            assertNotNull(expectedBuffer);
            assertNotNull(actualBuffer);
            assertEquals(expectedBuffer, actualBuffer);
            assertNull(proxy.getEndpoint());
            assertEquals(requestType, proxy.getWorkRequestType());
            assertEquals(PostedRequestType.SEND, proxy.getPostType());
        }
        proxyProvider.releaseWorkRequest(proxy);
    }


}
