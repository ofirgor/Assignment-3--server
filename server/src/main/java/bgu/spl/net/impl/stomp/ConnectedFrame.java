package bgu.spl.net.impl.stomp;

import java.util.Map;

public class ConnectedFrame extends AbstractStompFrame {
    public ConnectedFrame(Map<String, String> headers, String body) {
        super("CONNECTED", headers, body);
    }

    @Override
    public String getFrameType() {
        return "CONNECTED";
    }

    @Override
    public void execute() {

    }
}
