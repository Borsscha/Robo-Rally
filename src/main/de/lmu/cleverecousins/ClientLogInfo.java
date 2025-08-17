package de.lmu.cleverecousins;
/**
 * the log-in information is the information,
 * that the player give in to the server, when they log in
 * since that information is immutable,
 * we choose record instead of class for simplicity*/
public record ClientLogInfo(
        int clientID,
        String playerName,
        String groupName,
        boolean usingAI
) {}


