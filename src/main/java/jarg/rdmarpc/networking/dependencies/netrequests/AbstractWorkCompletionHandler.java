package jarg.rdmarpc.networking.dependencies.netrequests;

/**
 * Abstract class that defines dependencies needed to implement a {@link WorkCompletionHandler}.
 */
public abstract  class AbstractWorkCompletionHandler implements WorkCompletionHandler{

    // dependencies ----------------------
    private AbstractWorkRequestProxyProvider proxyProvider;

    public AbstractWorkCompletionHandler(){

    }

    public AbstractWorkRequestProxyProvider getProxyProvider() {
        return proxyProvider;
    }

    public void setProxyProvider(AbstractWorkRequestProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }
}
