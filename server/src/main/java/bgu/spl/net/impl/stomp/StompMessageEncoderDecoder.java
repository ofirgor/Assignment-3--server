package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StompMessageEncoderDecoder implements MessageEncoderDecoder<AbstractStompFrame> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private final char NULL_CHAR = '\u0000';

    @Override
    public AbstractStompFrame decodeNextByte(byte nextByte) {
        if (nextByte == NULL_CHAR)
            return popFrame();

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(AbstractStompFrame message) {
        return (message.toString() + NULL_CHAR).getBytes();
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    private AbstractStompFrame popFrame() {
        //full message received, we split it to lines
        String[] frameLines = popString().split("\n");
        String frameType = frameLines[0];

        //create a map to hold the headers
        Map<String, String> headers = new HashMap<>();

        //parse the headers
        int i = 1;
        while (i < frameLines.length && !frameLines[i].isEmpty()) {
            String[] headersParts = frameLines[i].split(":");
            String key = headersParts[0];
            String val = headersParts[1];
            headers.put(key, val);
            i++;
        }

        //get the body
        StringBuilder bodyStringBuilder = new StringBuilder();
        while (i < frameLines.length && frameLines[i] != null) {
            bodyStringBuilder.append(frameLines[i]).append("\n");
            i++;
        }
        String body = bodyStringBuilder.toString().trim();

        //creat the frame object
        AbstractStompFrame frame;
        switch (frameType) {
            case "CONNECT":
                frame = new ConnectFrame(headers, body);
                break;
            case "CONNECTED":
                frame = new ConnectedFrame(headers, body);
                break;
            case "ERROR":
                frame = new ErrorFrame(headers, body);
                break;
            case "MESSAGE":
                frame = new MessageFrame(headers, body);
                break;
            case "RECEIPT":
                frame = new ReceiptFrame(headers, body);
                break;
            default:
                throw new IllegalArgumentException("Unsupported frame type: " + frameType);
        }
        return frame;
    }
}
