package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StompMessageProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private Connections<StompFrame> connections;
    private int connectionId;
    private Manager manager;

    private boolean shouldTerminate;
    private static AtomicInteger messageCounter=new AtomicInteger(0);
    private ConcurrentHashMap<Integer,String> loggedInUsers = new ConcurrentHashMap<>();
    @Override
    public void start(int connectionId, Connections<StompFrame> connections,Manager manager) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.manager = manager;
        shouldTerminate = false;

    }
    public static int getNewMessageId(){
        return messageCounter.getAndIncrement();
    }

    @Override
    public void process(StompFrame message) {
        StompFrame responseMsg = null;
        String frameType = message.getFrameType();
        try {
            switch (frameType) {
                case "CONNECT":
                    responseMsg = connect(connectionId, message);
                    break;
                case "DISCONNECT":
                    responseMsg = disconnect(connectionId, message);
                    shouldTerminate = true;
                    break;
                case "SUBSCRIBE":
                    subscribe(connectionId, message);
                    break;
                case "UNSUBSCRIBE":
                    unsubscribe(connectionId, message);
                    break;
                case "SEND":
                    send(connectionId,message);
                    break;
            }
            if (responseMsg != null)
                connections.send(connectionId, responseMsg);
        } catch (FrameException ex) {
            ex.printStackTrace();
            connections.send(connectionId, ex.makeErrorFrame());
        }
    }

    public StompFrame connect(int connectionId, StompFrame message) throws FrameException{
        String version = message.getHeaderByKey("accept-version");
        String userName = message.getHeaderByKey("login");
        String pass = message.getHeaderByKey("passcode");
        manager.addUser(connectionId);
        message.getHeaderByKey("host"); //just to check it is a valid header
        if (!manager.isUserNameExist(userName)) {
            manager.addUserNameAndPass(userName, pass);
            loggedInUsers.putIfAbsent(connectionId, userName);
        } else {
            if (!manager.isCorrectPass(userName, pass)) {
                System.out.println("wrong password");
                throw new FrameException("wrong password", message);
            } else if (loggedInUsers.putIfAbsent(connectionId, userName) != null) {
                System.out.println("user already logged in");
                throw new FrameException("user already logged in", message);
            }

        }
        tryGetReceiptId(connectionId, message);
        HashMap<String, String> connectedHeaders = new HashMap<>();
        connectedHeaders.put("version", version);
        return new StompFrame("CONNECTED", connectedHeaders, "");
    }

    public StompFrame disconnect(int connectionId, StompFrame message) throws FrameException {
        manager.emptyUserChannels(connectionId);
        HashMap<String, String> receiptHeaders = new HashMap<>();
        receiptHeaders.put("receipt-id", message.getHeaderByKey("receipt"));
        if (loggedInUsers.remove(connectionId) == null)
            throw new FrameException("user is not connected", message);
        connections.disconnect(connectionId);
        System.out.println("user disconnected");
        return new StompFrame("RECEIPT", receiptHeaders, "");
    }

    public void subscribe(int connectionId, StompFrame message) throws FrameException {
        String[] valueParts = message.getHeaderByKey("destination").split("/");
        String topic = valueParts[valueParts.length - 1];
        String id = (message.getHeaderByKey("id"));
        tryGetReceiptId(connectionId, message);
        Integer subId = manager.parseToInt(id,message);
        manager.subscribeUser(connectionId, topic, subId);
    }

    public void unsubscribe(int connectionId, StompFrame message) throws FrameException{
        manager.unsubscribeUser(connectionId,message.getHeaderByKey("id"),message);
        tryGetReceiptId(connectionId, message);
    }

    public void send(int connectionId, StompFrame message) throws FrameException {
        String[] valueParts = message.getHeaderByKey("destination").split("/");
        String topic = valueParts[valueParts.length - 1];
        String body = message.getBody();
        Integer id = manager.getSubscriptionId(connectionId,topic,message);
        HashMap<String, String> messageHeaders = new HashMap<>();
        messageHeaders.put("subscription", String.valueOf(id));
        messageHeaders.put("message - id", String.valueOf(getNewMessageId()));
        messageHeaders.put("destination", message.getHeaderByKey("destination"));
        StompFrame messageFrame = new StompFrame("MESSAGE", messageHeaders, body);
        tryGetReceiptId(connectionId, message);
        connections.send(topic, messageFrame);
    }

    private void tryGetReceiptId(int connectionId, StompFrame message) {
        String receiptId="";
        try{
            receiptId = message.getHeaderByKey("receipt");
        }
        catch (FrameException ignored){}
        if (!receiptId.equals("")) {
            HashMap<String, String> receiptHeaders = new HashMap<>();
            receiptHeaders.put("receipt-id", receiptId);
            connections.send(connectionId, new StompFrame("RECEIPT",receiptHeaders,""));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }






}
