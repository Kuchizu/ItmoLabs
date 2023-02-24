package Furnitures;

import Enums.*;
import Exceptions.CapacityException;
import ObjMaker.CompositMaker;

public class NewsPaper extends Furniture {
    private int pages = 10;
    public NewsPaper(String name, int id, Color color, CompositMaker core) {
        super(name, id, color, core);
    }

    public int getPages(){
        return this.pages;
    }

    public String setPages(int cap) throws CapacityException {
        if (cap < 1) {
            throw new CapacityException(cap, 1, 100);
        } else {
            this.pages = cap;
            return "Количество страниц газеты установлено на " + cap;
        }
    }

}
