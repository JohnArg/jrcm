package jarg.rdmarpc.rdma.connections;

import com.ibm.disni.verbs.IbvWC;

import java.nio.ByteBuffer;

/**
 * Implement this to define how Work Completion events for the various types
 * of supported operations will be handled by a {@link RdmaRpcEndpoint}. An implementation of
 * this interface has to be passed to an {@link RdmaRpcEndpoint}'s constructor (Strategy Pattern).
 * Implementations of this interface must take care of using {@link RdmaRpcEndpoint#freeUpWrID freeUpWrID}
 * after finishing handling the Work Completion event. Otherwise, the Work Request id of this event cannot
 * be reused by subsequent Work Requests of the same type ('send' or 'receive'),
 * which will eventually prevent the {@link RdmaRpcEndpoint} from posting new requests to the NIC.
 */
public interface WorkCompletionHandler {
    void handleTwoSidedReceive(IbvWC wc, RdmaRpcEndpoint endpoint, ByteBuffer receiveBuffer);
    void handleTwoSidedSend(IbvWC wc, RdmaRpcEndpoint endpoint);
    void handleOneSidedWrite(IbvWC wc, RdmaRpcEndpoint endpoint);
    void handleOneSidedRead(IbvWC wc, RdmaRpcEndpoint endpoint);
}
