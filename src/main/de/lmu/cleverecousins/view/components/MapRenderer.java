package de.lmu.cleverecousins.view.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.RobotPosition;
import de.lmu.util.LogConfigurator;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * MapRenderer is a JavaFX UI component that renders the game board on screen.
 * <p>
 * It extends {@link GridPane} and displays the tiles defined in the game map,
 * typically built from a JSON-based map structure.
 * This class is responsible for visualizing the board for the player.
 */
public class MapRenderer extends GridPane {

    private static final Logger logger = Logger.getLogger(MapRenderer.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    private int tileSize;
    private final Map<Canvas, javafx.scene.image.WritableImage> baseTiles = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();


    private final Image tileEmpty = loadImage("/images/TilesetBoard.png");
    private final Image tileDark = loadImage("/images/TilesetBoard_darker.png");
    private final Image conveyor1 = loadImage("/images/ConveyorBelt1.png");
    private final Image conveyor2 = loadImage("/images/ConveyorBelt2.png");
    private final Image wallImg = loadImage("/images/Wall.png");
    private final Image laserImg = loadImage("/images/Laiser.png");
    private final Image energySingleImg = loadImage("/images/EnergySingle.png");
    private final Image energyDoubleImg = loadImage("/images/EnergyDouble.png");
    private final Image checkpoint1Img = loadImage("/images/Checkpoint1.png");
    private final Image checkpoint2Img = loadImage("/images/Checkpoint2.png");
    private final Image checkpoint3Img = loadImage("/images/Checkpoint3.png");
    private final Image checkpoint4Img = loadImage("/images/Checkpoint4.png");
    private final Image checkpoint5Img = loadImage("/images/Checkpoint5.png");
    private final Image pusher135b = loadImage("/images/TilesetPusher135B.png");
    private final Image pusher135l = loadImage("/images/TilesetPusher135L.png");
    private final Image pusher135r = loadImage("/images/TilesetPusher135R.png");
    private final Image pusher135t = loadImage("/images/TilesetPusher135T.png");
    private final Image pusher24b = loadImage("/images/TilesetPusher24B.png");
    private final Image pusher24l = loadImage("/images/TilesetPusher24L.png");
    private final Image pusher24r = loadImage("/images/TilesetPusher24R.png");
    private final Image pusher24t = loadImage("/images/TilesetPusher24T.png");
    private final Image pitImg = loadImage("/images/Pit.png");
    private final Image startPointImg = loadImage("/images/StartPoint.png");
    private final Image aImg = loadImage("/images/ABild.png");
    private final Image bImg = loadImage("/images/5BBild.png");
    private final Image antennaImg = loadImage("/images/Antenne.png");
    private final Image greenFieldImg = loadImage("/images/Green.png");
    private final Image gearRightImg = loadImage("/images/GearRight.png");
    private final Image gearLeftImg = loadImage("/images/GearLeft.png");
    private final Image board4aImg = loadImage("/images/4ABild.png");
    private final Image board1aImg = loadImage("/images/1ABild.png");
    private final Image board2aImg = loadImage("/images/2ABild.png");
    private final Map<Integer, Image> assignedImages = new HashMap<>();

    private final Image cb1BottomLeft = loadImage("/images/CB1BottomLeft.png");
    private final Image cb1BottomRight = loadImage("/images/CB1BottomRight.png");
    private final Image cb1LeftBottom = loadImage("/images/CB1LeftBottom.png");
    private final Image cb1LeftTop = loadImage("/images/CB1LeftTop.png");
    private final Image cb1RightBottom = loadImage("/images/CB1RightBottom.png");
    private final Image cb1RightTop = loadImage("/images/CB1RightTop.png");
    private final Image cb1TopLeft = loadImage("/images/CB1TopLeft.png");
    private final Image cb1TopRight = loadImage("/images/CB1TopRight.png");
    private final Image cb2LeftBottom = loadImage("/images/CB2LeftBottom.png");
    private final Image cb2LeftTop = loadImage("/images/CB2LeftTop.png");
    private final Image cb2RightBottom = loadImage("/images/CB2RightBottom.png");
    private final Image cb2RightTop = loadImage("/images/CB2RightTop.png");
    private final Image cb2TopRight = loadImage("/images/CB2TopRight.png");
    private final Image cb3BottomRightTop = loadImage("/images/CB3BottomRightTop.png");
    private final Image cb3BottomTopLeft = loadImage("/images/CB3BottomLeftTop.png");
    private final Image cb3LeftRightBottom = loadImage("/images/CB3LeftRightBottom.png");
    private final Image cb3LeftTopRight = loadImage("/images/CB3LeftRightTop.png");
    private final Image cb3RightLeftBottom = loadImage("/images/CB3RightLeftBottom.png");
    private final Image cb3RightTopLeft = loadImage("/images/CB3RightTopLeft.png");
    private final Image cb3TopLeftBottom = loadImage("/images/CB3TopLeftBottom.png");
    private final Image cb3TopRightBottom = loadImage("/images/CB3TopRightBottom.png");

    private final List<Region> startPointClickRegions = new ArrayList<>();
    private final List<int[]> startPositions = new ArrayList<>();
    private final List<Region> startPointRegions = new ArrayList<>();
    private StartPointClickListener startPointClickListener;

    private String mapPath;
    private int mapWidth;
    private int mapHeight;

    private final List<Image> robotImages = List.of(
            loadImage("/images/robot1.png"),
            loadImage("/images/robot2.png"),
            loadImage("/images/robot3.png"),
            loadImage("/images/robot4.png"),
            loadImage("/images/robot5.png"),
            loadImage("/images/robot6.png")
    );
    // Liste mit allen Robotern, die aktuell auf dem Spielfeld sind
    private List<RobotPosition> currentRobots = new ArrayList<>();


    public interface StartPointClickListener {
        void onStartPointClicked(int x, int y);
    }

    public void setStartPointClickListener(StartPointClickListener listener) {
        this.startPointClickListener = listener;
    }

    private void setupClickableStartPoints(JsonNode map) {
        // Alte Regionen entfernen
        for (Region region : startPointClickRegions) {
            this.getChildren().remove(region);
        }
        startPointClickRegions.clear();

        int cols = map.size();
        int rows = map.get(0).size();

        for (int x = 0; x < cols; x++) {
            JsonNode col = map.get(x);
            for (int y = 0; y < rows; y++) {
                int finalX = x;
                int finalY = y;
                JsonNode tileArray = col.get(y);
                if (tileArray.isArray()) {
                    for (JsonNode tile : tileArray) {
                        if ("StartPoint".equals(tile.get("type").asText())) {
                            Region region = new Region();
                            region.setPrefSize(tileSize , tileSize );
                            region.setPickOnBounds(true);
                            region.setStyle("-fx-background-color: transparent;"); // Oder zum Testen: rgba(255,0,0,0.15)

                            GridPane.setColumnIndex(region, finalX);
                            GridPane.setRowIndex(region, finalY);

                            region.setOnMouseClicked(e -> {
                                logger.fine(() -> "Startpunkt gewählt bei " + finalX + ", " + finalY);
                                if (startPointClickListener != null) {
                                    startPointClickListener.onStartPointClicked(finalX, finalY);
                                }
                            });

                            this.add(region, finalX, finalY);
                            startPointClickRegions.add(region);
                        }
                    }
                }
            }
        }
    }

    public MapRenderer(String mapPath, int tileSize) {
        this.tileSize = tileSize;
        this.mapPath = "/" + mapPath;
        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);

        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        loadMapFromJson(this.mapPath);
    }


    private Image loadImage(String path) {
        logger.fine("[DEBUG] Lade Bild: " + path);

        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            logger.severe("[FEHLER] Bild nicht gefunden: " + path);

            throw new RuntimeException("Image not found: " + path);
        }
        return new Image(stream);
    }

    private void loadMapFromJson(String path) {
        getChildren().clear();
        baseTiles.clear();
        startPointClickRegions.clear();
        startPositions.clear();

        try (InputStream is = getClass().getResourceAsStream(path)) {
            JsonNode root = mapper.readTree(is);
            JsonNode messageBody = root.get("messageBody");
            JsonNode map = messageBody.get("gameMap");

            int cols = map.size();
            int rows = map.get(0).size();
            this.mapWidth = cols;
            this.mapHeight = rows;

            List<Canvas> canvasList = new ArrayList<>();

            // Karte zeichnen
            for (int x = 0; x < cols; x++) {
                JsonNode col = map.get(x);
                for (int y = 0; y < rows; y++) {
                    JsonNode tileArray = col.get(y);
                    Canvas canvas = new Canvas(tileSize, tileSize);
                    GraphicsContext gc = canvas.getGraphicsContext2D();

                    // Beispiel Sonderzeichnungen je nach Koordinaten / Map
                    if (x == 0 && y == 9) {
                        gc.drawImage(aImg, 0, 0, tileSize, tileSize);
                    } else if (path.toLowerCase().contains("dizzy-highway") && x == 12 && y == 9) {
                        gc.drawImage(bImg, 0, 0, tileSize, tileSize);
                    } else if (x == 0 && y == 4) {
                        gc.drawImage(antennaImg, 0, 0, tileSize, tileSize);
                    } else if (path.toLowerCase().contains("extra-crispy") && x == 3 && y == 9) {
                        gc.drawImage(board4aImg, 0, 0, tileSize, tileSize);
                    } else if (path.toLowerCase().contains("lost-bearings") && x == 3 && y == 9) {
                        gc.drawImage(board1aImg, 0, 0, tileSize, tileSize);
                    } else if (path.toLowerCase().contains("death-trap") && x == 12 && y == 0) {
                        gc.drawImage(board2aImg, 0, 0, tileSize, tileSize);
                    } else {
                        gc.drawImage(x < 3 ? tileDark : tileEmpty, 0, 0, tileSize, tileSize);
                    }

                    if (tileArray.isArray()) {
                        for (JsonNode tile : tileArray) {
                            String type = tile.get("type").asText();

                            if ("StartPoint".equals(type)) {
                                startPositions.add(new int[]{x, y});
                            }


                            List<String> rawOrientations = tile.has("orientations") && tile.get("orientations").isArray()
                                    ? mapper.convertValue(tile.get("orientations"), List.class)
                                    : List.of();

                            List<String> orientations = rotateOrientations(rawOrientations);

                            switch (type) {
                                case "StartPoint" -> gc.drawImage(startPointImg, 0, 0, tileSize, tileSize);
                                case "Checkpoint" -> {
                                    int count = tile.has("count") ? tile.get("count").asInt() : 1;
                                    Image checkpointImgToUse = switch (count) {
                                        case 2 -> checkpoint2Img;
                                        case 3 -> checkpoint3Img;
                                        case 4 -> checkpoint4Img;
                                        case 5 -> checkpoint5Img;
                                        default -> checkpoint1Img;
                                    };
                                    gc.drawImage(checkpointImgToUse, 0, 0, tileSize, tileSize);
                                }
                                case "ConveyorBelt" -> {
                                    int speed = tile.has("speed") ? tile.get("speed").asInt() : 1;
                                    String to = rawOrientations.size() > 0 ? rawOrientations.get(0) : null;
                                    String from = rawOrientations.size() > 1 ? rawOrientations.get(1) : null;

                                    Image img = null;

                                    if (speed == 1) {
                                        if ("bottom".equals(to) && "left".equals(from)) img = cb1BottomLeft;
                                        else if ("bottom".equals(to) && "right".equals(from)) img = cb1BottomRight;
                                        else if ("left".equals(to) && "top".equals(from)) img = cb1LeftTop;
                                        else if ("left".equals(to) && "bottom".equals(from)) img = cb1LeftBottom;
                                        else if ("right".equals(to) && "top".equals(from)) img = cb1RightTop;
                                        else if ("right".equals(to) && "bottom".equals(from)) img = cb1RightBottom;
                                        else if ("top".equals(to) && "left".equals(from)) img = cb1TopLeft;
                                        else if ("top".equals(to) && "right".equals(from)) img = cb1TopRight;
                                    } else if (speed == 2) {
                                        if ("left".equals(to) && "bottom".equals(from)) img = cb2LeftBottom;
                                        else if ("left".equals(to) && "top".equals(from)) img = cb2LeftTop;
                                        else if ("right".equals(to) && "bottom".equals(from)) img = cb2RightBottom;
                                        else if (rawOrientations.equals(List.of("right", "top"))) img = cb2RightTop;
                                        else if (rawOrientations.equals(List.of("top", "right"))) img = cb2TopRight;
                                        else if (rawOrientations.equals(List.of("bottom", "right", "top")))
                                            img = cb3BottomRightTop;
                                        else if (rawOrientations.equals(List.of("bottom", "left", "top")))
                                            img = cb3BottomTopLeft;
                                        else if (rawOrientations.equals(List.of("left", "right", "bottom")))
                                            img = cb3LeftRightBottom;
                                        else if (rawOrientations.equals(List.of("left", "right", "top")))
                                            img = cb3LeftTopRight;
                                        else if (rawOrientations.equals(List.of("right", "left", "bottom")))
                                            img = cb3RightLeftBottom;
                                        else if (rawOrientations.equals(List.of("right", "top", "left")))
                                            img = cb3RightTopLeft;
                                        else if (rawOrientations.equals(List.of("top", "left", "bottom")))
                                            img = cb3TopLeftBottom;
                                        else if (rawOrientations.equals(List.of("top", "right", "bottom")))
                                            img = cb3TopRightBottom;
                                    }

                                    // Fallback für einfache Richtung
                                    if (img == null) {
                                        img = speed == 2 ? conveyor2 : conveyor1;
                                        drawRotated(gc, img, orientations, tileSize);
                                    } else {
                                        gc.drawImage(img, 0, 0, tileSize, tileSize);
                                    }
                                }
                                case "Wall" -> drawWallRotated(gc, orientations);
                                case "Laser" -> drawLaserFlipped(gc, orientations);
                                case "Energy" -> {
                                    int count = tile.has("count") ? tile.get("count").asInt() : 1;
                                    if (count == 2) {
                                        gc.save();
                                        gc.translate(tileSize / 2.0, tileSize / 2.0);
                                        gc.drawImage(energyDoubleImg, -tileSize / 2.0, -tileSize / 2.0, tileSize, tileSize);
                                        gc.restore();
                                    } else {
                                        double halfWidth = tileSize / 2.0;
                                        gc.drawImage(energySingleImg, 0, 0, halfWidth, tileSize);
                                    }
                                }
                                case "Pit" -> gc.drawImage(pitImg, 0, 0, tileSize, tileSize);
                                case "PushPanel" -> {
                                    List<Integer> registers = new ArrayList<>();
                                    if (tile.has("registers") && tile.get("registers").isArray()) {
                                        for (JsonNode reg : tile.get("registers")) {
                                            registers.add(reg.asInt());
                                        }
                                    }

                                    Set<Integer> regSet = new HashSet<>(registers);

                                    String orientation = tile.has("orientation") ? tile.get("orientation").asText().toLowerCase() : "top";

                                    Image pusherImg = null;

                                    if (regSet.equals(Set.of(0, 2, 4))) {
                                        switch (orientation) {
                                            case "top" -> pusherImg = pusher135t;
                                            case "right" -> pusherImg = pusher135r;
                                            case "bottom" -> pusherImg = pusher135b;
                                            case "left" -> pusherImg = pusher135l;
                                        }
                                    } else if (regSet.equals(Set.of(1, 3))) {
                                        switch (orientation) {
                                            case "top" -> pusherImg = pusher24t;
                                            case "right" -> pusherImg = pusher24r;
                                            case "bottom" -> pusherImg = pusher24b;
                                            case "left" -> pusherImg = pusher24l;
                                        }
                                    }

                                    if (pusherImg != null) {
                                        gc.drawImage(pusherImg, 0, 0, tileSize, tileSize);
                                    }
                                }
                                case "RestartPoint" -> {
                                    if (path.toLowerCase().contains("dizzy-highway")) {
                                        gc.drawImage(greenFieldImg, 0, 0, tileSize, tileSize);
                                    } else {
                                        gc.save();
                                        gc.translate(tileSize / 2.0, tileSize / 2.0);
                                        gc.rotate(-90);
                                        gc.drawImage(greenFieldImg, -tileSize / 2.0, -tileSize / 2.0, tileSize, tileSize);
                                        gc.restore();
                                    }
                                }
                                case "Gear" -> {
                                    if (orientations.contains("clockwise")) {
                                        gc.drawImage(gearRightImg, 0, 0, tileSize, tileSize);
                                    } else if (orientations.contains("counterclockwise")) {
                                        gc.drawImage(gearLeftImg, 0, 0, tileSize, tileSize);
                                    }
                                }
                            }
                        }
                    }

                    add(canvas, x, y);
                    canvas.setMouseTransparent(true);  // ← Roboterklicks ermöglichen
                    canvasList.add(canvas);            // für spätere Snapshot-Erstellung

                }
            }

            // Snapshots erzeugen (nachdem alles sichtbar ist)
            Platform.runLater(() -> {
                for (Canvas canvas : canvasList) {
                    baseTiles.put(canvas, canvas.snapshot(null, null));
                }
            });

            // Klickbereiche für Startpunkte
            for (int[] pos : startPositions) {
                int x = pos[0];
                int y = pos[1];

                Region clickableRegion = new Region();
                clickableRegion.setPrefSize(tileSize, tileSize);
                clickableRegion.setStyle("-fx-background-color: transparent;");
                setColumnIndex(clickableRegion, x);
                setRowIndex(clickableRegion, y);

                clickableRegion.setOnMouseClicked(e -> {
                    logger.fine(() -> "Startpunkt gewählt bei " + x + ", " + y);
                    if (startPointClickListener != null) {
                        startPointClickListener.onStartPointClicked(x, y);
                    }
                });

                startPointClickRegions.add(clickableRegion);
            }

            getChildren().addAll(startPointClickRegions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> rotateOrientations(List<String> orientations) {
        List<String> rotated = new ArrayList<>();
        for (String ori : orientations) {
            switch (ori) {
                case "top" -> rotated.add("left");
                case "left" -> rotated.add("bottom");
                case "bottom" -> rotated.add("right");
                case "right" -> rotated.add("top");
                default -> rotated.add(ori);
            }
        }
        return rotated;
    }


    public void updateRobots(List<RobotPosition> robots) {
        this.currentRobots = robots;
        redrawRobots();
    }

    private void redrawRobots() {
        Platform.runLater(() -> {
            // 1. Alle Tiles zurücksetzen
            for (var node : getChildren()) {
                if (node instanceof Canvas canvas) {
                    var base = baseTiles.get(canvas);
                    if (base != null) {
                        var gc = canvas.getGraphicsContext2D();
                        gc.setTransform(1, 0, 0, 1, 0, 0);
                        gc.clearRect(0, 0, tileSize, tileSize);
                        gc.drawImage(base, 0, 0, tileSize, tileSize);
                    }
                }
            }

            // 2. Roboter neu zeichnen
            for (RobotPosition robot : currentRobots) {
                int x = robot.x();
                int y = robot.y();

                var node = getChildren().stream()
                        .filter(c -> c instanceof Canvas
                                && Objects.equals(GridPane.getColumnIndex(c), x)
                                && Objects.equals(GridPane.getRowIndex(c), y))
                        .findFirst();

                if (node.isPresent()) {
                    Canvas canvas = (Canvas) node.get();
                    var gc = canvas.getGraphicsContext2D();

                    Image robotImg = assignedImages.get(robot.clientID());
                    if (robotImg == null) {
                        int index = robot.clientID() % robotImages.size();
                        robotImg = robotImages.get(index);
                        assignedImages.put(robot.clientID(), robotImg);
                    }

                    double scale = 0.9;
                    double size = tileSize * scale;

                    gc.save();
                    gc.translate(tileSize / 2.0, tileSize / 2.0);

                    switch (robot.direction().toLowerCase()) {
                        case "right" -> gc.rotate(90);
                        case "bottom" -> gc.rotate(180);
                        case "left" -> gc.rotate(270);
                        default -> gc.rotate(0);
                    }

                    gc.drawImage(robotImg, -size / 2.0, -size / 2.0, size, size);
                    gc.restore();
                }
            }
        });
    }



    private void drawRotated(GraphicsContext gc, Image img, List<String> orientations, int size) {
        double angle = orientations.contains("top") ? 0 :
                orientations.contains("right") ? 90 :
                        orientations.contains("bottom") ? 180 :
                                orientations.contains("left") ? 270 : 0;

        gc.save();
        gc.translate(tileSize  / 2.0, tileSize  / 2.0);
        gc.rotate(angle);
        gc.drawImage(img, -size / 2.0, -size / 2.0, size, size);
        gc.restore();
    }

    private void drawWallRotated(GraphicsContext gc, List<String> orientations) {
        gc.setFill(Color.BLACK);
        double thickness = 5;
        for (String dir : orientations) {
            switch (dir) {
                case "top" -> gc.fillRect(tileSize  - thickness, 0, thickness, tileSize );
                case "right" -> gc.fillRect(0, tileSize  - thickness, tileSize , thickness);
                case "bottom" -> gc.fillRect(0, 0, thickness, tileSize );
                case "left" -> gc.fillRect(0, 0, tileSize , thickness);
            }
        }
    }

    private void drawLaserFlipped(GraphicsContext gc, List<String> orientations) {
        double width = 4;
        gc.setStroke(Color.RED);
        gc.setLineWidth(width);
        for (String dir : orientations) {
            switch (dir) {
                case "top", "bottom" -> gc.strokeLine(0, tileSize  / 2.0, tileSize , tileSize  / 2.0);
                case "left", "right" -> gc.strokeLine(tileSize  / 2.0, 0, tileSize  / 2.0, tileSize );
            }
        }
    }

    public void assignImageIfNeeded(int clientID) {
        if (!assignedImages.containsKey(clientID)) {
            for (Image img : robotImages) {
                if (!assignedImages.containsValue(img)) {
                    assignedImages.put(clientID, img);
                    break;
                }
            }
        }
    }

    public void updateRobotPosition(int clientID, int x, int y, String direction) {
        Platform.runLater(() -> {
            // Roboterliste aktualisieren
            currentRobots.removeIf(r -> r.clientID() == clientID);
            currentRobots.add(new RobotPosition(clientID, x, y, direction));;

            // Bildzuweisung prüfen
            if (!assignedImages.containsKey(clientID)) {
                // ein Bild suchen, das noch nicht vergeben ist
                for (Image img : robotImages) {
                    if (!assignedImages.containsValue(img)) {
                        assignedImages.put(clientID, img);
                        break;
                    }
                }
            }

            // alle Tiles zurücksetzen
            for (var node : getChildren()) {
                if (node instanceof Canvas canvas) {
                    var base = baseTiles.get(canvas);
                    if (base != null) {
                        var gc = canvas.getGraphicsContext2D();
                        gc.setTransform(1, 0, 0, 1, 0, 0);
                        gc.clearRect(0, 0, tileSize , tileSize );
                        gc.drawImage(base, 0, 0, tileSize , tileSize );
                    }
                }
            }

            // alle Roboter zeichnen
            for (RobotPosition robot : currentRobots) {
                int mirroredX = robot.x();
                int ry = robot.y();

                var node = getChildren().stream()
                        .filter(c -> Objects.equals(GridPane.getColumnIndex(c), mirroredX)
                                && Objects.equals(GridPane.getRowIndex(c), ry))
                        .findFirst();

                if (node.isPresent() && node.get() instanceof Canvas canvas) {
                    var gc = canvas.getGraphicsContext2D();
                    Image robotImg = assignedImages.get(robot.clientID());
                    double scale = 0.9;
                    double size = tileSize  * scale;

                    gc.save();
                    gc.translate(tileSize  / 2.0, tileSize  / 2.0);

                    switch (robot.direction().toLowerCase()) {
                        case "right" -> gc.rotate(90);
                        case "bottom" -> gc.rotate(180);
                        case "left" -> gc.rotate(270);
                        default -> gc.rotate(0); // top
                    }
                    gc.drawImage(robotImg, -size / 2.0, -size / 2.0, size, size);
                    gc.restore();
                }
            }
        });
    }

    public void rotateRobot(int clientID, String rotation) {
        for (int i = 0; i < currentRobots.size(); i++) {
            RobotPosition r = currentRobots.get(i);
            if (r.clientID() == clientID) {
                String newDir = rotateDirection(r.direction(), rotation);
                currentRobots.set(i, new RobotPosition(r.clientID(), r.x(), r.y(), newDir));
                redrawRobots();
                break;
            }
        }
    }

    private String rotateDirection(String current, String rotation) {
        List<String> dirs = List.of("top", "right", "bottom", "left");
        int index = dirs.indexOf(current.toLowerCase());
        return switch (rotation.toLowerCase()) {
            case "clockwise" -> dirs.get((index + 1) % 4);
            case "counterclockwise" -> dirs.get((index + 3) % 4);
            case "uturn" -> dirs.get((index + 2) % 4);
            default -> current;
        };
    }

    public Image getImageForClient(int clientID) {
        return assignedImages.get(clientID);
    }

    public Image peekAssignedImage(int clientID) {
        Image img = assignedImages.get(clientID);
        logger.fine("[DEBUG] peekAssignedImage für clientID " + clientID + ": " + img);
        return img;
    }

    public void rescaleTiles() {
        for (var node : getChildren()) {
            if (node instanceof Canvas canvas) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                var base = baseTiles.get(canvas);
                if (base != null) {
                    canvas.setWidth(tileSize);
                    canvas.setHeight(tileSize);
                    gc.setTransform(1, 0, 0, 1, 0, 0);
                    gc.clearRect(0, 0, tileSize, tileSize);
                    gc.drawImage(base, 0, 0, tileSize, tileSize);
                }
            }
        }

        // Klickbereiche der Startpunkte ebenfalls anpassen
        for (Region region : startPointClickRegions) {
            region.setPrefSize(tileSize, tileSize);
        }

        redrawRobots(); // Damit Roboter auch neu skaliert werden
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        if (this.tileSize != tileSize) {
            this.tileSize = tileSize;
            rescaleTiles(); // nur skalieren, kein komplettes redraw
        }
    }


    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void redraw() {
        loadMapFromJson(this.mapPath);
    }

}
