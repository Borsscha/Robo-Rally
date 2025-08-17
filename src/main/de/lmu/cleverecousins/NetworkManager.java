package de.lmu.cleverecousins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.message.*;
import de.lmu.cleverecousins.protocol.messageBody.*;

import java.io.IOException;

/**
 * Utility class for (de)serializing protocol messages to and from JSON using Jackson.
 * <p>
 * It exposes a shared, statically configured {@link ObjectMapper} that knows all
 * concrete message subtypes used by the client. Typical usage:
 * <pre>{@code
 * String json = NetworkManager.serialize(message);
 * String type = NetworkManager.getMessageType(json);
 * JsonNode body = NetworkManager.getMessageBody(json);
 * }</pre>
 * <p>
 * The class is stateless aside from the shared mapper and is therefore thread-safe as
 * long as the mapper is not reconfigured at runtime.
 */
public class NetworkManager {

    /** Shared and preconfigured Jackson mapper. */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register all concrete message classes so Jackson can deserialize by type
        objectMapper.registerSubtypes(
                HelloClientMessage.class,
                HelloServerMessage.class,
                WelcomeMessage.class,
                AliveMessage.class,

                PlayerValuesMessage.class,
                PlayerAddedMessage.class,
                SetStatusMessage.class,
                PlayerStatusMessage.class,

                SendChatMessage.class,
                ReceivedChatMessage.class,

                SelectMapMessage.class,
                MapSelectedMessage.class,

                ActivePhaseMessage.class,
                CurrentPlayerMessage.class,
                CardsYouGotNowMessage.class,
                PlayCardMessage.class,
                CardPlayedMessage.class,
                SelectedCardMessage.class,
                SelectionFinishedMessage.class,
                ReplaceCardMessage.class,
                TimerStartedMessage.class,
                TimerEndedMessage.class,

                SetStartingPointMessage.class,
                StartingPointTakenMessage.class,
                NotYourCardsMessage.class,
                YourCardsMessage.class,
                RebootDirectionMessage.class,

                PickDamageMessage.class,
                SelectedDamageMessage.class
        );
    }

    /**
     * Serializes a message object into a JSON string.
     *
     * @param message the message instance to serialize
     * @return JSON representation of the message
     * @throws IOException if Jackson fails to write the value
     */
    public static String serialize(BaseMessage<?> message) throws IOException {
        return objectMapper.writeValueAsString(message);
    }

    /**
     * Extracts the {@code messageType} field from a JSON string without fully deserializing it.
     *
     * @param json raw JSON string of a protocol message
     * @return the value of the {@code messageType} field
     * @throws IOException              if parsing fails
     * @throws IllegalArgumentException if the field is missing or null
     */
    public static String getMessageType(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode messageTypeNode = root.get("messageType");

        if (messageTypeNode == null || messageTypeNode.isNull()) {
            throw new IllegalArgumentException("Fehlendes Feld 'messageType' im JSON");
        }

        return messageTypeNode.asText();
    }

    /**
     * Provides access to the shared {@link ObjectMapper} for advanced usage.
     * Do not modify global configuration at runtime to retain thread safety.
     *
     * @return the configured object mapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Returns the JSON node contained in the {@code messageBody} field.
     *
     * @param json raw JSON string of a protocol message
     * @return the body node, or {@code null} if absent
     * @throws IOException if parsing fails
     */
    public static JsonNode getMessageBody(String json) throws IOException {
        return objectMapper.readTree(json).get("messageBody");
    }

    /**
     * Deserializes the JSON string into the specified concrete message class.
     *
     * @param json  the raw JSON message
     * @param clazz the concrete {@link BaseMessage} subclass to deserialize to
     * @return the deserialized message instance
     * @throws IOException if Jackson cannot read or bind the JSON
     */
    public static BaseMessage<?> deserialize(String json, Class<? extends BaseMessage<?>> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}
