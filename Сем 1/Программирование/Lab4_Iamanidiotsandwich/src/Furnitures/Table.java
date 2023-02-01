package Furnitures;

import Enums.*;
import ObjMaker.CompositMaker;

public class Table extends Furniture {
    public Table(String name, int id, Color color, CompositMaker core) {
        super(name, id, color, core);
    }


    public String lift(CompositMaker obj) {
        return "На " + this.getName() + " лежит " + obj;
    }
}
