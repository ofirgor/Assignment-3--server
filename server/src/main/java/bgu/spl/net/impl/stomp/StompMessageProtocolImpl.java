package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;

public class StompMessageProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private Connections<StompFrame> connections;
    private int connectionId;
    private Manager manager;

    private boolean shouldTerminate;
    @Override
    public void start(int connectionId, Connections<StompFrame> connections) {
        //initiates the connections object in the protocol
        this.connectionId = connectionId;
        this.connections = connections;
        shouldTerminate = false;
    }

    @Override
    public void process(StompFrame message) {
        StompFrame responseMsg = new StompFrame();
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
                    connectedHeaders.put("version","1.2");
                    responseMsg = new StompFrame("CONNECTED", connectedHeaders, "");

                }
                catch (FrameException ex){
                    StompFrame errorFrame = ex.makeErrorFrame();
                    connections.send(connectionId, errorFrame);
                }
                break;
            case "DISCONNECT":
                    manager.emptyUserChannels(connectionId);
                    manager.removeUserFromChannel(connectionId);
                    connections.disconnect(connectionId);
                    HashMap<String,String> receiptHeaders = new HashMap<>();
                    try {
                        receiptHeaders.put("receipt - id", message.getHeaderByKey("receipt"));
                    }
                    catch (FrameException ex){
                        StompFrame errorFrame = ex.makeErrorFrame();
                        connections.send(connectionId, errorFrame);
                    }
                    responseMsg = new StompFrame("RECEIPT",receiptHeaders,"");
                    shouldTerminate = true;

                    break;
            case "SUBSCRIBE":
                try {
                    String[] valueParts = message.getHeaderByKey("destination").split("/");
                    String topic = valueParts[valueParts.length-1];
                    manager.subscribeUser(connectionId, topic);

                }
                catch (FrameException ex){

                }



        }
        connections.send(connectionId, responseMsg);


    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }



}
