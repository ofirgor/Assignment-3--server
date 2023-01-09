package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import java.util.HashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    public HashMap<Integer,ConnectionHandler<T>> activeUsers = new HashMap<>();
    @Override
    //the send functions need to call the send function in connection handler
    public boolean send(int connectionId, T msg) {
        //need to use the send() function in connection handler
        return false;
    }

    @Override
    public void send(String channel, T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
