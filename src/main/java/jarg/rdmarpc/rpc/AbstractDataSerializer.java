package jarg.rdmarpc.rpc;

import jarg.rdmarpc.rdma.connections.WorkRequestData;
import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;

/**
 * Abstract class that implements {@link RpcDataSerializer}, but only handles
 * getting and setting a {@link WorkRequestData}.
 */
public abstract class AbstractDataSerializer implements RpcDataSerializer{

    private WorkRequestData workRequestData;

    public AbstractDataSerializer(){}


    public WorkRequestData getWorkRequestData() {
        return workRequestData;
    }

    public void setWorkRequestData(WorkRequestData workRequestData) {
        this.workRequestData = workRequestData;
    }

    /**
     * Throws an exception if the two serial versions are not compatible.
     * @param remoteSerialVersion
     * @param localSerialVersion
     * @throws RpcDataSerializationException
     */
    public void throwIfSerialVersionInvalid(long localSerialVersion, long remoteSerialVersion)
                                                            throws RpcDataSerializationException{
        if(remoteSerialVersion != localSerialVersion){
            throw new RpcDataSerializationException("Serial versions do not match. Local version : "+
                    localSerialVersion + ", remote version : " + remoteSerialVersion + ".");
        }
    }
}
