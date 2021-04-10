package jarg.jrcm.networking.communicators.impl;

import com.ibm.disni.RdmaActiveEndpoint;
import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvWC;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.jrcm.networking.communicators.RdmaCommunicator;
import jarg.jrcm.networking.dependencies.RdmaCommunicatorDependencies;
import jarg.jrcm.networking.dependencies.netbuffers.NetworkBufferManager;
import jarg.jrcm.networking.dependencies.netrequests.*;
import jarg.jrcm.networking.dependencies.svc.AbstractSVCManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This {@link RdmaCommunicator} establishes a channel of RDMA communications with other servers.
 * It has the following jobs:
 * <ul>
 *     <li>It performs all necessary preparations before staring RDMA communications,
 *          including memory allocations, memory registration to the Network Interface Controller
 *          (NIC) and SVC creation <i>(see IBM's jverbs)</i>. To do all this, the communicator is
 *          passed an {@link RdmaCommunicatorDependencies} object in the constructor. This object
 *          encapsulates other objects that take care of preparing resources for RDMA communications
 *          or determine how certain tasks will be performed during communications.
 *          These are called "dependencies" of the ActiveRdmaCommunicator, since the communicator
 *          depends on these objects to be able to perform RDMA communications. The dependencies
 *          are interfaces or abstract classes, allowing different implementations ("strategies") to
 *          be passed to the communicator, without having to change the communicator's code.
 *          This was important for allowing more flexibility in how RDMA communications will be performed,
 *          which was one of the main goals of jRCM.
 *     </li>
 *     <li>It transmits and receives data using the RDMA technology. Applications can pass it
 *     {@link WorkRequestProxy} objects that contain all the information needed
 *     about the RDMA operation to post to the RDMA NIC.
 *     </li>
 *     <li> This type of {@link RdmaCommunicator} is also an {@link RdmaActiveEndpoint}, which means that
 *         it receives notifications about RDMA Work Request Completion Events through a
 *         {@link RdmaActiveEndpointGroup}. Then the communicator can decide what to do with these notifications
 *         by invoking a {@link WorkCompletionHandler} strategy.
 *     </li>
 * </ul>
 */
public class ActiveRdmaCommunicator extends RdmaActiveEndpoint implements RdmaCommunicator {

    private IbvMr registeredMemoryRegion;
    // Injected dependencies --------------------------
    private RdmaCommunicatorDependencies dependencies;  // kept for querying purposes
    private NetworkBufferManager bufferManager;
    private AbstractSVCManager svcManager;
    private WorkRequestProxyProvider proxyProvider;
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
        proxyProvider.setCommunicator(this);
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
        if(isShutDown()){
            return false;
        }
        return svcManager.executeSVC(workRequestProxy);
    }

    @Override
    public WorkRequestProxyProvider getWorkRequestProxyProvider() {
        return proxyProvider;
    }

    @Override
    public boolean isShutDown() {
        return (isClosed() || !isConnected() || !getQp().isOpen());
    }


    public RdmaCommunicatorDependencies getDependencies() {
        return dependencies;
    }
}
