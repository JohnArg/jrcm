package jarg.jrcm.rpc.packets;

/**
 * Specifies whether the message to be sent or received is a request, a response or an error.
 */
public class RpcMessageType {
    public static final byte REQUEST = 1;
    public static final byte RESPONSE = 2;
    public static final byte ERROR = 3;
}
