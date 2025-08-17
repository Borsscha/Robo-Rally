package de.lmu.cleverecousins.protocol.messageBody;

public class MapSelectedBody {
    private String mapName;
    private String playerName;  // neu

    // Default-Konstruktor für Jackson
    public MapSelectedBody() { }

    /**
     * Ein-Argument-Konstruktor, damit bestehender Aufruf new MapSelectedBody(mapName) weiter funktioniert
     */
    public MapSelectedBody(String mapName) {
        this(mapName, "");
    }

    // Neuer Konstruktor mit beiden Parametern
    public MapSelectedBody(String mapName, String playerName) {
        this.mapName = mapName;
        this.playerName = playerName;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
