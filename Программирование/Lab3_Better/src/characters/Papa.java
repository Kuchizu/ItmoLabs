package characters;

import BuildingObjects.BuildingObject;

public class Papa extends Human{
    public Papa(String name, int id, BuildingObject location) {
        super(name, id, location);
        System.out.println(name + " создался.");
    }
}
