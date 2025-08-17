package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloServerBody {
    private String group;

    @JsonProperty("isAI")
    private boolean ai;
    private String protocol;

    public HelloServerBody() {}
    public HelloServerBody(String group, boolean ai, String protocol) {
        this.group = group;
        this.ai = ai;
        this.protocol = protocol;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isAI() {
        return ai;
    }

    public void setAI(boolean ai) {
        this.ai = ai;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}


