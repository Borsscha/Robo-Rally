package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReceivedChatBody {

    private String message;
    private int from;
    private String fromName;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    public ReceivedChatBody() {}

    public ReceivedChatBody(int from, String fromName, String message) {
        this(from, fromName, message, false);
    }

    public ReceivedChatBody(int from, String fromName, String message, boolean isPrivate) {
        this.from = from;
        this.fromName = fromName;
        this.message = message;
        this.isPrivate = isPrivate;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @Override
    public String toString() {
        return (isPrivate ? "[privat] " : "") + fromName + ": " + message;
    }
}
