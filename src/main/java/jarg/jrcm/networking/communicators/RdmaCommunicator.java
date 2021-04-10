package jarg.jrcm.networking.communicators;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxyProvider;

/**
 * Used to communicate with RDMA, by posting RDMA
 * networking operations to the Network Interface Controller.
 * It reads the necessary information about the networking operation
 * to perform from a {@link WorkRequestProxy}.
 * Additionally, it allows the user application to access to a
 * {@link WorkRequestProxyProvider}, which can supply the application
 * with WorkRequestProxies to use during communications.
 */
public interface RdmaCommunicator {

    /**
     * Posts a networking operation to the network card.
     * @param proxy the object describing the network operation.
     * @return true on success, false otherwise.
     */
    boolean postNetOperationToNIC(WorkRequestProxy proxy);

    /**
     * Returns the {@link WorkRequestProxyProvider} that the
     * endpoint uses.
     */
    WorkRequestProxyProvider getWorkRequestProxyProvider();

    /**
     * Checks if the communicator is in the process of being
     * shut down.
     * @return true if the communicator is in the process of a
     * shutdown, false otherwise.
     */
    boolean isShutDown();
}
