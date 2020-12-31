package jarg.rdmarpc.rdma.connections;

import com.ibm.disni.RdmaActiveCqProcessor;
import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaCqProvider;
import com.ibm.disni.util.DiSNILogger;
import com.ibm.disni.verbs.IbvCQ;
import com.ibm.disni.verbs.IbvContext;
import com.ibm.disni.verbs.IbvQP;
import com.ibm.disni.verbs.IbvQPInitAttr;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;

/**
 * An {@link RdmaActiveEndpointGroup RdmaActiveEndpointGroup} that gives each Endpoint its
 * own {@link RdmaActiveCqProcessor RdmaActiveCqProcessor}. This way each Endpoint has its own
 * Completion Queue and uses its own thread to poll it for Completion Events. This is different
 * from using an {@link RdmaActiveEndpointGroup RdmaActiveEndpointGroup}, which creates only one
 * Completion Queue that is shared by the Endpoints and polled by an
 * {@link RdmaActiveCqProcessor RdmaActiveCqProcessor}, that dispatches events to the corresponding
 * endpoints.
 */
public class RpcActiveEndpointGroup extends RdmaActiveEndpointGroup<RdmaRpcEndpoint> {
    private static final Logger logger = DiSNILogger.getLogger();
    private HashMap<Integer, RdmaActiveCqProcessor<RdmaRpcEndpoint>> cqMap;
    private int timeout;
    private boolean polling;
    protected int cqSize;
    protected int maxSge;
    protected int maxWR;
    private HashMap<RdmaRpcEndpoint, RdmaActiveCqProcessor<RdmaRpcEndpoint>> endpointProcessorMap;

    public RpcActiveEndpointGroup(int timeout, boolean polling, int maxWR,
                                  int maxSge, int cqSize) throws IOException {
        super(timeout, polling, maxWR, maxSge, cqSize);
        this.timeout = timeout;
        this.polling = polling;
        this.maxWR = maxWR;
        this.maxSge = maxSge;
        this.cqSize = cqSize;
    }


    @Override
    public RdmaCqProvider createCqProvider(RdmaRpcEndpoint endpoint) throws IOException {
        logger.info("setting up cq processor");
        IbvContext context = endpoint.getIdPriv().getVerbs();
        if (context != null) {
            logger.info("setting up cq processor, context found");
            RdmaActiveCqProcessor<RdmaRpcEndpoint> cqProcessor = null;
            if (!endpointProcessorMap.containsKey(endpoint)) {
                cqProcessor = new RdmaActiveCqProcessor<RdmaRpcEndpoint>(endpoint.getIdPriv().getVerbs(),
                        cqSize, maxWR, 0, 1, timeout, polling);
                endpointProcessorMap.put(endpoint, cqProcessor);
                cqProcessor.start();
                return cqProcessor;
            }
            cqProcessor = endpointProcessorMap.get(endpoint);
            return cqProcessor;
        } else {
            throw new IOException("setting up cq processor, no context found");
        }
    }

    @Override
    public IbvQP createQpProvider(RdmaRpcEndpoint endpoint) throws IOException{
        IbvContext context = endpoint.getIdPriv().getVerbs();
        RdmaActiveCqProcessor<RdmaRpcEndpoint> cqProcessor = endpointProcessorMap.get(endpoint);
        IbvCQ cq = cqProcessor.getCQ();

        IbvQPInitAttr attr = new IbvQPInitAttr();
        attr.cap().setMax_recv_sge(maxSge);
        attr.cap().setMax_recv_wr(maxWR);
        attr.cap().setMax_send_sge(maxSge);
        attr.cap().setMax_send_wr(maxWR);
        attr.setQp_type(IbvQP.IBV_QPT_RC);
        attr.setRecv_cq(cq);
        attr.setSend_cq(cq);
        IbvQP qp = endpoint.getIdPriv().createQP(endpoint.getPd(), attr);

        logger.info("registering endpoint with cq");

        cqProcessor.registerQP(qp.getQp_num(), endpoint);
        return qp;
    }
}

