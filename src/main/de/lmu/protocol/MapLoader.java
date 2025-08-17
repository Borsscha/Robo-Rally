package de.lmu.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.protocol.message.BaseMessage;
import de.lmu.protocol.messageBody.GameStartedBody;

import java.io.IOException;
import java.io.InputStream;

public class MapLoader {

    /**
     * 从 resources 文件夹加载指定路径的地图 JSON，并反序列化为 GameStartedBody 对象
     *
     * @param resourcePath 资源路径，如 "/map-dizzy-highway.json"
     * @return GameStartedBody 对象
     * @throws IOException 若读取失败
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

