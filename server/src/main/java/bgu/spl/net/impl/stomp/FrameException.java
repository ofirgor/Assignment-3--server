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
    public ErrorFrame makeErrorFrame(){
        HashMap<String,String> errorHeaders = new HashMap<>();
        String body ="";
        ErrorFrame errorFrame;
        if (frame.getHeaders().containsKey("receipt - id"))
            errorHeaders.put("receipt - id", frame.getHeaders().get("receipt - id"));
        errorHeaders.put("message", super.getMessage());
        String theMessageValue = "\n" + "-----" + frame.toString() + "-----";
        errorHeaders.put("The message", theMessageValue);
        errorFrame = new ErrorFrame(errorHeaders, theMessageValue);
        return errorFrame;
    }
}
