package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class PickDamageBody {

    private int count;
    private List<String> availablePiles;

    public PickDamageBody(){}

    public PickDamageBody(int count, List<String> availablePiles){
        this.count = count;
        this.availablePiles = availablePiles;
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }

    public List<String> getAvailablePiles(){
        return availablePiles;
    }

    public void setAvailablePiles(List<String> availablePiles){
        this.availablePiles = availablePiles;
    }
}
