package jarg.jrcm.networking.dependencies.svc;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;

/**
 * Manages Stateful Verb Calls, which is a feature of IBM's jVerbs library.
 * Using RDMA in Java requires JNI calls which can be pretty expensive in terms of
 * latency. To minimize latency, jVerbs saves the state that has to be serialized
 * for such a JNI call into an SVC object. This object can be reused to perform the
 * same RDMA operation, which means posting the same RDMA Work Request to the RDMA NIC.
 * By reusing SVCs, the latency costs of serializing the necessary data for JNI calls
 * are suffered only the first time an SVC is executed. Subsequent executions are
 * faster. This has enabled fast RDMA
 * communications in Java as well, with performance very close to this of C code.
 * Thus, the SVC feature is also used by jRCM through the SVCManager.
 * The SVCManager's job is to create and store SVCs before commencing the RDMA
 * communications and to reuse those SVCs during communications.
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
