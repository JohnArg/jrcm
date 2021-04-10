package jarg.jrcm.networking.dependencies.svc.impl;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;
import com.ibm.disni.verbs.SVCPostSend;
import jarg.jrcm.networking.dependencies.svc.AbstractSVCManager;
import jarg.jrcm.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.networking.dependencies.netrequests.impl.postsend.OneSidedRequest;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static jarg.jrcm.networking.dependencies.netrequests.impl.postsend.OneSidedRequest.OneSidedRequestType.READ;

/**
 * Manages SVCs <i>(see IBM's jVerbs => SVC)</i> for
 * <i>one-sided RDMA READ</i> operations.
 */
public class OneSidedReadSVCManager extends AbstractSVCManager {

    private static final Logger logger = LoggerFactory.getLogger(OneSidedReadSVCManager.class);

    private SVCPostSend[] oneSidedReadSVCs;            // A one-sided WRITE SVC for each WR id


    public OneSidedReadSVCManager(int maxBufferSize, int maxWorkRequests) {
        super(maxBufferSize, maxWorkRequests);
    }


    @Override
    public void initializeSVCs() {
        // get dependencies
        int maxWorkRequests = getMaxWorkRequests();
        int maxBufferSize = getMaxBufferSize();
        IbvMr registeredMemoryRegion = getRegisteredMemoryRegion();
        NetworkBufferManager bufferManager = getBufferManager();
        RdmaEndpoint rdmaEndpoint = getRdmaEndpoint();

        OneSidedRequest oneSidedWriteRequest;
        oneSidedReadSVCs = new SVCPostSend[maxWorkRequests];
        // pre-create SVCs
        for(int i=0; i < maxWorkRequests; i++){
            // We need to store an SVC for one request at a time, so
            // we need a new list each time
            List<IbvSendWR> sendRequests = new ArrayList<>(maxWorkRequests);

            oneSidedWriteRequest = new OneSidedRequest(registeredMemoryRegion, READ);
            oneSidedWriteRequest.prepareRequest();
            oneSidedWriteRequest.setRequestId(i);
            oneSidedWriteRequest.setSgeLength(maxBufferSize);
            oneSidedWriteRequest.setBufferMemoryAddress(
                    bufferManager.getWorkRequestBufferAddress(WorkRequestType.ONE_SIDED_READ_SIGNALED, i));
            sendRequests.add(oneSidedWriteRequest.getSendWR());
            // create and store SVCs
            try {
                oneSidedReadSVCs[i] = rdmaEndpoint.postSend(sendRequests);
            } catch (IOException e) {
                logger.error("Failed to initialize SVCs.", e);
            }
        }
    }

    @Override
    public boolean executeSVC(WorkRequestProxy workRequestProxy) {
        boolean success = true;
        int workRequestId = workRequestProxy.getId();
        int dataLength = workRequestProxy.getBuffer().limit();
        WorkRequestType workRequestType = workRequestProxy.getWorkRequestType();
        try {
            oneSidedReadSVCs[workRequestId].getWrMod(0).
                    getSgeMod(0).setLength(dataLength);
            oneSidedReadSVCs[workRequestId].execute();
        }catch (IOException e){
            logger.error("Failed to execute SVC for Work Request Type "
                    + workRequestType + " and id " + workRequestId, e);
            success = false;
        }
        return success;
    }
}
