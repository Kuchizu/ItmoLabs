package Furnitures;

import Enums.*;
import Exceptions.CompositException;
import ObjMaker.CompositMaker;
import actions.Puttable;

import java.util.Objects;

public class Chair extends Furniture implements Puttable {
    public Chair(String name, int id, Color color, CompositMaker core) throws CompositException {
        super(name, id, color, core);

        if(core.getConsistance() != Core.WOOD){
            throw new CompositException("Стул", core.getConsistance(), Core.WOOD);
        }
    }

    @Override
    public String put(String obj) {
        return obj + " сидит на стуле";
    }
}
