package bgu.spl.net.impl.stomp;

import java.util.Map;
public class StompFrame {
    protected String frameType;
    private Map<String,String> headers;
    private String body;
    public final char NULL_CHAR = '\u0000';

    public StompFrame(){
        this.frameType = null;
        this.headers = null;
        this.body = null;
    }
    public StompFrame(String command, Map<String, String> headers, String body) {
        this.frameType = command;
        this.headers = headers;
        this.body = body + "\n" + NULL_CHAR;
    }

    public String getFrameType(){
        return frameType;
    }
    public String getBody(){
        return body;
    }
    public Map<String,String> getHeaders(){
        return headers;
    }
    public void setFrameType(String frameType){
        this.frameType = frameType;
    }
    public void setBody(String body){
        this.body = body;
    }
    public void setHeaders(String key, String value){
        headers.put(key, value);
    }
    public String getHeaderByKey(String headerName) throws FrameException{
        if(headers.containsKey(headerName))
            return headers.get(headerName);
        throw new FrameException("No " + headerName+ " key", this);
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.frameType).append("\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("\n").append(body).append("\n");
        return sb.toString();
    }

}
