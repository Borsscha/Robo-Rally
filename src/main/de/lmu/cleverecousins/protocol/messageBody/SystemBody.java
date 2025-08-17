package de.lmu.cleverecousins.protocol.messageBody;

public class SystemBody {
    private String message;

    public SystemBody() {}

    public SystemBody(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
