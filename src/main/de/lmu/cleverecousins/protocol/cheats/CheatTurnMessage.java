package de.lmu.cleverecousins.protocol.cheats;

import de.lmu.cleverecousins.protocol.BaseMessage;

public class CheatTurnMessage extends BaseMessage {
    private CheatTurnBody body;

    public CheatTurnMessage() {
        super("CheatTurn");
    }

    public CheatTurnMessage(CheatTurnBody body) {
        super("CheatTurn");
        this.body = body;
    }

    public CheatTurnBody getBody() {
        return body;
    }

    public void setBody(CheatTurnBody body) {
        this.body = body;
    }
}
