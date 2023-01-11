import BuildingObjects.*;
import Enums.Color;
import Enums.Core;
import Exceptions.CapacityException;
import Exceptions.TemperatureException;
import ObjMaker.*;
import Machines.*;

public class Main {
    public static void main(String[] args) throws CapacityException, TemperatureException {

        BuildingObject area = new Park("Площадка", 1, 1000, 1000);

        CoreMaker metal = new CoreMaker(Core.METAL);
        Truck truck = new Truck("Грузовик", 2, Color.PURPLE, metal);

        CoreMaker steel = new CoreMaker(Core.STAINLESSTEEL);
        steel.tempwhat(100);
        Rocket rocket = new Rocket("Ракета", 1, Color.BLACK, steel);

        System.out.println(truck.ship(rocket));

        Helicopter helicopter = new Helicopter("Вертолёт", 3, Color.RED, metal);
        System.out.println(helicopter.lift(steel));

        CoreMaker constmoplastic = new CoreMaker(Core.COSTMOPLASTIC);
        System.out.println(helicopter.lift(constmoplastic));

        System.out.println(rocket.setCapacity(10));
        System.out.println(rocket.getCapacity());

        /* Exceptions */

//        rocket.setCapacity(-1);
//        steel.tempwhat(100000000);

    }
}