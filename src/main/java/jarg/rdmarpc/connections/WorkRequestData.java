package jarg.rdmarpc.connections;

import java.nio.ByteBuffer;

public class WorkRequestData {
    private int id;
    private ByteBuffer buffer;

    public WorkRequestData(int id, ByteBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
