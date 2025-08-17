package de.lmu.cleverecousins.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.messageBody.GameStartedBody;

import java.io.IOException;
import java.io.InputStream;

public class MapLoader {

    /**
     * Loads a map JSON file from the resources folder and deserializes it into a GameStartedBody object.
     *
     * @param resourcePath the resource path, e.g., "/map-dizzy-highway.json"
     * @return the deserialized GameStartedBody object
     * @throws IOException if the file cannot be read
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

