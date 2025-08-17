package de.lmu.cleverecousins;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/// Gilt für jede Map mit definierter Antennenposition

public class Antenne {

    private Position position;

    public Antenne(Position position){
        this.position = position;
    }

    public Position getPosition(){
        return position;
    }

    public void setPosition (Position position){
        this.position = position;
    }

    /**
     * Gibt eine Liste der Spieler, sortiert nach Priotrität (nächstgelegener zuerst)
     * Bei Gleichstand wird eine tiebreak-Regel angewendet (Uhrzeigersinn)
     * @param players
     * @return
     */
    public List<Player> getPriorityOrder(List<Player> players){
        return players.stream()
                .sorted(Comparator.comparingInt((Player p) -> manhattanDistance(p.getRobot().getPosition()))
                        .thenComparing((Player p) -> angleTo(p.getRobot().getPosition())))
                .collect(Collectors.toList());
    }

    /**
     * Manhattan-Distanz zur Antenne
     * @param other
     * @return
     */
    private int manhattanDistance(Position other){
        return Math.abs(position.getX() - other.getX()) + Math.abs(position.getY() - other.getY());
    }

    /**
     * Tiebreaker: Winkel im Uhrzeigersinn vom Antennenkopf zu einer Roboterposition
     * Kleinere Winkel haben Vorrang
     * @param other
     * @return
     */
    private double angleTo(Position other){
        int dx = other.getX() - position.getX();
        int dy = position.getY() - other.getY();
        return Math.atan2(dx, dy);
    }

//    @Override
//    public String toString(){
//        return "Antenne at " + position;
//    }
}

