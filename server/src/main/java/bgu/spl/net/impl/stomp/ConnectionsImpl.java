package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionsImpl<T> implements Connections<T> {
    public ConcurrentHashMap<Integer,ConnectionHandler<T>> activeUsers;
    private static AtomicInteger connectionsIdCounter;
    private Manager manager; // noteToSelf: need to initiate manager
    private ReadWriteLock readWriteLock;


    public ConnectionsImpl(){
        activeUsers = new ConcurrentHashMap<>();
        connectionsIdCounter=new AtomicInteger(0);
        readWriteLock = new ReentrantReadWriteLock();
    } public int incAndGetIdCount(){
        return connectionsIdCounter.incrementAndGet();
    }
    @Override
    public boolean send(int connectionId, T msg) {
        readWriteLock.readLock().lock();
        try {
            if (activeUsers.containsKey(connectionId)) {
                activeUsers.get(connectionId).send(msg);
                return true;
            }
            else
                return false;
        }
        finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public void send(String channel, T msg) {
        readWriteLock.readLock().lock();
        for (Map.Entry<Integer,ConnectionHandler<T>> entry: activeUsers.entrySet()) {
                entry.getValue().send(msg);
        }
        readWriteLock.readLock().unlock();
    }

    @Override
    public void disconnect(int connectionId) {
        activeUsers.remove(connectionId);
    }
    public void connect(int connectionId, ConnectionHandler<T> connectionHandler){
        activeUsers.putIfAbsent(connectionId, connectionHandler);
    }

    public boolean isLoggedIn(int connectionsId){
        return activeUsers.containsKey(connectionsId);
    }

}
