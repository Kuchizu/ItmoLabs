package BuildingObjects;

import actions.Capacityable;
import actions.Locationable;

public class Home extends BuildingObject implements Capacityable, Locationable {
    public Home(String name, int id, double square, double height){
        super(name, square, height, BuildingObjectEnum.Homeable, id);
    }

    @Override
    public String capacity(int num) {
        return this.getName() + " вмещает " + num + " человек";
    }

    @Override
    public String xycord(int x, int y) {
        return this.getName() + " находится на локации (x = " + x + ", y = " + y + ")";
    }
}
