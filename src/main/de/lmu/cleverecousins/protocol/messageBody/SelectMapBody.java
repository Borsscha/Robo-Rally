package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class SelectMapBody {
    private List<String> availableMaps;
    private int allowedClientId; // ID des Spielers, der die Map auswählen darf

    public SelectMapBody() {
        // Standard-Konstruktor für Jackson
    }

    public SelectMapBody(List<String> availableMaps, int allowedClientId) {
        this.availableMaps = availableMaps;
        this.allowedClientId = allowedClientId;
    }

    public List<String> getAvailableMaps() {
        return availableMaps;
    }

    public void setAvailableMaps(List<String> availableMaps) {
        this.availableMaps = availableMaps;
    }

    public int getAllowedClientId() {
        return allowedClientId;
    }

    public void setAllowedClientId(int allowedClientId) {
        this.allowedClientId = allowedClientId;
    }
}



