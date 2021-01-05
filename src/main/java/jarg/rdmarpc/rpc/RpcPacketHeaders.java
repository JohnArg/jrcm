package jarg.rdmarpc.rpc;

import jarg.rdmarpc.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * Encapsulates RPC message headers.
 */
public class RpcPacketHeaders extends AbstractDataSerializer {

    private static final long serialVersionId = 1L;

    // Headers ---------------------------------------------------------------------------
    private byte messageType;                       // what kind of message? (e.g. request or a response?)
    private int operationType;                      // what type of operation (rpc function) to invoke?
    private long operationID;                       // unique operation identifier
    private int packetNumber;                       // the number of this packet
                                                        // in case a large message is split in multiple packets

    public RpcPacketHeaders(){};

    public RpcPacketHeaders(byte messageType, int operationType,
                            long operationID, int packetNumber) {
        this.messageType = messageType;
        this.operationType = operationType;
        this.operationID = operationID;
        this.packetNumber = packetNumber;
    }

    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        buffer.putLong(serialVersionId);
        buffer.put(messageType);
        buffer.putInt(operationType);
        buffer.putLong(operationID);
        buffer.putInt(packetNumber);
    }

    @Override
    public void readFromWorkRequestBuffer() throws RpcDataSerializationException {
        ByteBuffer buffer = getWorkRequestData().getBuffer();
        // read headers -----------------
        long receivedSerialVersionId = buffer.getLong();
        if(receivedSerialVersionId != serialVersionId){
            throw new RpcDataSerializationException("Serial versions do not match. Local version : "+
                    serialVersionId + ", remote version : " + receivedSerialVersionId + ".");
        }
        messageType = buffer.get();
        operationType = buffer.getInt();
        operationID = buffer.getLong();
        packetNumber = buffer.getInt();
    }

    /* *********************************************************
    *   Getters/Setters
    ********************************************************* */

    public static long getSerialVersionId() {
        return serialVersionId;
    }

    public byte getMessageType() {
        return messageType;
    }

    public int getOperationType() {
        return operationType;
    }

    public long getOperationID() {
        return operationID;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    // Enable method chaining on the setters ---------------

    public RpcPacketHeaders setMessageType(byte messageType) {
        this.messageType = messageType;
        return this;
    }

    public RpcPacketHeaders setOperationType(int operationType) {
        this.operationType = operationType;
        return this;
    }

    public RpcPacketHeaders setOperationID(long operationID) {
        this.operationID = operationID;
        return this;
    }

    public RpcPacketHeaders setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
        return this;
    }
}