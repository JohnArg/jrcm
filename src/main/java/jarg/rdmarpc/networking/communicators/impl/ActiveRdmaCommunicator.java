package jarg.rdmarpc.networking.communicators.impl;

import com.ibm.disni.RdmaActiveEndpoint;
import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvWC;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.rdmarpc.networking.communicators.RdmaCommunicator;
import jarg.rdmarpc.networking.dependencies.RdmaCommunicatorDependencies;
import jarg.rdmarpc.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.rdmarpc.networking.dependencies.netrequests.*;
import jarg.rdmarpc.networking.dependencies.svc.AbstractSVCManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This {@link RdmaCommunicator} establishes a channel of RDMA communications with remote machines.
 * It the following jobs:
 * <ul>
 *     <li>It performs all necessary preparations before staring RDMA communications,
 *          including memory allocations, memory registration to the Network Interface Controller
 *          (NIC) and SVC creation <i>(see IBM's jverbs)</i>.
 *     </li>
 *     <li>It transmits and receives data using the RDMA technology.</li>
 *     <li>In order to transmit and receive data, RDMA Work Requests (WRs) are sent to the
 *          NIC. The communicator creates, stores, submits to the NIC and manages the lifecycle
 *          of such WRs. Therefore, it also has to be able to identify the WRs and associate
 *          them with events, data buffers and SVCs.
 *     </li>
 * </ul>
 *<p>
 * This type of {@link RdmaCommunicator} is also an {@link RdmaActiveEndpoint}, which means that
 * it receives notifications about RDMA Work Request Completion Events through a
 * {@link RdmaActiveEndpointGroup}.
 * Then the communicator can decide what to do with these notifications by invoking a
 * {@link WorkCompletionHandler} strategy.
 * </p>
 */
public class ActiveRdmaCommunicator extends RdmaActiveEndpoint implements RdmaCommunicator {

    private IbvMr registeredMemoryRegion;
    // Injected dependencies --------------------------
    private RdmaCommunicatorDependencies dependencies;  // kept for querying purposes
    private NetworkBufferManager bufferManager;
    private AbstractSVCManager svcManager;
    private AbstractWorkRequestProxyProvider proxyProvider;
    private AbstractWorkCompletionHandler workCompletionHandler;

    public ActiveRdmaCommunicator(RdmaActiveEndpointGroup<? extends ActiveRdmaCommunicator> group,
                                  RdmaCmId idPriv, boolean serverSide,
                                  RdmaCommunicatorDependencies dependencies)
                            throws IOException {

        super(group, idPriv, serverSide);
        // extract dependencies
        this.dependencies = dependencies;
        bufferManager = dependencies.getBufferManager();
        svcManager  = dependencies.getSvcManager();
        proxyProvider = dependencies.getProxyProvider();
        workCompletionHandler = dependencies.getWorkCompletionHandler();
    }

    @Override
    public void init() throws IOException{
        // allocate communication buffers and register their memory region to the NIC
        bufferManager.allocateCommunicationBuffers();
        ByteBuffer registeredBuffer = bufferManager.getBufferToRegister();
        registeredMemoryRegion = registerMemory(registeredBuffer).execute().free().getMr();
        // pass SVC manager dependencies and initialize SVCs
        svcManager.setRdmaEndpoint(this);
        svcManager.setBufferManager(bufferManager);
        svcManager.setRegisteredMemoryRegion(registeredMemoryRegion);
        svcManager.initializeSVCs();
        // pass proxy provider dependencies
        proxyProvider.setBufferManager(bufferManager);
        proxyProvider.setEndpoint(this);
        // pass proxy provider to work completion handler
        workCompletionHandler.setProxyProvider(proxyProvider);
    }

    /**
     * When a Work Completion event is ready, call the {@link WorkCompletionHandler} strategy
     * to handle the event.
     * @param wc the Work Completion event dispatched to this Endpoint.
     * @throws IOException
     */
    @Override
    public void dispatchCqEvent(IbvWC wc) throws IOException {
        int status = wc.getStatus();
        if(status != 0){    // an error occurred
            workCompletionHandler.handleCqEventError(wc);
        }else{
            workCompletionHandler.handleCqEvent(wc);
        }
    }

    /**
     * Clean up resources when closing this Endpoint.
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void close() throws IOException, InterruptedException {
        super.close();
    }

    @Override
    public boolean postNetOperationToNIC(WorkRequestProxy workRequestProxy){
        if(super.isClosed() || !(super.qp.isOpen())){
            return false;
        }
        return svcManager.executeSVC(workRequestProxy);
    }

    @Override
    public WorkRequestProxyProvider getWorkRequestProxyProvider() {
        return proxyProvider;
    }


    public RdmaCommunicatorDependencies getDependencies() {
        return dependencies;
    }
}
