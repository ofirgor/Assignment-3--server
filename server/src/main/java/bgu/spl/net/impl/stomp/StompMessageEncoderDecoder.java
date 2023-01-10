package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StompMessageEncoderDecoder implements MessageEncoderDecoder<StompFrame> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    public final char NULL_CHAR = '\u0000';

    @Override
    public StompFrame decodeNextByte(byte nextByte) {
        if (nextByte == NULL_CHAR)
            return popFrame();

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(StompFrame message) {
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

    private StompFrame popFrame() {
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
        StompFrame frame;
        switch (frameType) {
            case "CONNECT":
                frame = new StompFrame("CONNECT", headers, body);
                break;
            case "SEND":
                frame = new StompFrame("SEND", headers, body);
                break;
            case "SUBSCRIBE":
                frame = new StompFrame("SUBSCRIBE", headers, body);
                break;
            case "UNSUBSCRIBE":
                frame = new StompFrame("UNSUBSCRIBE", headers, body);
                break;
            case "DISCONNECT":
                frame = new StompFrame("DISCONNECT", headers, body);
                break;
            default:
                throw new IllegalArgumentException("Unsupported frame type: " + frameType);
        }
        return frame;
    }
}
