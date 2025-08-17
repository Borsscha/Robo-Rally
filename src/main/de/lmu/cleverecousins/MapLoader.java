package de.lmu.cleverecousins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.GameStartedBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for loading a map JSON from the classpath (resources folder) and
 * deserializing it into a {@link GameStartedBody}.
 * <p>
 * The JSON is expected to have the envelope structure of a {@code BaseMessage<GameStartedBody>}
 * as used by the network protocol.
 */
public class MapLoader {

    /**
     * Loads the given resource from the classpath and converts it into a {@link GameStartedBody}.
     *
     * @param resourcePath path within the resources folder (e.g. {@code "/map-dizzy-highway.json"})
     * @return the deserialized {@link GameStartedBody}
     * @throws IOException if the resource cannot be found or read, or if deserialization fails
     */
    public static GameStartedBody loadMap(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = MapLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            BaseMessage<GameStartedBody> message = mapper.readValue(is, mapper.getTypeFactory()
                    .constructParametricType(BaseMessage.class, GameStartedBody.class));
            return message.getMessageBody();
        }
    }
}


