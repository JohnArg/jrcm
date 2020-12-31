package jarg.rdmarpc.rdma.connections;

import java.nio.ByteBuffer;

public class WorkRequestData {
    private int id;
    private RdmaRpcEndpoint.PostedRequestType requestType;
    private ByteBuffer buffer;
    private RdmaRpcEndpoint endpoint;

    public WorkRequestData(int id, RdmaRpcEndpoint.PostedRequestType requestType,
                           ByteBuffer buffer, RdmaRpcEndpoint endpoint) {
        this.id = id;
        this.requestType = requestType;
        this.buffer = buffer;
        this.endpoint = endpoint;
    }

    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public RdmaRpcEndpoint getEndpoint() {
        return endpoint;
    }

    public RdmaRpcEndpoint.PostedRequestType getRequestType() {
        return requestType;
    }
}
