package bgu.spl.net.impl.stomp;

import java.util.Map;

public class ErrorFrame extends StompFrame {
    public ErrorFrame(Map<String, String> headers, String body){
        super("ERROR", headers, body);
    }
    @Override
    public String getFrameType() {
        return "ERROR";
    }

}
