package jarg.rdmarpc.rpc.exception;

/**
 * Exception that can be thrown when an error occurs while executing
 * an RPC.
 */
public class RpcExecutionException extends Exception{

    public RpcExecutionException(String message) {
        super(message);
    }

    public RpcExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
