package jarg.rdmarpc.rpc;

/**
 * Specifies whether the message to be sent or received is a request or a response.
 */
public class RpcMessageType {
    public static final byte REQUEST = 0;
    public static final byte RESPONSE = 1;
    public static final byte ERROR = 3;
}
