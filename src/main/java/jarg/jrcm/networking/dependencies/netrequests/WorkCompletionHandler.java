package jarg.jrcm.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvWC;
import jarg.jrcm.networking.communicators.impl.ActiveRdmaCommunicator;

/**
 * A WorkCompletionHandler specifies what will happen when a notification from the Network Card is
 * received about the completion of a previously posted RDMA Work Request. These notifications are
 * called Work Completion Events and contain information about the completed Work Request, as well
 * as whether it was successful or not. An {@link ActiveRdmaCommunicator} is passed WCE notifications
 * and according to whether the notification indicates success or error, it calls the appropriate
 * method of a WorkCompletionHandler. Applications need only implement this interface instead of
 * writing code to check status codes themselves.
 */
public interface WorkCompletionHandler {

    /**
     * Handle the successful completion of a Work Request.
     * @param workCompletionEvent the notification about the Work Request's completion.
     */
    void handleCqEvent(IbvWC workCompletionEvent);

    /**
     * Handle errors that occurred during the execution of an RDMA Work Request.
     * @param workCompletionEvent the notification about the Work Request's completion.
     */
    void handleCqEventError(IbvWC workCompletionEvent);
}