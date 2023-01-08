package characters;

import BuildingObjects.BuildingObject;

public class Betan extends Human{
    public Betan(String name, int id, BuildingObject location){
        super(name, id, location);
        System.out.println(name + " создался");
    }
}
