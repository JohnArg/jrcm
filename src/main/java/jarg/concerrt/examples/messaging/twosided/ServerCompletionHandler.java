package jarg.concerrt.examples.messaging.twosided;

import com.ibm.disni.verbs.IbvWC;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.connections.WorkCompletionHandler;
import jarg.concerrt.connections.WorkRequestData;
import jarg.concerrt.requests.WorkRequestTypes;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerCompletionHandler implements WorkCompletionHandler {

    @Override
    public void handleTwoSidedReceive(IbvWC wc, ConceRRTEndpoint endpoint, ByteBuffer receiveBuffer) {
        int bufferId = receiveBuffer.getInt();
        receiveBuffer.limit(32);
        ByteBuffer textBuffer = receiveBuffer.slice();
        String text = textBuffer.asCharBuffer().toString();
        System.out.format("Received completion for WR %d. Buffer address %d,  id : %d. The data is : %s\n",
                ((sun.nio.ch.DirectBuffer) receiveBuffer).address(), (int) wc.getWr_id(), bufferId, text);

        // create a response for the received message ----------------------------
        String response = "ACK " + bufferId;
        // we don't need the received data anymore
        receiveBuffer.clear();
        // Always free the Work Request id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), ConceRRTEndpoint.PostedRequestType.RECEIVE);

        // Send a response to the client -----------------------------------------
        WorkRequestData wrData = endpoint.getWorkRequestBlocking();
        ByteBuffer sendBuffer = wrData.getBuffer();
        for(int j=0; j < response.length(); j ++){
            sendBuffer.putChar(response.charAt(j));
        }
        sendBuffer.flip();
        // send the data across
        try {
            endpoint.send(wrData.getId(), sendBuffer.limit(), WorkRequestTypes.TWO_SIDED_SIGNALED);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
