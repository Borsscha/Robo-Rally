package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.StartingPointTakenBody;

public class StartingPointTakenMessage extends BaseMessage <StartingPointTakenBody>{

    public StartingPointTakenMessage(){
        super();
    }

    public StartingPointTakenMessage(StartingPointTakenBody body){
        super(body);
    }

    public StartingPointTakenMessage (int x, int y, String direction, int clientID){
        super(new StartingPointTakenBody(x, y, direction, clientID));
    }
}

