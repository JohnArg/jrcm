package jarg.concerrt.examples.messaging.twosided;

import com.ibm.disni.verbs.IbvWC;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.connections.WorkCompletionHandler;

import java.nio.ByteBuffer;

public class ClientCompletionHandler implements WorkCompletionHandler {

    @Override
    public void handleTwoSidedReceive(IbvWC wc, ConceRRTEndpoint endpoint, ByteBuffer receiveBuffer) {
        String text = receiveBuffer.asCharBuffer().toString();
        System.out.format("Received completion for WR %d. Buffer address %d. The data is : %s\n",
                ((sun.nio.ch.DirectBuffer) receiveBuffer).address(), (int) wc.getWr_id(), text);
        receiveBuffer.clear();
        // Always free the Work Request id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), ConceRRTEndpoint.PostedRequestType.RECEIVE);
    }

    @Override
    public void handleTwoSidedSend(IbvWC wc, ConceRRTEndpoint endpoint) {
        System.out.format("My message with id %d was sent\n", (int) wc.getWr_id());
        // Always free the Work Request id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), ConceRRTEndpoint.PostedRequestType.SEND);
    }

    // We don't care about the following two here -------

    @Override
    public void handleOneSidedWrite(IbvWC wc, ConceRRTEndpoint endpoint) {

    }

    @Override
    public void handleOneSidedRead(IbvWC wc, ConceRRTEndpoint endpoint) {

    }
}
