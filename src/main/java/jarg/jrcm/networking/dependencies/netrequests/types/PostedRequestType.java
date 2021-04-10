package jarg.jrcm.networking.dependencies.netrequests.types;

import com.ibm.disni.RdmaEndpoint;

import java.util.List;

/**
 * RDMA operations can be posted to the RDMA NIC either via
 * {@link RdmaEndpoint#postSend(List)} or {@link RdmaEndpoint#postRecv(List)}.
 * This type specifies which method should be used to post an RDMA operation.
 */
public enum PostedRequestType {
    SEND,       // a postSend-type request
    RECEIVE     // a postRecv-type request
}
