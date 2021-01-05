package jarg.rdmarpc.networking.dependencies.svc;

import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;

/**
 * Manages Stateful Verb Calls <i>(see IBM's jVerbs => SVC)</i>.
 * An SVC represents the state of an RDMA Work Request for the Network Card (NIC).
 * SVCs are meant to be created once and reused, in order to reduce overheads of JNI
 * calls for posting RDMA Work Requests to the NIC.
 */
public interface SVCManager {

    /**
     * Creates and stores SVCs.
     */
    void initializeSVCs();

    /**
     * Execute the SVC corresponding to the Work Request that is represented by the
     * provided {@link WorkRequestProxy}.
     * @param workRequestProxy the object representing the Work Request.
     *
     * @return true on success, false on error.
     */
    boolean executeSVC(WorkRequestProxy workRequestProxy);
}
