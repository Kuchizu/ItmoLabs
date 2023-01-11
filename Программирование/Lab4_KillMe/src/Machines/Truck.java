package Machines;

import Enums.Color;
import ObjMaker.CoreMaker;
import actions.Shipable;

public class Truck extends Machine implements Shipable {
    public Truck(String name, int id, Color color, CoreMaker core) {
        super(name, id, color, core);
    }

    @Override
    public String ship(Machine obj) {
        return this.getName() + " перевозит " + obj.getName();
    }

    @Override
    public String lift(CoreMaker obj) {
        return null;
    }
}
