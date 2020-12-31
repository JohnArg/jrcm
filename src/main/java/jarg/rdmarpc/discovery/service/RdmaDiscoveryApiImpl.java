package jarg.rdmarpc.discovery.service;

import jarg.rdmarpc.discovery.RdmaDiscoveryApi;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * A registry service that will be used by RDMA-capable servers to discover each other.
 */
public class RdmaDiscoveryApiImpl implements RdmaDiscoveryApi {
    private final Set<InetSocketAddress> registeredServers;

    public RdmaDiscoveryApiImpl(){
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
    public synchronized int getServerPortByIp(InetAddress ipAddress) {
        for(InetSocketAddress socketAddress : registeredServers){
            if(socketAddress.getAddress().equals(ipAddress)){
                return socketAddress.getPort();
            }
        }
        return -1;
    }

}
