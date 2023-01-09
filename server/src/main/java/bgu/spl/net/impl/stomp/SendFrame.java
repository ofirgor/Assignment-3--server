package bgu.spl.net.impl.stomp;

import java.util.Map;

public class SendFrame extends AbstractStompFrame{
    public SendFrame(Map<String, String> headers, String body) {
        super("SEND", headers, body);
    }

    @Override
    public String getFrameType() {
        return "SEND";
    }

    @Override
    public void execute() {

    }
}
