package jarg.examples.messaging.two_sided;

import com.ibm.disni.verbs.IbvWC;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.connections.WorkCompletionHandler;

import java.nio.ByteBuffer;

public class ServerCompletionHandler implements WorkCompletionHandler {


    @Override
    public void handleTwoSidedReceive(IbvWC wc, ConceRRTEndpoint endpoint, ByteBuffer receiveBuffer) {
        System.out.format("Received completion for WR %d. The data is : "
                + receiveBuffer.asCharBuffer().toString() + "\n", (int) wc.getWr_id());
        receiveBuffer.clear();
        // Always free the id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), ConceRRTEndpoint.PostedRequestType.RECEIVE);
    }

    @Override
    public void handleTwoSidedSend(IbvWC wc, ConceRRTEndpoint endpoint) {
        System.out.format("My message with id %d was sent\n", (int) wc.getWr_id());
        // Always free the id after we're done
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
