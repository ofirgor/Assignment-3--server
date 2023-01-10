package bgu.spl.net.impl.stomp;
import java.util.HashMap;

public class FrameException extends Exception{
    public StompFrame frame;
    public FrameException(String msg){
        super(msg);
        frame = null;
    }
    public FrameException(String msg, StompFrame frame){
        super(msg);
        this.frame = frame;
    }
    public StompFrame makeErrorFrame(){
        HashMap<String,String> errorHeaders = new HashMap<>();
        String body ="";
        if (frame.getHeaders().containsKey("receipt - id"))
            errorHeaders.put("receipt - id", frame.getHeaders().get("receipt - id"));
        errorHeaders.put("message", super.getMessage());
        String theMessageValue = "\n" + "-----" + frame.toString() + "-----";
        errorHeaders.put("The message", theMessageValue);
        return new StompFrame("ERROR",errorHeaders, theMessageValue);
    }
}
