package jarg.rdmarpc.rpc.exception;

/**
 * An exception thrown when message data could not be (de)serialized correctly.
 */
public class RpcDataSerializationException extends Exception{

    public RpcDataSerializationException(String message){
        super(message);
    }

    public RpcDataSerializationException(String message, Throwable cause){
        super(message, cause);
    }
}
