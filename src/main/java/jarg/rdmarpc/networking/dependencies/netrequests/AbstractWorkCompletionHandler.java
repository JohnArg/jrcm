package jarg.rdmarpc.networking.dependencies.netrequests;

/**
 * Abstract class that defines dependencies needed to implement a {@link WorkCompletionHandler}.
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
