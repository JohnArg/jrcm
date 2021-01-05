package jarg.rdmarpc.networking.dependencies.netrequests;

import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.rdmarpc.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

public class WorkRequestProxy {
    private final int id;
    private final PostedRequestType postType;
    private final WorkRequestType workRequestType;
    private final ByteBuffer buffer;
    private final RdmaCommunicator endpoint;

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
}
