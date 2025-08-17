package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerAddedBody {

    private int clientID;
    private String name;

    @JsonProperty("figure")
    private int figure;

    public PlayerAddedBody() {};

    public PlayerAddedBody(int clientID, String name, int figure) {
        this.clientID = clientID;
        this.name = name;
        this.figure = figure;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFigure() {
        return figure;
    }

    public void setFigure(int figure) {
        this.figure = figure;
    }
}
