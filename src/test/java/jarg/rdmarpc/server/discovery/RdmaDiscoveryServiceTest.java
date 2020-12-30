package jarg.rdmarpc.server.discovery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testing RdmaDiscovery API")
public class RdmaDiscoveryServiceTest {

    @Test
    @Tag("rdma_discovery")
    @DisplayName("Testing new server registration without any pre-registered servers")
    public void registerOnEmptyTest(){
        RdmaDiscoveryService service = new RdmaDiscoveryService();
        String ipToTest = "177.128.3.72";
        int portToTest = 3000;

        Set<InetSocketAddress> previousMembers = service.registerServer(
                new InetSocketAddress(ipToTest, portToTest));
        assertNotNull(previousMembers);
        assertEquals(previousMembers.size() ,0);
        assertEquals(portToTest, service.getServerPortByIp(ipToTest));
    }

    @Test
    @Tag("rdma_discovery")
    @DisplayName("Testing new server registration with pre-registered servers")
    public void registerWithPreExistingTest(){
        RdmaDiscoveryService service = new RdmaDiscoveryService();
        Set<InetSocketAddress> initialMembers = new HashSet<>();

        initialMembers.add(new InetSocketAddress("177.128.3.73", 3053));
        initialMembers.add(new InetSocketAddress("177.128.3.74", 3054));
        initialMembers.add(new InetSocketAddress("177.128.3.75", 3055));

        Set<InetSocketAddress> expectedMembers = new HashSet<>();
        for(InetSocketAddress address : initialMembers){
            Set<InetSocketAddress> previousMembers = service.registerServer(address);
            assertNotNull(previousMembers);
            assertTrue(expectedMembers.containsAll(previousMembers));
            assertEquals(address.getPort(), service.getServerPortByIp(address.getAddress().getHostAddress()));
            expectedMembers.add(address);
        }
    }

    @Test
    @Tag("rdma_discovery")
    @DisplayName("Testing unregistering a server")
    public void unregisterServerTest(){
        RdmaDiscoveryService service = new RdmaDiscoveryService();
        String ipToTest = "177.128.3.72";
        int portToTest = 3000;
        InetSocketAddress serverAddress = new InetSocketAddress(ipToTest, portToTest);

        // test with empty set
        assertFalse(service.unregisterServer(new InetSocketAddress(ipToTest, portToTest)));
        // test after adding the server
        service.registerServer(serverAddress);
        assertTrue(service.unregisterServer(serverAddress));
    }

    @Test
    @Tag("rdma_discovery")
    @DisplayName("Testing getting all registered servers.")
    public void getRegisteredServersTest(){
        RdmaDiscoveryService service = new RdmaDiscoveryService();
        Set<InetSocketAddress> initialMembers = new HashSet<>();

        // assert empty set
        Set<InetSocketAddress> actualMembers = service.getRegisteredServers();
        assertTrue(initialMembers.containsAll(actualMembers));

        initialMembers.add(new InetSocketAddress("177.128.3.73", 3053));
        initialMembers.add(new InetSocketAddress("177.128.3.74", 3054));
        initialMembers.add(new InetSocketAddress("177.128.3.75", 3055));

        for(InetSocketAddress address : initialMembers){
            service.registerServer(address);
        }

        // assert that everyone that was added is returned
        assertTrue(initialMembers.containsAll(actualMembers));
    }

    @Test
    @Tag("rdma_discovery")
    @DisplayName("Testing getting correct server ports when searching by IP")
    public void getServerPortByIpTest(){
        RdmaDiscoveryService service = new RdmaDiscoveryService();
        String ipToTest = "177.128.3.72";
        int portToTest = 3000;
        InetSocketAddress serverAddress = new InetSocketAddress(ipToTest, portToTest);

        // test with empty set
        assertEquals(-1, service.getServerPortByIp(ipToTest));
        // test after adding the server
        service.registerServer(serverAddress);
        assertEquals(portToTest, service.getServerPortByIp(ipToTest));
    }
}
