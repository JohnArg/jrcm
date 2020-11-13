package jarg.examples.messaging.two_sided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.connections.WorkRequestData;
import jarg.concerrt.requests.WorkRequestTypes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TwoSidedClient {

    private String serverHost;
    private String serverPort;
    private RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup;
    private ClientEndpointFactory factory;
    ConceRRTEndpoint clientEndpoint;

    public TwoSidedClient(String host, String port){
        this.serverHost = host;
        this.serverPort = port;
    }

    public void init() throws IOException {
        // Create endpoint
        endpointGroup =  new RdmaActiveEndpointGroup<>(1000, false,
                128, 4, 128);
        factory = new ClientEndpointFactory(endpointGroup,200, 100);
        endpointGroup.init(factory);
        clientEndpoint = endpointGroup.createEndpoint();
    }

    public void operate() throws Exception {

        // connect to server
        InetAddress serverIp = InetAddress.getByName(serverHost);
        InetSocketAddress serverSockAddr = new InetSocketAddress(serverIp,
                Integer.parseInt(serverPort));
        clientEndpoint.connect(serverSockAddr, 1000);
        System.out.println("Client connected to server in address : "
                + clientEndpoint.getDstAddr().toString());

        // send messages ------------------------------------
        int retries = 5;
        for(int i=0; i < retries; i++){
            // get free Work Request id for a 'send' operation
            WorkRequestData wrData = clientEndpoint.
                    getWorkRequestBlocking(ConceRRTEndpoint.PostedRequestType.SEND);
            // fill tha data buffer with data to send across
            String helloMessage = "Hello message " +  Integer.toString(i);
            ByteBuffer sendBuffer = wrData.getBuffer();
            for(int j=0; j < helloMessage.length(); j ++){
                sendBuffer.putChar(helloMessage.charAt(i));
            }
            sendBuffer.flip();
            // send the data across
            clientEndpoint.send(wrData.getId(), sendBuffer.limit(), WorkRequestTypes.TWO_SIDED_SIGNALED);
        }

    }

    public void finalize(){
        //close endpoint/group
        try {
            clientEndpoint.close();
            endpointGroup.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
