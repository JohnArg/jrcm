package jarg.rdmarpc.discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * The API of a registry service that will be used by RDMA-capable servers to find discover each other.
 */
public interface RdmaDiscoveryApi {

    /**
     * Registers a server with this registry service. It returns a
     * @param serverAddress the server address to register
     * @return the addresses of the registered servers.
     */
    Set<InetSocketAddress> registerServer(InetSocketAddress serverAddress);

    /**
     * Unregister a server from this registry.
     * @param serverAddress the address of the server to unregister.
     * @return true on success, false otherwise.
     */
    boolean unregisterServer(InetSocketAddress serverAddress);

    /**
     * Get all the registered servers.
     * @return the addresses of the registered servers.
     */
    Set<InetSocketAddress> getRegisteredServers();

    /**
     * Find a registered server's port using its IP address. If no such server was registered,
     * return -1, otherwise return the IP address.
     * @param ipAddress the address of the server to find.
     * @return  the port of the server or -1 if no such server is registered.
     */
    int getServerPortByIp(InetAddress ipAddress);

}
