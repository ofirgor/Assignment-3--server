package bgu.spl.net.impl.stomp;

import java.util.Map;

public class ReceiptFrame extends AbstractStompFrame{
    public ReceiptFrame(Map<String, String> headers, String body) {
        super("RECEIPT", headers, body);
    }

    @Override
    public String getFrameType() {
        return "RECEIPT";
    }
}
