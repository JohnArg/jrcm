package jarg.jrcm.rpc.serialization;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.rpc.exception.RpcDataSerializationException;

/**
 * Abstract class that associates a {@link WorkRequestProxy} with an {@link RpcDataSerializer}.
 * The WorkRequestProxy's ByteBuffer should be used for data serialization and deserialization.
 */
public abstract class AbstractDataSerializer implements RpcDataSerializer{

    protected WorkRequestProxy workRequestProxy;

    public AbstractDataSerializer(){};

    public AbstractDataSerializer(WorkRequestProxy workRequestProxy){
        this.workRequestProxy = workRequestProxy;
    }

    public WorkRequestProxy getWorkRequestProxy() {
        return workRequestProxy;
    }

    public void setWorkRequestProxy(WorkRequestProxy workRequestProxy) {
        this.workRequestProxy = workRequestProxy;
    }

    /**
     * Throws an exception if the two serial versions are not compatible.
     * @param remoteSerialVersion   the serial version provided by the remote side.
     * @param localSerialVersion    the local serial version.
     * @throws RpcDataSerializationException thrown if the remote serial version is not compatible.
     */
    public void throwIfSerialVersionInvalid(long localSerialVersion, long remoteSerialVersion)
                                                            throws RpcDataSerializationException{
        if(remoteSerialVersion != localSerialVersion){
            throw new RpcDataSerializationException("Serial versions do not match. Local version : "+
                    localSerialVersion + ", remote version : " + remoteSerialVersion + ".");
        }
    }
}
