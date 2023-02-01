package BuildingObjects;

import actions.*;

public class Park extends BuildingObject implements Capacityable, Locationable{
    public Park(String name, int id, double square, double height){
        super(name, square, height, BuildingObjectEnum.Amusement, id);
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
