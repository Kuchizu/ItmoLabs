package Machines;

import Enums.*;
import ObjMaker.*;
import actions.Liftable;

public abstract class Machine {
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
    public boolean flyable(int speed, int height){
        class flying {
            private int speed;
            private int height;

            public flying(int speed, int height) {   // конструктор локального класса
                this.speed = speed;
                this.height = height;
            }

            public boolean isflying() {
                return speed > 0 && height > 0;
            }
        }
        flying ability = new flying(speed, height);
        return ability.isflying();
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
