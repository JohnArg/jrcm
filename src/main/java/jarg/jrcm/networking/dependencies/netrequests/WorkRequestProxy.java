package jarg.jrcm.networking.dependencies.netrequests;

import com.ibm.disni.verbs.IbvWC;
import jarg.jrcm.networking.communicators.RdmaCommunicator;
import jarg.jrcm.networking.dependencies.netrequests.types.PostedRequestType;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;

import java.nio.ByteBuffer;

/**
 * <p>
 * A proxy object that represents an RDMA Work Request.
 * The latter describes to the RDMA NIC the networking operation
 * that it has to perform.
 * Applications that use jRCM need only use WorkRequestProxies for
 * RDMA communications. These proxies contain all the necessary
 * information to tell jRCM what kind of RDMA networking operation to
 * perform. They also contain a reference to the network buffer that
 * will be used for an RDMA Work Request. Applications can fill this
 * buffer with data to send, or read received data from it.
 * <b>Important!</b> When the application is finished with a WorkRequestProxy, it must call
 * {@link WorkRequestProxy#releaseWorkRequest()} to make it available to later
 * reuse, otherwise it will run out of WorkRequestProxies to use.
 * </p>
 * </p>
 *
 * <p>
 * Applications need to use WorkRequestProxies both for sending and
 * receiving data. When sending data, an application can request
 * an available WorkRequestProxy from a {@link WorkRequestProxyProvider}.
 * The WorkRequestProxyProvider can be retrieved by the {@link RdmaCommunicator}
 * that will be used to communicate. Once the application gets an available
 * WorkRequestProxy for sending data, it can fill its ByteBuffer with data to
 * send and then call the {@link WorkRequestProxy#post()} method.
 * When an application wants to receive data, it must implement
 * {@link WorkCompletionHandler#handleCqEvent(IbvWC)}
 * to use the {@link WorkRequestProxyProvider#getWorkRequestProxyForWc(IbvWC)}
 * to "translate" an {@link IbvWC} notification from the Network Card to a
 * WorkRequestProxy object. That proxy object will contain the received
 * message in its internal ByteBuffer and the application can read data from it.
 * After finishing with the WorkRequestProxy, the application has to release it.
 */
public class WorkRequestProxy {
    private int id;
    private PostedRequestType postType;
    private WorkRequestType workRequestType;
    private ByteBuffer buffer;
    private RdmaCommunicator rdmaCommunicator;

    public WorkRequestProxy(){}

    public WorkRequestProxy(int id, PostedRequestType postType,
                            WorkRequestType workRequestType, ByteBuffer buffer, RdmaCommunicator endpoint) {
        this.id = id;
        this.postType = postType;
        this.workRequestType = workRequestType;
        this.buffer = buffer;
        this.rdmaCommunicator = endpoint;
    }


    public int getId() {
        return id;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public RdmaCommunicator getRdmaCommunicator() {
        return rdmaCommunicator;
    }

    public PostedRequestType getPostType() {
        return postType;
    }

    public WorkRequestType getWorkRequestType() {
        return workRequestType;
    }

    public WorkRequestProxy setId(int id) {
        this.id = id;
        return this;
    }

    public WorkRequestProxy setPostType(PostedRequestType postType) {
        this.postType = postType;
        return this;
    }

    public WorkRequestProxy setWorkRequestType(WorkRequestType workRequestType) {
        this.workRequestType = workRequestType;
        return this;
    }

    public WorkRequestProxy setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public WorkRequestProxy setRdmaCommunicator(RdmaCommunicator rdmaCommunicator) {
        this.rdmaCommunicator = rdmaCommunicator;
        return this;
    }

    /**
     * Releases the Work Request associated with this proxy, so that it
     * can be reused.
     */
    public void releaseWorkRequest(){
        rdmaCommunicator.getWorkRequestProxyProvider().releaseWorkRequest(this);
    }

    /**
     * Convenience method for posting the Work Request represented by this proxy to the NIC.
     */
    public void post(){
        rdmaCommunicator.postNetOperationToNIC(this);
    }

}
