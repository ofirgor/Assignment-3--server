package bgu.spl.net.impl.stomp;

import java.util.Map;

public class SubscribeFrame extends AbstractStompFrame{
    public SubscribeFrame(Map<String, String> headers, String body) {
        super("SUBSCRIBE", headers, body);
    }

    @Override
    public String getFrameType() {
        return "SUBSCRIBE";
    }

    @Override
    public void execute() {

    }
}
