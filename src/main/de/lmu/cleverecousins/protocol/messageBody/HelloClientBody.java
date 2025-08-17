package de.lmu.cleverecousins.protocol.messageBody;

public class HelloClientBody {
    private String protocol;

    public HelloClientBody() {};
    public HelloClientBody(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}

