package jarg.rdmarpc.server.rdma.connections;

import java.nio.ByteBuffer;

public class WorkRequestData {
    private int id;
    private ByteBuffer buffer;
    private RpcBasicEndpoint endpoint;

    public WorkRequestData(int id, ByteBuffer buffer, RpcBasicEndpoint endpoint) {
        this.id = id;
        this.buffer = buffer;
        this.endpoint = endpoint;
    }

    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public RpcBasicEndpoint getEndpoint() {
        return endpoint;
    }
}
