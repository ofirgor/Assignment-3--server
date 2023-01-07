package bgu.spl.net.impl.stomp;

import java.util.Map;

public class MessageFrame extends AbstractStompFrame{
    public MessageFrame(Map<String, String> headers, String body) {
        super("MESSAGE", headers, body);
    }

    @Override
    public String getFrameType() {
        return "MESSAGE";
    }
}
