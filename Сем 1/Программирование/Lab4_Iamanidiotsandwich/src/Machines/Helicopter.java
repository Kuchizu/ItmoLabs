package Machines;

import Enums.*;
import Exceptions.CapacityException;
import ObjMaker.CoreMaker;
import actions.Liftable;

public class Helicopter extends Machine implements Liftable {
    private int capacity = 6;
    public Helicopter(String name, int id, Color color, CoreMaker core) {
        super(name, id, color, core);
    }

    public int getCapacity(){
        return this.capacity;
    }

    public void setCapacity(int cap) throws CapacityException {
        if (capacity < 1) {
            throw new CapacityException(cap, 1, 5);
        } else {
            this.capacity = cap;
        }
    }

    @Override
    public String lift(CoreMaker obj) {
        return this.getName() + " поднимает " + obj.getType();
    }

}
