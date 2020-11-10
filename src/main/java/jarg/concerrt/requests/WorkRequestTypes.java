package jarg.concerrt.requests;

public class WorkRequestTypes {
    public static final int TWO_SIDED_SEND_SIGNALED = 1;  // 0b1
    public static final byte TWO_SIDED_RECV = 2;          // 0b10
    public static final byte ONE_SIDED_WRITE = 4;         // 0b100
    public static final byte ONE_SIDED_READ = 8;          // 0b1000
}
