package jarg.jrcm.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvWC;

public interface WorkCompletionHandler {

    void handleCqEvent(IbvWC workCompletionEvent);

    void handleCqEventError(IbvWC workCompletionEvent);
}