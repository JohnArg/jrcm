package jarg.jrcm.networking.dependencies.netrequests.types;

/**
 * Contains constants representing two-sided and one-sided RDMA requests.
 */
public enum WorkRequestType {
    TWO_SIDED_SEND_SIGNALED,
    TWO_SIDED_RECV,
    ONE_SIDED_WRITE_SIGNALED,
    ONE_SIDED_READ_SIGNALED
}
