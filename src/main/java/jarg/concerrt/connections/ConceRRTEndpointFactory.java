package jarg.concerrt.connections;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;

import java.io.IOException;

/**
 * Generates {@link ConceRRTEndpoint ConceRRTEndpoints}. Used by {@link RdmaActiveEndpointGroup
 * RdmaActiveEndpointGroup} implementations.
 */
public class ConceRRTEndpointFactory implements RdmaEndpointFactory<ConceRRTEndpoint> {
    private RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup;
    private int maxBufferSize;
    private int maxWRs;
    private int supportedOperationsFlag;

    public ConceRRTEndpointFactory(RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup,
                                   int maxBufferSize, int maxWRs, int supportedOperationsFlag) {
        this.endpointGroup = endpointGroup;
        this.maxBufferSize = maxBufferSize;
        this.maxWRs = maxWRs;
        this.supportedOperationsFlag = supportedOperationsFlag;
    }

    @Override
    public ConceRRTEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
        return new ConceRRTEndpoint(endpointGroup, id, serverSide, maxBufferSize,
                maxWRs, supportedOperationsFlag);
    }
}
