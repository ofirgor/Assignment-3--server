package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
        ConnectionsImpl<StompFrame> connections = new ConnectionsImpl<>();
        Manager manager = new Manager();
        switch (args[1]) {
            case "tpc":
                Server.threadPerClient(
                7777, //port
                StompMessageProtocolImpl::new, //protocol factory
                StompMessageEncoderDecoder::new, //message encoder decoder factory
                connections, manager).serve();
            break;
            case "reactor":
                Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    7777, //port
                    StompMessageProtocolImpl::new, //protocol factory
                    StompMessageEncoderDecoder::new, //message encoder decoder factory
                    connections, manager).serve();
        }
    }

}
