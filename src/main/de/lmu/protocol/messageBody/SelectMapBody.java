package de.lmu.protocol.messageBody;

import java.util.List;

public class SelectMapBody {
    private List<String> availableMaps;

    public SelectMapBody() {}

    public SelectMapBody(List<String> availableMaps) {
        this.availableMaps = availableMaps;
    }

    public List<String> getAvailableMaps() {
        return availableMaps;
    }

    public void setAvailableMaps(List<String> availableMaps) {
        this.availableMaps = availableMaps;
    }
}
