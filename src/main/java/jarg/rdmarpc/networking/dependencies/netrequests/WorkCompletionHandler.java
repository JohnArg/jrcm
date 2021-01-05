package jarg.rdmarpc.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvWC;
import jarg.rdmarpc.networking.communicators.impl.ActiveRdmaCommunicator;

import java.nio.ByteBuffer;

public interface WorkCompletionHandler {

    void handleCqEvent(IbvWC workCompletionEvent);

    void handleCqEventError(IbvWC workCompletionEvent);
}