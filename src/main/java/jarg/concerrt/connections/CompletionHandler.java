package jarg.concerrt.connections;

import com.ibm.disni.verbs.IbvWC;

public interface CompletionHandler {
    void handleCompletionEvent(IbvWC wcEvent);
}
