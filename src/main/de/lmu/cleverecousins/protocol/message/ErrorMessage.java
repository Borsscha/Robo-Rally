// ErrorMessage.java
package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ErrorBody;

@JsonTypeName("Error")
public class ErrorMessage extends BaseMessage<ErrorBody> {
    public ErrorMessage() {}
    public ErrorMessage(ErrorBody body) {
        super(body);
    }
}


