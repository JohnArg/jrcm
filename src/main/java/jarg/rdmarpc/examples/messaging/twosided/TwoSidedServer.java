package jarg.rdmarpc.examples.messaging.twosided;

import com.ibm.disni.RdmaActiveEndpointGroup;
import com.ibm.disni.RdmaServerEndpoint;
import jarg.rdmarpc.connections.RpcBasicEndpoint;
import jarg.rdmarpc.connections.WorkCompletionHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class TwoSidedServer {

    private String serverHost;
    private String serverPort;
    private RdmaActiveEndpointGroup<RpcBasicEndpoint> endpointGroup;
    private ServerEndpointFactory factory;
    private RdmaServerEndpoint<RpcBasicEndpoint> serverEndpoint;
    private WorkCompletionHandler completionHandler;
    private List<RpcBasicEndpoint> clients;

    public TwoSidedServer(String host, String port){
        this.serverHost = host;
        this.serverPort = port;
    }

    public void init() throws Exception {
        // Settings
        int timeout = 1000;
        boolean polling = false;
        int maxWRs = 10;
        int cqSize = maxWRs;
        int maxSge = 1;
        int maxBufferSize = 200;

        clients = new ArrayList<>();
        // Create endpoint
        endpointGroup = new RdmaActiveEndpointGroup<>(timeout, polling,
                maxWRs, maxSge, cqSize);
        factory = new ServerEndpointFactory(endpointGroup, maxBufferSize, maxWRs);
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

        while(true){
            // accept client connection
            RpcBasicEndpoint clientEndpoint = serverEndpoint.accept();
            clients.add(clientEndpoint);

            System.out.println("Client connection accepted. Client : "
                    + clientEndpoint.getDstAddr().toString());
        }


    }

    public void finalize(){
        //Cleanup -------------------------------------------
        try {
            for (RpcBasicEndpoint clientEndpoint : clients) {
                clientEndpoint.close();
            }
            serverEndpoint.close();
            endpointGroup.close();
            System.out.println("Server is shut down");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
