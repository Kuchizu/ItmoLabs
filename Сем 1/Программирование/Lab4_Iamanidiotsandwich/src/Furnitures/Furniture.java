package Furnitures;

import Enums.*;
import ObjMaker.*;

public abstract class Furniture {
    private final String name;
    private final int id;
    private CompositMaker core;
    private Color color;
    public Furniture(String name, int id, Color color, CompositMaker core){
        this.name = name;
        this.id = id;
        this.color = color;
        this.core = core;
        System.out.println("Создание новой мебели " + name);
    }
    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }

    public Color getColor() {
        return this.color;
    }
    public CompositMaker getCore() {
        return this.core;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public void setCore(CompositMaker core) {
        this.core = core;
    }

    @Override
    public String toString() {
        return "Machine: " + this.getName() + '\''
                + "Hash: " + this.hashCode();
    }

    @Override
    public int hashCode() {
        return 555 * this.name.hashCode() + 333;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
}
