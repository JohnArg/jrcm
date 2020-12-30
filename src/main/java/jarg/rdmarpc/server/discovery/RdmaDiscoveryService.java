package jarg.rdmarpc.server.discovery;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * A registry service that will be used by RDMA-capable servers to discover each other.
 */
public class RdmaDiscoveryService implements RdmaDiscoveryAPI{
    private final Set<InetSocketAddress> registeredServers;

    public RdmaDiscoveryService(){
        registeredServers = new HashSet<>();
    }

    @Override
    public synchronized Set<InetSocketAddress> registerServer(InetSocketAddress serverAddress) {
        HashSet<InetSocketAddress> existingMembers;
        existingMembers = new HashSet<>(registeredServers);
        registeredServers.add(serverAddress);
        return existingMembers;
    }

    @Override
    public synchronized boolean unregisterServer(InetSocketAddress serverAddress) {
        return registeredServers.remove(serverAddress);
    }

    @Override
    public synchronized Set<InetSocketAddress> getRegisteredServers() {
        return new HashSet<>(registeredServers);
    }

    @Override
    public synchronized int getServerPortByIp(String ipAddress) {
        for(InetSocketAddress socketAddress : registeredServers){
            if(socketAddress.getAddress().getHostAddress().equals(ipAddress)){
                return socketAddress.getPort();
            }
        }
        return -1;
    }

}
