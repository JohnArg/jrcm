package jarg.rdmarpc.networking.dependencies;

import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkCompletionHandler;
import jarg.rdmarpc.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.svc.AbstractSVCManager;

/**
 * Dependencies that will be passed to an {@link RdmaCommunicator}.
 */
public class RdmaCommunicatorDependencies {
    private NetworkBufferManager bufferManager;
    private AbstractSVCManager svcManager;
    private WorkRequestProxyProvider proxyProvider;
    private AbstractWorkCompletionHandler workCompletionHandler;
    private int maxWorkRequests;
    private int maxBufferSize;

    public RdmaCommunicatorDependencies() {}

    public NetworkBufferManager getBufferManager() {
        return bufferManager;
    }

    public RdmaCommunicatorDependencies setBufferManager(NetworkBufferManager bufferManager) {
        this.bufferManager = bufferManager;
        return this;
    }

    public AbstractSVCManager getSvcManager() {
        return svcManager;
    }

    public RdmaCommunicatorDependencies setSvcManager(AbstractSVCManager svcManager) {
        this.svcManager = svcManager;
        return this;
    }

    public WorkRequestProxyProvider getProxyProvider() {
        return proxyProvider;
    }

    public RdmaCommunicatorDependencies setProxyProvider(WorkRequestProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
        return this;
    }

    public AbstractWorkCompletionHandler getWorkCompletionHandler() {
        return workCompletionHandler;
    }

    public RdmaCommunicatorDependencies setWorkCompletionHandler(AbstractWorkCompletionHandler workCompletionHandler) {
        this.workCompletionHandler = workCompletionHandler;
        return this;
    }

    public int getMaxWorkRequests() {
        return maxWorkRequests;
    }

    public RdmaCommunicatorDependencies setMaxWorkRequests(int maxWorkRequests) {
        this.maxWorkRequests = maxWorkRequests;
        return this;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public RdmaCommunicatorDependencies setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
        return this;
    }
}
