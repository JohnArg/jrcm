package jarg.rdmarpc.server.rpc;

import jarg.rdmarpc.server.rdma.connections.WorkRequestData;
import jarg.rdmarpc.server.rpc.RpcDataSerializer;
import jarg.rdmarpc.server.rpc.exception.RpcDataSerializationException;

import java.nio.ByteBuffer;

/**
 * Encapsulates RPC message headers.
 */
public class RpcPacketHeaders implements RpcDataSerializer {

    private static final long serialVersionId = 1L;

    private WorkRequestData workRequestData;
    // Headers ---------------------------------------------------------------------------
    private byte messageType;                       // what kind of message? (e.g. request or a response?)
    private int operationType;                      // what type of operation (rpc function) to invoke?
    private long operationID;                       // unique operation identifier
    private int packetNumber;                       // the number of this packet
                                                        // in case a large message is split in multiple packets

    public RpcPacketHeaders(WorkRequestData workRequestData){
        this.workRequestData = workRequestData;
    }

    public RpcPacketHeaders(WorkRequestData workRequestData,
                            byte messageType, int operationType,
                            long operationID, int packetNumber) {
        this.workRequestData = workRequestData;
        this.messageType = messageType;
        this.operationType = operationType;
        this.operationID = operationID;
        this.packetNumber = packetNumber;
    }

    @Override
    public void writeToWorkRequestBuffer() {
        ByteBuffer buffer = workRequestData.getBuffer();
        buffer.putLong(serialVersionId);
        buffer.put(messageType);
        buffer.putInt(operationType);
        buffer.putLong(operationID);
        buffer.putInt(packetNumber);
    }

    @Override
    public void readFromWorkRequestBuffer() throws RpcDataSerializationException {
        ByteBuffer buffer = workRequestData.getBuffer();
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

    @Override
    public WorkRequestData getWorkRequestData() {
        return workRequestData;
    }

    @Override
    public void setWorkRequestData(WorkRequestData workRequestData) {
        this.workRequestData = workRequestData;
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


}