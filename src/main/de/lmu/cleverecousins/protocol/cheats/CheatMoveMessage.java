package de.lmu.cleverecousins.protocol.cheats;

import de.lmu.cleverecousins.protocol.BaseMessage;

public class CheatMoveMessage extends BaseMessage {
    private CheatMoveBody body;

    public CheatMoveMessage() {
        super("CheatMove");
    }

    public CheatMoveMessage(CheatMoveBody body) {
        super("CheatMove");
        this.body = body;
    }

    public CheatMoveBody getBody() {
        return body;
    }

    public void setBody(CheatMoveBody body) {
        this.body = body;
    }
}
