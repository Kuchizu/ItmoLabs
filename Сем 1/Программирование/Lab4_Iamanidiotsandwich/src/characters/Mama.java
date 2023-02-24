package characters;

import BuildingObjects.BuildingObject;
import actions.Liftable;

public class Mama extends Human {
    public Mama(String name, int id, BuildingObject location) {
        super(name, id, location);
        System.out.println(name + " создался.");
    }

}

