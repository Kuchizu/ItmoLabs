package characters;

import BuildingObjects.BuildingObject;

public class Malish extends Human{
    public Malish(String name, int id, BuildingObject location) {
        super(name, id, location);
        System.out.println(name + " создался.");
    }
}
