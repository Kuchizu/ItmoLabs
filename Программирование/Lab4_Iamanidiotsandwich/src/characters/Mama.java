package characters;

import BuildingObjects.BuildingObject;
import actions.Liftable;

public class Mama extends Human implements Liftable {
    public Mama(String name, int id, BuildingObject location) {
        super(name, id, location);
        System.out.println(name + " создался.");
    }

    @Override
    public String lift(Malish obj) {
        return "Малыш сидит у " + this.getName() + " на коленях";
    }
}

