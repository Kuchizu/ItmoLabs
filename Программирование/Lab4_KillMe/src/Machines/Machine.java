package Machines;

import Enums.*;
import ObjMaker.*;
import actions.Liftable;
import actions.Shipable;

public abstract class Machine implements Liftable, Shipable {
    private final String name;
    private final int id;
    private CoreMaker core;
    private Color color;
    public Machine(String name, int id, Color color, CoreMaker core){
        this.name = name;
        this.id = id;
        this.color = color;
        this.core = core;
        System.out.println("Создание новой машины " + name);
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
    public CoreMaker getCore() {
        return this.core;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public void setCore(CoreMaker core) {
        this.core = core;
    }

    @Override
    public int hashCode() {
        return 555 * this.name.hashCode() + 333;
    }

    @Override
    public String toString() {
        return "Machine: " + this.getName() + '\''
                + "Hash: " + this.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}
