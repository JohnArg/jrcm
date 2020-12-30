package jarg.rdmarpc.server.rdma.netrequests;

public class WorkRequestTypes {
    public static final int TWO_SIDED_SIGNALED = 1;       // 0b1
    public static final byte ONE_SIDED_WRITE_SIGNALED = 2;         // 0b10
    public static final byte ONE_SIDED_READ_SIGNALED = 4;          // 0b100
}
