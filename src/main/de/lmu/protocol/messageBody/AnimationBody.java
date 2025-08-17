// AnimationBody.java
package de.lmu.protocol.messageBody;

public class AnimationBody {
    private String type;

    public AnimationBody() {}

    public AnimationBody(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
