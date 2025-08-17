package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;
public class PlayerValuesBody {

    private String name;

    @JsonProperty("figure")
    private int figure;

    public PlayerValuesBody() {};
    public PlayerValuesBody(String name, int figure) {
        this.name = name;
        this.figure = figure;
    }

    public int getFigure() {
        return figure;
    }
    public void setFigure(int figure) {
        this.figure = figure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

