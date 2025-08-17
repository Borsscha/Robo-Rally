package de.lmu.cleverecousins.protocol.messageBody;

public class SendChatBody {
    private String message;
    private int to;

    public SendChatBody() {};
    public SendChatBody(String message, int to) {
        this.message = message;
        this.to = to;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTo(){
        return this.to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}

