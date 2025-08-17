package de.lmu.cleverecousins.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.lmu.cleverecousins.protocol.cheats.CheatMoveMessage;
import de.lmu.cleverecousins.protocol.cheats.CheatTurnMessage;
import de.lmu.cleverecousins.protocol.message.*;

/**
 * Abstract base class for all messages exchanged between client and server in the RoboRally protocol.
 * <p>
 * This class uses Jackson annotations to support polymorphic deserialization of message types
 * based on the {@code messageType} field in the JSON. Subclasses represent specific message types
 * and contain a message body of type {@code T}.
 *
 * @param <T> The type of the message body (e.g., {@code HelloClientBody}, {@code WelcomeBody}, etc.)
*/
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "messageType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HelloClientMessage.class, name = "HelloClient"),
        @JsonSubTypes.Type(value = HelloServerMessage.class, name = "HelloServer"),
        @JsonSubTypes.Type(value = AliveMessage.class, name = "Alive"),
        @JsonSubTypes.Type(value = PlayerValuesMessage.class, name = "PlayerValues"),
        @JsonSubTypes.Type(value = WelcomeMessage.class, name = "Welcome"),
        @JsonSubTypes.Type(value = PlayerAddedMessage.class, name = "PlayerAdded"),
        @JsonSubTypes.Type(value = SendChatMessage.class, name = "SendChat"),
        @JsonSubTypes.Type(value = ReceivedChatMessage.class, name = "ReceivedChat"),
        @JsonSubTypes.Type(value = SetStatusMessage.class, name = "SetStatus"),
        @JsonSubTypes.Type(value = PlayerStatusMessage.class, name = "PlayerStatus"),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = "ErrorMessage"),
        @JsonSubTypes.Type(value = GameStartedMessage.class, name = "GameStarted"),
        @JsonSubTypes.Type(value = MapSelectedMessage.class, name = "MapSelected"),
        @JsonSubTypes.Type(value = SelectMapMessage.class, name = "SelectMap"),
        @JsonSubTypes.Type(value = CurrentPlayerMessage.class, name = "CurrentPlayer"),
        @JsonSubTypes.Type(value = ActivePhaseMessage.class, name = "ActivePhase"),
        @JsonSubTypes.Type(value = SetStartingPointMessage.class, name = "SetStartingPoint"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "StartingPointTaken"),
        @JsonSubTypes.Type(value = YourCardsMessage.class, name = "YourCards"),
        @JsonSubTypes.Type(value = NotYourCardsMessage.class, name = "NotYourCards"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "ShuffleCoding"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "SelectedCard"),
        @JsonSubTypes.Type(value = CardSelectedMessage.class, name = "CardSelected"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "SelectionFinished"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "TimerStarted"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "TimerEnded"),
        @JsonSubTypes.Type(value = StartingPointTakenMessage.class, name = "CardsYouGotNow"),
        @JsonSubTypes.Type(value = CurrentCardsMessage.class, name = "CurrentCards"),
        @JsonSubTypes.Type(value = ReplaceCardMessage.class, name = "ReplaceCard"),
        @JsonSubTypes.Type(value = PlayerDisconnectedMessage.class, name = "PlayerDisconnected"), // <--- ergänzt
        //@JsonSubTypes.Type(value = ConnectionUpdateMessage.class, name = "ConnectionUpdate"),
        //@JsonSubTypes.Type(value = DrawDamageMessage.class, name = "DrawDamage"),
        @JsonSubTypes.Type(value = PickDamageMessage.class, name = "PickDamage"),
        @JsonSubTypes.Type(value = SelectedDamageMessage.class, name = "SelectedDamage"),
        //@JsonSubTypes.Type(value = PlayCardMessage.class, name = "PlayCard"),
        //@JsonSubTypes.Type(value = CardPlayedMessage.class, name = "CardPlayed"),
        //@JsonSubTypes.Type(value = MovementMessage.class, name = "Movement"),
        //@JsonSubTypes.Type(value = PlayerTurningMessage.class, name = "PlayerTurning"),
        //@JsonSubTypes.Type(value = AnimationMessage.class, name = "Animation"),
        //@JsonSubTypes.Type(value = RebootMessage.class, name = Reboot"),
        //@JsonSubTypes.Type(value = RebootDirectionMessage, name = "RebootDirection"),
        //@JsonSubTypes.Type(value = EnergyMessage, name = "Energy"),
        @JsonSubTypes.Type(value = CheckPointReachedMessage.class, name = "CheckPointReached"),
        @JsonSubTypes.Type(value = GameFinishedMessage.class, name = "GameFinished"),
        @JsonSubTypes.Type(value = RobotPositionMessage.class, name = "RobotPosition"),
        @JsonSubTypes.Type(value = GameFinishedMessage.class, name = "GameFinished"),
        @JsonSubTypes.Type(value = UsedRobotsMessage.class, name = "UsedRobots"),
        @JsonSubTypes.Type(value = CheatMoveMessage.class, name = "CheatMove"),
        @JsonSubTypes.Type(value = CheatTurnMessage.class, name = "CheatTurn")
        })

public abstract class BaseMessage<T> {

    /**
     * The body of the message, containing all relevant data.
     * Its exact type depends on the concrete message subclass.
     */
    @JsonProperty("messageBody")
    private T messageBody;

    /**
     * No-args constructor required for Jackson deserialization.
     */
    public BaseMessage() {
        // no-args constructor needed for Jackson
    }

    /**
     * Constructs a new message with the given message body.
     *
     * @param messageBody the content of the message
     */
    public BaseMessage(T messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * Returns the message body.
     *
     * @return the message body
     */
    public T getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the message body.
     *
     * @param messageBody the message body to set
     */
    public void setMessageBody(T messageBody) {
        this.messageBody = messageBody;
    }
}
