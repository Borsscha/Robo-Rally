package de.lmu.protocol.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/** the general message type. BaseMessage<T>
 By combining polymorphic deserialization with generics,
 we achieve a design that is:
 - extensible (easy to add new message types),
 - type-safe (no unchecked casts),
 - and maintainable (clear separation between message wrapper and body).*/
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "messageType"
)
@JsonSubTypes({
       /* @JsonSubTypes.Type(value = HelloClientMessage.class, name = "HelloClient"),
        @JsonSubTypes.Type(value = HelloServerMessage.class, name = "HelloServer"),
        @JsonSubTypes.Type(value = AliveMessage.class, name = "Alive"),
        @JsonSubTypes.Type(value = PlayerValuesMessage.class, name = "PlayerValues"),
        @JsonSubTypes.Type(value = WelcomeMessage.class, name = "Welcome"),
        @JsonSubTypes.Type(value = PlayerAddedMessage.class, name = "PlayerAdded"),
        @JsonSubTypes.Type(value = SendChatMessage.class, name = "SendChat"),
        @JsonSubTypes.Type(value = ReceivedChatMessage.class, name = "ReceivedChat"),
        @JsonSubTypes.Type(value = SetStatusMessage.class, name = "SetStatus"),
        @JsonSubTypes.Type(value = PlayerStatusMessage.class, name = "PlayerStatus"),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = "ErrorMessage"),*/
        @JsonSubTypes.Type(value = GameStartedMessage.class, name = "GameStarted"),
        @JsonSubTypes.Type(value = MapSelectedMessage.class, name = "MapSelected"),
        @JsonSubTypes.Type(value = SelectMapMessage.class, name = "SelectMap")
})
public abstract class BaseMessage<T> {

    @JsonProperty("messageBody")
    private T messageBody;

    public BaseMessage() {} //this no-args constructor is needed by Jackson

    public BaseMessage(T messageBody) {
        this.messageBody = messageBody;
    }


    //public abstract String getMessageType();
    public T getMessageBody() {return messageBody;}
    public void setMessageBody(T messageBody) {this.messageBody = messageBody;}
}


