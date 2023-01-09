package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import java.util.HashMap;

public class StompMessageProtocolImpl implements StompMessagingProtocol<AbstractStompFrame> {
    private Connections<AbstractStompFrame> connections;
    private int connectionId;
    @Override
    public void start(int connectionId, Connections<AbstractStompFrame> connections) {
        //initiates the connections list in the protocol
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(AbstractStompFrame message) {
        //noteToSelf: gets a frame and handles it according to the frame type,
        // consider making a function for each frame and calling them in process


    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }


}
