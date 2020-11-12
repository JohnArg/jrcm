package jarg.examples.messaging;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaServerEndpoint;
import jarg.concerrt.connections.ConceRRTEndpoint;
import jarg.concerrt.connections.ConceRRTEndpointFactory;
import jarg.concerrt.connections.WorkRequestData;
import jarg.concerrt.requests.WorkRequestTypes;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TwoSidedServer {

    private String serverHost;
    private String serverPort;
    private RdmaActiveEndpointGroup<ConceRRTEndpoint> endpointGroup;
    private ConceRRTEndpointFactory factory;
    private RdmaServerEndpoint<ConceRRTEndpoint> serverEndpoint;

    public TwoSidedServer(String host, String port){
        this.serverHost = host;
        this.serverPort = port;
    }

    public void init() throws Exception {
        // Settings
        int timeout = 1000;
        boolean polling = false;
        int maxWRs = 128;
        int cqSize = maxWRs;
        int maxSge = 1;
        int maxBufferSize = 200;

        // Create endpoint
        endpointGroup = new RdmaActiveEndpointGroup<>(timeout, polling,
                maxWRs, maxSge, cqSize);
        factory = new ConceRRTEndpointFactory(endpointGroup, maxBufferSize, maxWRs,
                WorkRequestTypes.TWO_SIDED_SIGNALED);
        endpointGroup.init(factory);
        serverEndpoint = endpointGroup.createServerEndpoint();

        // bind server to address/port
        InetAddress serverIp = InetAddress.getByName(serverHost);
        InetSocketAddress serverSockAddr = new InetSocketAddress(serverIp,
                Integer.parseInt(serverPort));
        serverEndpoint.bind(serverSockAddr, 10);
        System.out.println("Server bound to address : "
                + serverSockAddr.toString());
    }

    public void operate() throws Exception {

        // accept client connection
        ConceRRTEndpoint clientEndpoint = serverEndpoint.accept();
        // prepare for two sided operations on that endpoint

//
//        System.out.println("Client connection accepted. Client : "
//                + clientEndpoint.getDstAddr().toString());
//
//        // get response
//        String message = clientEndpoint.getTextMessage();
//        if(message == null){
//            System.out.println("Error in getting message");
//        }else{
//            System.out.println(message);
//        }
//
//        // send message
//        clientEndpoint.sendTextMessage("Hello back!");


        //close endpoint/group
        clientEndpoint.close();
        serverEndpoint.close();
        endpointGroup.close();

    }
}
