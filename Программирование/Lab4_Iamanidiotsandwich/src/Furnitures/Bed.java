package Furnitures;

import Enums.Color;
import ObjMaker.CompositMaker;
import actions.Sleepable;

public class Bed extends Furniture implements Sleepable {
    public Bed(String name, int id, Color color, CompositMaker core) {
        super(name, id, color, core);
    }

    @Override
    public String sleep(String obj) {
        return obj + " спит на кровати";
    }
}
