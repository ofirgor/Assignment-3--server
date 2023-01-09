package bgu.spl.net.impl.stomp;

import java.io.Serializable;
import java.util.Map;
public abstract class AbstractStompFrame implements Serializable {
    protected String frameType;
    private Map<String,String> headers;
    private String body;

    public AbstractStompFrame(String command, Map<String, String> headers, String body) {
        this.frameType = command;
        this.headers = headers;
        this.body = body;
    }

    public abstract String getFrameType();
    public String getBody(){
        return body;
    }
    public Map<String,String> getHeaders(){
        return headers;
    }
    public abstract void execute();


}