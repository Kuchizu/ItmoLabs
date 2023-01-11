package BuildingObjects;

import actions.*;

public abstract class BuildingObject implements Capacityable, Locationable {
    private String name;
    private BuildingObjectEnum type;
    private String time;
    private int id;
    private double square;
    private double height;

    public BuildingObject(String name, BuildingObjectEnum type){
        this.name = name;
        this.type = type;
    }

    public BuildingObject(String name, double Square, double Height, BuildingObjectEnum type, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.square = Square;
        this.height = Height;
    }

    public String getName(){
        return this.name;
    }

    public double getSquare(){
        return this.square;
    }

    public double getHeight(){
        return this.height;
    }

    public void getId(){
        System.out.println(this.id);
    }

    public void getType(){
        System.out.println(this.type);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.name.hashCode();
    }

    @Override
    public String toString() {
        return "Building: " + this.getName() + " : " + this.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
}
