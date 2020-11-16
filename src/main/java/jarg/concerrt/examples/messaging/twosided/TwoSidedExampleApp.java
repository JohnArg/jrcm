package jarg.concerrt.examples.messaging.twosided;

import java.io.IOException;

public class TwoSidedExampleApp {

    public static void main(String[] args) {
        if(args.length < 3){
            System.out.println("Please provide a role (s for server, c for client),"+
                    " an ip (not localhost) and a port.");
            System.out.println("E.g. for the server run with args : s 10.0.2.4 3000");
            System.out.println("E.g. for a client of the previous server run with args : c 10.0.2.4 3000");
            System.exit(1);
        }

        String role = args[0];
        String host = args[1];
        String port = args[2];

        // run as a server
        if(role.equals("s")){
            TwoSidedServer server = new TwoSidedServer(host, port);
            try {
                server.init();
                server.operate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(role.equals("c")) { // run as a client
            TwoSidedClient client = new TwoSidedClient(host, port);
            try {
                client.init();
                client.operate();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
