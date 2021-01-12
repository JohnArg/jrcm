package jarg.rdmarpc.networking.communicators;

import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;

/**
 * Posts networking operations to the Network Interface Controller.
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
}
