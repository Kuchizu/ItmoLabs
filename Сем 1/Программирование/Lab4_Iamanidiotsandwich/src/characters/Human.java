package characters;

import BuildingObjects.*;
import Furnitures.Furniture;
import ObjMaker.CoreMaker;
import actions.*;

public abstract class Human{
    private final String name;
    private final BuildingObject location;


    public Human(String name, int id, BuildingObject location) {
        this.name = name;
        this.location = location;
    }

    public static class starets{
        private final String name;
        private final BuildingObject location;
        public starets(String name, int id, BuildingObject location){
            this.name = name;
            this.location = location;
        }
        public void Mine(CoreMaker core){
            System.out.println("Старец добывает " + core.getType());
        }
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location.getName();
    }

    public void go(Locationable target, int x, int y) {
        System.out.println(this.name + " пошёл на координаты " + target.xycord(x, y));
    }

    public void read(String text) {
        System.out.println(this.name + " прочитал " + text);
    }

    public void hug(Human who) {
        System.out.println(this.name + " обнял " + who.getName());
    }

    public void sleep() {
        System.out.println(this.name + " спит ");
    }

    public void sit(Furniture obj){
        System.out.println(this.getName() + " Сидит на " + obj.getName());
    }

    public void say(String word) {
        System.out.println(this.name + " сказал " + word);
    }

    public void go(String towhere) {
        System.out.println(this.name + " пошёл на " + towhere);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.name.hashCode();
    }

    @Override
    public String toString() {
        return "Human: " + this.getName() + " : " + this.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}
