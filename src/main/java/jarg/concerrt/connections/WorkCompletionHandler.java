package jarg.concerrt.connections;

import com.ibm.disni.verbs.IbvWC;

import java.nio.ByteBuffer;

/**
 * Implement this to define how Work Completion events for the various types
 * of supported operations will be handled by a {@link ConceRRTEndpoint}. An implementation of
 * this interface has to be passed to an {@link ConceRRTEndpoint}'s constructor (Strategy Pattern).
 * Implementations of this interface must take care of using {@link ConceRRTEndpoint#freeUpWrID freeUpWrID}
 * after finishing handling the Work Completion event. Otherwise, the Work Request id of this event cannot
 * be reused by subsequent Work Requests of the same type ('send' or 'receive'),
 * which will eventually prevent the {@link ConceRRTEndpoint} from posting new requests to the NIC.
 */
public interface WorkCompletionHandler {
    void handleTwoSidedReceive(IbvWC wc, ConceRRTEndpoint endpoint, ByteBuffer receiveBuffer);
    void handleTwoSidedSend(IbvWC wc, ConceRRTEndpoint endpoint);
    void handleOneSidedWrite(IbvWC wc, ConceRRTEndpoint endpoint);
    void handleOneSidedRead(IbvWC wc, ConceRRTEndpoint endpoint);
}
