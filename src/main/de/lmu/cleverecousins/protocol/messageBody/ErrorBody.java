// ErrorBody.java
package de.lmu.cleverecousins.protocol.messageBody;

public class ErrorBody {
    private String error;

    public ErrorBody() {}
    public ErrorBody(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

