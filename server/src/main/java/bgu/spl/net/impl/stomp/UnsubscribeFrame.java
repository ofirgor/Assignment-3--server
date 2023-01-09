package bgu.spl.net.impl.stomp;

import java.util.Map;

public class UnsubscribeFrame extends AbstractStompFrame{
    public UnsubscribeFrame(Map<String, String> headers, String body) {
        super("UNSUBSCRIBE", headers, body);
    }

    @Override
    public String getFrameType() {
        return "UNSUBSCRIBE";
    }

    @Override
    public AbstractStompFrame response() {
        return null;
    }
}
