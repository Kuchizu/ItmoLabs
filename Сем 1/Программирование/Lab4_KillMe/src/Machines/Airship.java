package Machines;

import Enums.*;
import ObjMaker.CoreMaker;
import ObjMaker.MachineMaker;
import actions.Liftable;

public class Airship extends Machine implements Liftable {
    public Airship(String name, int id, Color color, CoreMaker core) {
        super(name, id, color, core);
    }


    @Override
    public String lift(CoreMaker obj) {
        return this.getName() + " поднимает " + obj;
    }

    @Override
    public String ship(Machine obj) {
        return null;
    }
}
