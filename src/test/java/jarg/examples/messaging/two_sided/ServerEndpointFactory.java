package jarg.examples.messaging.two_sided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.requests.WorkRequestTypes;

import java.io.IOException;

public class ServerEndpointFactory implements RdmaEndpointFactory<ConceRRTEndpoint> {
    private RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup;
    private int maxBufferSize;
    private int maxWRs;

    public ServerEndpointFactory(RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup,
                                 int maxBufferSize, int maxWRs) {
        this.endpointGroup = endpointGroup;
        this.maxBufferSize = maxBufferSize;
        this.maxWRs = maxWRs;
    }

    @Override
    public ConceRRTEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
        return new ConceRRTEndpoint(endpointGroup, id, serverSide, maxBufferSize, maxWRs,
                WorkRequestTypes.TWO_SIDED_SIGNALED, new ServerCompletionHandler());
    }
}
