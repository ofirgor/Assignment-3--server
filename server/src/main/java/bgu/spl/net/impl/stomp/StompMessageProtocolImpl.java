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
    public void start(int connectionId, Connections<StompFrame> connections) {
        //initiates the connections object in the protocol
        this.connectionId = connectionId;
        this.connections = connections;
        shouldTerminate = false;

    }
    public static int getNewMessageId(){
        return messageCounter.getAndIncrement();
    }

    @Override
    public void process(StompFrame message) {
        StompFrame responseMsg = null;
        String frameType = message.getFrameType();
        switch (frameType){
            case "CONNECT":
                try {
                    String version = message.getHeaderByKey("accept - version");
                    String userName = message.getHeaderByKey("login");
                    String pass = message.getHeaderByKey("passcode");
                    String host = message.getHeaderByKey("host");

                    if (!manager.isUserNameExist(userName)) {
                        manager.addUser(connectionId);
                        manager.addUserNameAndPass(userName, pass);
                    } else {
                        if (!manager.isCorrectPass(userName, pass))
                            throw new FrameException("wrong password", message);
                        else if (connections.isLoggedIn(connectionId))
                            throw new FrameException("user already logged in", message);

                    }
                    HashMap<String, String> connectedHeaders = new HashMap<>();
                    connectedHeaders.put("version",version);
                    responseMsg = new StompFrame("CONNECTED", connectedHeaders, "");

                }
                catch (FrameException ex){
                    connections.send(connectionId, ex.makeErrorFrame());
                }
                break;
            case "DISCONNECT":
                    manager.emptyUserChannels(connectionId);
                    manager.removeUserFromChannels(connectionId);
                    connections.disconnect(connectionId);
                    HashMap<String,String> receiptHeaders = new HashMap<>();
                    try {
                        receiptHeaders.put("receipt - id", message.getHeaderByKey("receipt"));
                    }
                    catch (FrameException ex){
                        connections.send(connectionId,ex.makeErrorFrame());
                    }
                    responseMsg = new StompFrame("RECEIPT",receiptHeaders,"");
                    shouldTerminate = true;
                    break;
            case "SUBSCRIBE":
                try {
                    String[] valueParts = message.getHeaderByKey("destination").split("/");
                    String topic = valueParts[valueParts.length-1];
                    String id = (message.getHeaderByKey("id"));
                    String receipt = message.getHeaderByKey("receipt");
                    Integer subId = manager.getSubscriptionId(connectionId,id,message);
                    manager.subscribeUser(connectionId, topic,subId);
                    HashMap<String,String> receiptSubscribedHeaders = new HashMap<>();
                    receiptSubscribedHeaders.put("receipt - id",receipt);
                    responseMsg = new StompFrame("RECEIPT", receiptSubscribedHeaders,"");

                    //need to complete this~~


                }
                catch (FrameException ex){
                    connections.send(connectionId,ex.makeErrorFrame());
                }
                break;
            case "UNSUBSCRIBE":
                    try{
                        HashMap<String,String> receiptUnsubscribedHeaders = new HashMap<>();
                        Integer id = manager.getSubscriptionId(connectionId,message.getHeaderByKey("id"),message);
                        manager.removeUserFromChannels(connectionId);
                        manager.removeChannel(connectionId,id,message);
                        receiptUnsubscribedHeaders.put("receipt - id", String.valueOf(id));
                        responseMsg = new StompFrame("RECEIPT", receiptUnsubscribedHeaders,"");
                    }
                    catch (FrameException ex) {
                        connections.send(connectionId,ex.makeErrorFrame());
                    }
                    break;
            case "SEND":
                try {
                    String[] valueParts = message.getHeaderByKey("destination").split("/");
                    String topic = valueParts[valueParts.length-1];
                    String body = message.getBody();
                    String id = String.valueOf(manager.getSubscriptionId(connectionId,topic,message));
                    HashMap<String,String> messageHeaders = new HashMap<>();
                    messageHeaders.put("subscription", id);
                    messageHeaders.put("message - id", String.valueOf(getNewMessageId()));
                    messageHeaders.put("destination", message.getHeaderByKey("destination"));
                    StompFrame messageFrame = new StompFrame("MESSAGE",messageHeaders,body);
                    connections.send(topic,messageFrame);

                }
                catch (FrameException ex){
                    connections.send(connectionId,ex.makeErrorFrame());
                }




        }
        if (responseMsg != null)
            connections.send(connectionId, responseMsg);


    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }






}
