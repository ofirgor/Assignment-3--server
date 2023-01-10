package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConnectionsImpl<T> implements Connections<T> {
    public HashMap<Integer,ConnectionHandler<T>> activeUsers = new HashMap<>();
    public static int connectionsIdCounter; //use atomic integer
    private Manager manager; // noteToSelf: need to initiate manager

    public ConnectionsImpl(){
        connectionsIdCounter ++;
    }
    @Override
    public boolean send(int connectionId, T msg) {
        if (activeUsers.containsKey(connectionId)) {
            activeUsers.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) {
        for (Map.Entry<Integer,ConnectionHandler<T>> entry: activeUsers.entrySet()) {
            if (manager.isUserInChannel(entry.getKey(), channel))
                entry.getValue().send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        activeUsers.remove(connectionId);
    }

    public boolean isLoggedIn(int connectionsId){
        return activeUsers.containsKey(connectionsId);
    }

}
