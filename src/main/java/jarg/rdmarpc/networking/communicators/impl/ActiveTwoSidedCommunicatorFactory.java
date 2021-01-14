package jarg.rdmarpc.networking.communicators.impl;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.rdmarpc.networking.dependencies.RdmaCommunicatorDependencies;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkCompletionHandler;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkRequestProxyProvider;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * A Factory of {@link ActiveRdmaCommunicator ActiveRdmaCommunicators} that use <i>two-sided</i> RDMA
 * operations.
 */
public class ActiveTwoSidedCommunicatorFactory implements RdmaEndpointFactory<ActiveRdmaCommunicator> {

    private RdmaActiveEndpointGroup<ActiveRdmaCommunicator> endpointGroup;
    private TwoSidedDependenciesFactory dependenciesFactory;

    public ActiveTwoSidedCommunicatorFactory(RdmaActiveEndpointGroup<ActiveRdmaCommunicator> endpointGroup,
                                             int maxWorkRequests, int maxBufferSize,
                                             Supplier<AbstractWorkRequestProxyProvider> proxyProviderSupplier,
                                             Supplier<AbstractWorkCompletionHandler> workCompletionHandlerSupplier) {
        this.endpointGroup = endpointGroup;
        dependenciesFactory = new TwoSidedDependenciesFactory(maxWorkRequests, maxBufferSize, proxyProviderSupplier,
                workCompletionHandlerSupplier);
    }

    @Override
    public ActiveRdmaCommunicator createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
        RdmaCommunicatorDependencies dependencies = dependenciesFactory.get();
        return new ActiveRdmaCommunicator(endpointGroup, id, serverSide, dependencies);
    }
}
