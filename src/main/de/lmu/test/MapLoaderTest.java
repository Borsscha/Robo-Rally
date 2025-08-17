package de.lmu.test;

import de.lmu.protocol.MapLoader;
import de.lmu.protocol.messageBody.GameStartedBody;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MapLoaderTest {

    private static final Logger logger = Logger.getLogger(MapLoaderTest.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public static void main(String[] args) {
        try {
            // 加载地图
             GameStartedBody mapBody = MapLoader.loadMap("/map-dizzy-highway.json");

            // 测试输出地图尺寸或能量值
            logger.info("Map energy: " + mapBody.getEnergy());

            logger.info("Map size: " + mapBody.getGameMap().size() + " rows");

            logger.info("First row size: " + mapBody.getGameMap().get(0).size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
