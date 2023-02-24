package Machines;

import Enums.*;
import Exceptions.CapacityException;
import ObjMaker.CoreMaker;
import actions.Liftable;

public class Rocket extends Machine implements Liftable {
    private int capacity = 10;
    public Rocket(String name, int id, Color color, CoreMaker core) {
        super(name, id, color, core);
    }

    public int getCapacity(){
        return this.capacity;
    }

    public String setCapacity(int cap) throws CapacityException {
        if (capacity < 1) {
            throw new CapacityException(capacity, 1, 10);
        } else {
            this.capacity = cap;
            return "Вместимость установлено на " + cap;
        }
    }

    @Override
    public String lift(CoreMaker lift) {
        return this.getName() + " поднимает " + lift.getType();
    }

}
