package jarg.rdmarpc.examples.messaging.twosided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import jarg.rdmarpc.connections.RpcBasicEndpoint;
import jarg.rdmarpc.connections.WorkRequestData;
import jarg.rdmarpc.requests.WorkRequestTypes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TwoSidedClient {

    private String serverHost;
    private String serverPort;
    private RdmaActiveEndpointGroup<RpcBasicEndpoint> endpointGroup;
    private ClientEndpointFactory factory;
    RpcBasicEndpoint clientEndpoint;
    private int messagesToSend;

    public TwoSidedClient(String host, String port){
        this.serverHost = host;
        this.serverPort = port;
    }

    public void init() throws IOException {
        // Settings
        int timeout = 1000;
        boolean polling = false;
        int maxWRs = 10;
        int cqSize = maxWRs;
        int maxSge = 1;
        int maxBufferSize = 200;
        messagesToSend = 5;
        // Create endpoint
        endpointGroup =  new RdmaActiveEndpointGroup<>(timeout, polling,
                maxWRs, maxSge, cqSize);
        factory = new ClientEndpointFactory(endpointGroup,maxBufferSize, maxWRs, messagesToSend);
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
        for(int i=0; i < messagesToSend; i++){
            // get free Work Request id for a 'send' operation
            WorkRequestData wrData = clientEndpoint.getWorkRequestBlocking();
            // fill tha data buffer with data to send across
            ByteBuffer sendBuffer = wrData.getBuffer();
            sendBuffer.putInt(wrData.getId()+10);  // use this to identify the message
            String helloMessage = "Hello message ";
            for(int j=0; j < helloMessage.length(); j ++){
                sendBuffer.putChar(helloMessage.charAt(j));
            }
            sendBuffer.flip();

            // See what you send
            int irrelevant = sendBuffer.getInt();
            String text = sendBuffer.asCharBuffer().toString();
            System.out.println("Will send : "+text);
            sendBuffer.rewind();
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
