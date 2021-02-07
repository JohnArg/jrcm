package jarg.rdmarpc.networking.communicators.impl;

import jarg.rdmarpc.networking.dependencies.RdmaCommunicatorDependencies;
import jarg.rdmarpc.networking.dependencies.netbuffers.impl.TwoSidedBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkCompletionHandler;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.svc.impl.TwoSidedSVCManager;

import java.util.function.Supplier;

/**
 * A Supplier that provides {@link RdmaCommunicatorDependencies} for an {@link ActiveRdmaCommunicator}
 * that wants to use only <i>two-sided</i> RDMA operations.
 */
public class TwoSidedDependenciesFactory implements Supplier<RdmaCommunicatorDependencies> {

    private int maxWorkRequests;
    private int maxBufferSize;
    private Supplier<WorkRequestProxyProvider> proxyProviderSupplier;
    private Supplier<AbstractWorkCompletionHandler> workCompletionHandlerSupplier;

    public TwoSidedDependenciesFactory(int maxWorkRequests, int maxBufferSize,
                                       Supplier<WorkRequestProxyProvider> proxyProviderSupplier,
                                       Supplier<AbstractWorkCompletionHandler> workCompletionHandlerSupplier) {
        this.maxWorkRequests = maxWorkRequests;
        this.maxBufferSize = maxBufferSize;
        this.proxyProviderSupplier = proxyProviderSupplier;
        this.workCompletionHandlerSupplier = workCompletionHandlerSupplier;
    }

    @Override
    public RdmaCommunicatorDependencies get() {
        RdmaCommunicatorDependencies dependencies = new RdmaCommunicatorDependencies();
        dependencies.setMaxWorkRequests(maxWorkRequests)
                .setMaxBufferSize(maxBufferSize)
                .setBufferManager(new TwoSidedBufferManager(maxBufferSize, maxWorkRequests))
                .setSvcManager(new TwoSidedSVCManager(maxBufferSize, maxWorkRequests))
                .setProxyProvider(proxyProviderSupplier.get())
                .setWorkCompletionHandler(workCompletionHandlerSupplier.get());

        return dependencies;
    }
}
