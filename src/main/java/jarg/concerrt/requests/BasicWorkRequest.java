package jarg.concerrt.requests;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSge;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class defining an RDMA Work Request (WR) for the Network Card.
 */
public abstract class BasicWorkRequest {
    IbvMr requestBufferMR;
    IbvSge requestSge;
    LinkedList<IbvSge> sgeList;  // disni requires this to be a LinkedList

    public BasicWorkRequest(IbvMr memoryRegion){
        requestBufferMR = memoryRegion;
        requestSge = new IbvSge();
        sgeList = new LinkedList<>();
        sgeList.add(requestSge);
    }

    public void setSgeLength(int length){
        requestSge.setLength(length);
    }

    public abstract void setRequestId(int workRequestId);
}
