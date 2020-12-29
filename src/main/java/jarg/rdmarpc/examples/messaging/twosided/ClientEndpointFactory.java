package jarg.rdmarpc.examples.messaging.twosided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.rdmarpc.connections.RpcBasicEndpoint;
import jarg.rdmarpc.requests.WorkRequestTypes;

import java.io.IOException;

public class ClientEndpointFactory implements RdmaEndpointFactory<RpcBasicEndpoint> {

    private RdmaActiveEndpointGroup<RpcBasicEndpoint> endpointGroup;
    private int maxBufferSize;
    private int maxWRs;
    private int maxAcks;

    public ClientEndpointFactory(RdmaActiveEndpointGroup<RpcBasicEndpoint> endpointGroup,
                                 int maxBufferSize, int maxWRs, int maxAcks) {
        this.endpointGroup = endpointGroup;
        this.maxBufferSize = maxBufferSize;
        this.maxWRs = maxWRs;
        this.maxAcks = maxAcks;
    }

    @Override
    public RpcBasicEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
        ClientCompletionHandler handler = new ClientCompletionHandler(maxAcks);
        return new RpcBasicEndpoint(endpointGroup, id, serverSide, maxBufferSize, maxWRs,
                WorkRequestTypes.TWO_SIDED_SIGNALED, handler);
    }
}
