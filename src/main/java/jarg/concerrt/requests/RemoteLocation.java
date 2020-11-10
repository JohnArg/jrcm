package jarg.concerrt.requests;

/**
 * Holds information about a remote memory location, that
 * is needed to perform one-sided RDMA operations.
 */
public class RemoteLocation {
    public long remoteMemoryAddress;
    public int remoteLKey;

    public RemoteLocation(){
        remoteMemoryAddress = 0;
        remoteLKey = 0;
    }

    public RemoteLocation(long address, int Lkey){
        remoteMemoryAddress = address;
        remoteLKey = Lkey;
    }
}
