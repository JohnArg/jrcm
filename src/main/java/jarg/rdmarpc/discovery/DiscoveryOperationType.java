package jarg.rdmarpc.discovery;

/**
 * Used to specify an {@link RdmaDiscoveryApi} operation.
 */
public class DiscoveryOperationType {
    public static final int REGISTER_SERVER = 0;
    public static final int UNREGISTER_SERVER = 1;
    public static final int GET_SERVERS = 2;
    public static final int GET_SERVER_PORT = 3;
}
