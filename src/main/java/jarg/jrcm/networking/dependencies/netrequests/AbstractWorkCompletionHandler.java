package jarg.jrcm.networking.dependencies.netrequests;

/**
 * An abstract {@link WorkCompletionHandler} that is associated with a {@link WorkRequestProxyProvider}.
 * A WorkRequestProxyProvider can be used inside a WorkCompletionHandler to "translate" a Work Completion
 * Event notification from the network to a {@link WorkRequestProxy}. Such a
 * WorkRequestProxy represents the RDMA Work Request that completed either successfully or
 * with errors. In case of a successful message reception with RDMA RECV, the WorkRequestProxy
 * also contains the ByteBuffer with the received message.
 */
public abstract  class AbstractWorkCompletionHandler implements WorkCompletionHandler{

    // dependencies ----------------------
    protected WorkRequestProxyProvider proxyProvider;

    public AbstractWorkCompletionHandler(){

    }

    public AbstractWorkCompletionHandler(WorkRequestProxyProvider proxyProvider){
        this.proxyProvider = proxyProvider;
    }

    public WorkRequestProxyProvider getProxyProvider() {
        return proxyProvider;
    }

    public void setProxyProvider(WorkRequestProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }
}
