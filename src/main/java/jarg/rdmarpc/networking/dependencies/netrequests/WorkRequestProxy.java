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

    public WorkRequestProxy(){}

    public WorkRequestProxy(int id, PostedRequestType postType,
                            WorkRequestType workRequestType, ByteBuffer buffer, RdmaCommunicator endpoint) {
        this.id = id;
        this.postType = postType;
        this.workRequestType = workRequestType;
        this.buffer = buffer;
        this.endpoint = endpoint;
    }


    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public RdmaCommunicator getEndpoint() {
        return endpoint;
    }

    public PostedRequestType getPostType() {
        return postType;
    }

    public WorkRequestType getWorkRequestType() {
        return workRequestType;
    }

    public WorkRequestProxy setId(int id) {
        this.id = id;
        return this;
    }

    public WorkRequestProxy setPostType(PostedRequestType postType) {
        this.postType = postType;
        return this;
    }

    public WorkRequestProxy setWorkRequestType(WorkRequestType workRequestType) {
        this.workRequestType = workRequestType;
        return this;
    }

    public WorkRequestProxy setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public WorkRequestProxy setEndpoint(RdmaCommunicator endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Releases the Work Request associated with this proxy, so that it
     * can be reused.
     */
    public void releaseWorkRequest(){
        endpoint.getWorkRequestProxyProvider().releaseWorkRequest(this);
    }

    /**
     * Convenience method for posting this Work Request to the NIC.
     */
    public void post(){
        endpoint.postNetOperationToNIC(this);
    }
}
