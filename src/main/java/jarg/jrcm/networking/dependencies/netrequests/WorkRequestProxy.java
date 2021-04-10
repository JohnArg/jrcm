package jarg.jrcm.networking.dependencies.netrequests;

import jarg.jrcm.networking.communicators.RdmaCommunicator;
import jarg.jrcm.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

public class WorkRequestProxy {
    private int id;
    private PostedRequestType postType;
    private WorkRequestType workRequestType;
    private ByteBuffer buffer;
    private RdmaCommunicator rdmaCommunicator;

    public WorkRequestProxy(){}

    public WorkRequestProxy(int id, PostedRequestType postType,
                            WorkRequestType workRequestType, ByteBuffer buffer, RdmaCommunicator endpoint) {
        this.id = id;
        this.postType = postType;
        this.workRequestType = workRequestType;
        this.buffer = buffer;
        this.rdmaCommunicator = endpoint;
    }


    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public RdmaCommunicator getRdmaCommunicator() {
        return rdmaCommunicator;
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

    public WorkRequestProxy setRdmaCommunicator(RdmaCommunicator rdmaCommunicator) {
        this.rdmaCommunicator = rdmaCommunicator;
        return this;
    }

    /**
     * Releases the Work Request associated with this proxy, so that it
     * can be reused.
     */
    public void releaseWorkRequest(){
        rdmaCommunicator.getWorkRequestProxyProvider().releaseWorkRequest(this);
    }

    /**
     * Convenience method for posting this Work Request to the NIC.
     */
    public void post(){
        rdmaCommunicator.postNetOperationToNIC(this);
    }

}
