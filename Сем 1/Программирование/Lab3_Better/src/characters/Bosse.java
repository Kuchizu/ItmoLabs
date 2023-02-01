package characters;

import BuildingObjects.BuildingObject;

public class Bosse extends Human{
    public Bosse(String name, int id, BuildingObject location){
        super(name, id, location);
        System.out.println(name + " создался");
    }
}

