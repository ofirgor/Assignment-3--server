package bgu.spl.net.impl.stomp;

import java.util.Map;

public class DisconnectFrame extends AbstractStompFrame{
    public DisconnectFrame(Map<String, String> headers, String body) {
        super("DISCONNECT", headers, body);
    }

    @Override
    public String getFrameType() {
        return "DISCONNECT";
    }

    @Override
    public void execute() {

    }
}
