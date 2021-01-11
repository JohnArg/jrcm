package jarg.rdmarpc.networking.dependencies.netrequests;

import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

public class WorkRequestProxy {
    private int id;
    private PostedRequestType postType;
    private WorkRequestType workRequestType;
    private ByteBuffer buffer;
    private RdmaCommunicator endpoint;
    private boolean isValid;                // used as a "guard" to prevent accessing data when invalidated

    public WorkRequestProxy(int id, PostedRequestType postType,
                            WorkRequestType workRequestType, ByteBuffer buffer, RdmaCommunicator endpoint) {
        this.id = id;
        this.postType = postType;
        this.workRequestType = workRequestType;
        this.buffer = buffer;
        this.endpoint = endpoint;
        this.isValid = true;
    }


    public int getId() {
        if(!isValid){
            return -1;
        }
        return id;
    }

    public ByteBuffer getBuffer() {
        if(!isValid){
            return null;
        }
        return buffer;
    }

    public RdmaCommunicator getEndpoint() {
        if(!isValid){
            return null;
        }
        return endpoint;
    }

    public PostedRequestType getPostType() {
        if(!isValid){
            return null;
        }
        return postType;
    }

    public WorkRequestType getWorkRequestType() {
        if(!isValid){
            return null;
        }
        return workRequestType;
    }

    public boolean isValid() {
        return isValid;
    }

    /**
     * Releases the Work Request associated with this proxy, so that it
     * can be reused. After this operation, the proxy becomes invalid
     * and its internal data should not be used.
     */
    public void releaseWorkRequest(){
        isValid = false;    // invalidate this object
        endpoint.getWorkRequestProxyProvider().releaseWorkRequest(this);
    }
}
