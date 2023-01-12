package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StompMessageProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private Connections<StompFrame> connections;
    private int connectionId;
    private Manager manager;

    private boolean shouldTerminate;
    private static AtomicInteger messageCounter=new AtomicInteger(0);
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
                    responseMsg = subscribe(connectionId, message);
                    break;
                case "UNSUBSCRIBE":
                    responseMsg = unsubscribe(connectionId, message);
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
        String host = message.getHeaderByKey("host");
        tryGetReceiptId(connectionId, message);
        if (!manager.isUserNameExist(userName)) {
            manager.addUser(connectionId);
            manager.addUserNameAndPass(userName, pass);
        } else {
            if (!manager.isCorrectPass(userName, pass)) {
                System.out.println("wrong password");
                throw new FrameException("wrong password", message);
            } else if (connections.isLoggedIn(connectionId)) {
                System.out.println("user already logged in");
                throw new FrameException("user already logged in", message);
            }

        }
        HashMap<String, String> connectedHeaders = new HashMap<>();
        connectedHeaders.put("version", version);
        return new StompFrame("CONNECTED", connectedHeaders, "");
    }

    public StompFrame disconnect(int connectionId, StompFrame message) throws FrameException {
        manager.emptyUserChannels(connectionId);
        connections.disconnect(connectionId);
        HashMap<String, String> receiptHeaders = new HashMap<>();
        receiptHeaders.put("receipt-id", message.getHeaderByKey("receipt"));

        return new StompFrame("RECEIPT", receiptHeaders, "");
    }

    public StompFrame subscribe(int connectionId, StompFrame message) throws FrameException {
        String[] valueParts = message.getHeaderByKey("destination").split("/");
        String topic = valueParts[valueParts.length - 1];
        String id = (message.getHeaderByKey("id"));
        String receipt = message.getHeaderByKey("receipt");
        Integer subId = manager.parseToInt(id,message);
        manager.subscribeUser(connectionId, topic, subId);
        HashMap<String, String> receiptHeaders = new HashMap<>();
        receiptHeaders.put("receipt-id", receipt);
        return new StompFrame("RECEIPT", receiptHeaders, "");
    }

    public StompFrame unsubscribe(int connectionId, StompFrame message) throws FrameException{
        HashMap<String, String> receiptHeaders = new HashMap<>();
        manager.unsubscribeUser(connectionId,message.getHeaderByKey("id"),message);
        receiptHeaders.put("receipt-id", message.getHeaderByKey("id"));
        return new StompFrame("RECEIPT", receiptHeaders, "");
    }

    public void send(int connectionId, StompFrame message) throws FrameException {
        tryGetReceiptId(connectionId, message);
        String[] valueParts = message.getHeaderByKey("destination").split("/");
        String topic = valueParts[valueParts.length - 1];
        String body = message.getBody();
        Integer id = manager.getSubscriptionId(connectionId,topic,message);
        HashMap<String, String> messageHeaders = new HashMap<>();
        messageHeaders.put("subscription", String.valueOf(id));
        messageHeaders.put("message - id", String.valueOf(getNewMessageId()));
        messageHeaders.put("destination", message.getHeaderByKey("destination"));
        StompFrame messageFrame = new StompFrame("MESSAGE", messageHeaders, body);
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
