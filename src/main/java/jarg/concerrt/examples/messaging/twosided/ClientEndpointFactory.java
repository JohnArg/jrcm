package jarg.concerrt.examples.messaging.twosided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.requests.WorkRequestTypes;

import java.io.IOException;

public class ClientEndpointFactory implements RdmaEndpointFactory<ConceRRTEndpoint> {

    private RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup;
    private int maxBufferSize;
    private int maxWRs;
    private int maxAcks;

    public ClientEndpointFactory(RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup,
                                 int maxBufferSize, int maxWRs, int maxAcks) {
        this.endpointGroup = endpointGroup;
        this.maxBufferSize = maxBufferSize;
        this.maxWRs = maxWRs;
        this.maxAcks = maxAcks;
    }

    @Override
    public ConceRRTEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
        ClientCompletionHandler handler = new ClientCompletionHandler(maxAcks);
        return new ConceRRTEndpoint(endpointGroup, id, serverSide, maxBufferSize, maxWRs,
                WorkRequestTypes.TWO_SIDED_SIGNALED, handler);
    }
}
