package bgu.spl.net.impl.stomp;

import java.util.Map;

public class ConnectFrame extends AbstractStompFrame {
    public Manager manager;
    public ConnectFrame(Map<String, String> headers, String body) {
        super("CONNECT", headers, body);
    }

    @Override
    public String getFrameType() {
        return "CONNECT";
    }

    @Override
    public AbstractStompFrame response() {
      //implement
        return null;
    }
}
