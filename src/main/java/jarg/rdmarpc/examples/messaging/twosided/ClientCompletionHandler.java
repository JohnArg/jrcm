package jarg.rdmarpc.examples.messaging.twosided;

import com.ibm.disni.verbs.IbvWC;
import jarg.rdmarpc.connections.RpcBasicEndpoint;
import jarg.rdmarpc.connections.WorkCompletionHandler;

import java.nio.ByteBuffer;

public class ClientCompletionHandler implements WorkCompletionHandler {
    private int ACK_COUNT;      // how many ACKs we got
    private int MAX_ACKS;       // how many ACKs to expect

    public ClientCompletionHandler(int maxACKs){
        ACK_COUNT = 0;
        MAX_ACKS = maxACKs;
    }

    @Override
    public void handleTwoSidedReceive(IbvWC wc, RpcBasicEndpoint endpoint, ByteBuffer receiveBuffer) {
        String text = receiveBuffer.asCharBuffer().toString();
        System.out.format("Received completion for WR %d. The data is : %s\n",
                (int) wc.getWr_id(), text);
        receiveBuffer.clear();
        // Always free the Work Request id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), RpcBasicEndpoint.PostedRequestType.RECEIVE);

        //For simplicity we don't check if the received message is the one expected
        ACK_COUNT++;
        if (ACK_COUNT == MAX_ACKS){
            System.exit(0);
        }
    }

    @Override
    public void handleTwoSidedSend(IbvWC wc, RpcBasicEndpoint endpoint) {
        System.out.format("My message with WR id %d was sent\n", (int) wc.getWr_id());
        // Always free the Work Request id after we're done
        endpoint.freeUpWrID((int) wc.getWr_id(), RpcBasicEndpoint.PostedRequestType.SEND);
    }

    // We don't care about the following two here -------

    @Override
    public void handleOneSidedWrite(IbvWC wc, RpcBasicEndpoint endpoint) {

    }

    @Override
    public void handleOneSidedRead(IbvWC wc, RpcBasicEndpoint endpoint) {

    }

}
