package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConnectionsImpl<T> implements Connections<T> {
    public HashMap<Integer,ConnectionHandler<T>> activeUsers = new HashMap<>();
    private Manager manager;

    @Override
    public boolean send(int connectionId, T msg) {
        activeUsers.get(connectionId).send(msg);
        return true;// noteToSelf: check when to return true
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
}
