package jarg.rdmarpc.networking.dependencies;

import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkCompletionHandler;
import jarg.rdmarpc.networking.dependencies.netrequests.AbstractWorkRequestProxyProvider;
import jarg.rdmarpc.networking.dependencies.svc.AbstractSVCManager;

/**
 * Will hold the dependencies that will be passed to an {@link RdmaCommunicator}.
 */
public class RdmaCommunicatorDependencies {
    private NetworkBufferManager bufferManager;
    private AbstractSVCManager svcManager;
    private AbstractWorkRequestProxyProvider proxyProvider;
    private AbstractWorkCompletionHandler workCompletionHandler;
    private int maxWorkRequests;
    private int maxBufferSize;

    public RdmaCommunicatorDependencies() {}

    public NetworkBufferManager getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(NetworkBufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public AbstractSVCManager getSvcManager() {
        return svcManager;
    }

    public void setSvcManager(AbstractSVCManager svcManager) {
        this.svcManager = svcManager;
    }

    public AbstractWorkRequestProxyProvider getProxyProvider() {
        return proxyProvider;
    }

    public void setProxyProvider(AbstractWorkRequestProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    public AbstractWorkCompletionHandler getWorkCompletionHandler() {
        return workCompletionHandler;
    }

    public void setWorkCompletionHandler(AbstractWorkCompletionHandler workCompletionHandler) {
        this.workCompletionHandler = workCompletionHandler;
    }

    public int getMaxWorkRequests() {
        return maxWorkRequests;
    }

    public void setMaxWorkRequests(int maxWorkRequests) {
        this.maxWorkRequests = maxWorkRequests;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }
}
