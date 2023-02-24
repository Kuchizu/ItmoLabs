package Furnitures;

import Enums.Color;
import ObjMaker.CompositMaker;

public class Blanket extends Furniture {
    public Blanket(String name, int id, Color color, CompositMaker core) {
        super(name, id, color, core);
    }

    public static class withholes {
        Blanket blanket;
        boolean hole = true;
        public withholes(Blanket blanket) {
            this.blanket = blanket;
        }
    }
}
