package de.lmu.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MapTileDefinition {

    private String type;

    @JsonProperty("isOnBoard")
    private String isOnBoard;

    private List<String> orientations;
    private Integer speed;
    private List<Integer> registers;
    private Integer count;
    private String rotation;

    // ✅ 新增字段：用于从 MapLoader 中注入 tile 坐标
    private int x;
    private int y;

    // --- Getters & Setters ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOnBoard() {
        return this.isOnBoard;
    }

    public void setOnBoard(String isOnBoard) {
        this.isOnBoard = isOnBoard;
    }

    public List<String> getOrientations() {
        return orientations != null ? orientations : List.of();
    }

    public void setOrientations(List<String> orientations) {
        this.orientations = orientations;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public List<Integer> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Integer> registers) {
        this.registers = registers;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    // ✅ 新增坐标字段支持
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}



