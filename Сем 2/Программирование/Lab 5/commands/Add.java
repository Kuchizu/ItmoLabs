package commands;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import exceptions.CreateObjException;
import managers.XMLManager;

import java.util.Objects;
import java.util.Scanner;

public class Add extends Command {
    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public String getDesc() {
        return "Add element";
    }

    @Override
    public void execute() throws CreateObjException {
        Scanner obj = new Scanner(System.in);

        System.out.println("Введите name (String):");
        String name = obj.nextLine();

        if(name.isEmpty()){
            throw new CreateObjException("Строка не может быть пустой");
        }

        System.out.println("Введите координаты x, y (Long, Double):");
        String cord = obj.nextLine();

        if(cord.isEmpty()){
            throw new CreateObjException("Поле не может быть null");
        }

        System.out.println("Введите area (Integer): ");
        String areas = obj.nextLine();

        if(areas.isEmpty()){
            throw new CreateObjException("Поле не может быть null");
        }

        int area = Integer.parseInt(areas);

        if(area < 1){
            throw new CreateObjException("Значение поля должно быть больше  0");
        }

        System.out.println("Введите numberOfRooms (Integer):");
        String numberOfRooms = obj.nextLine();

        if(!numberOfRooms.isEmpty()){
            if(Integer.parseInt(numberOfRooms) < 1){
                throw new CreateObjException("Значение поля должно быть больше  0");
            }
            else if(Integer.parseInt(numberOfRooms) > 19){
                throw new CreateObjException("Максимальное значение поля: 19");
            }
        }

        System.out.println("Введите timeToMetroOnFoot (Float):");
        String timeToMetroOnFoot = obj.nextLine();
        if(!timeToMetroOnFoot.isEmpty()){
            if(Integer.parseInt(timeToMetroOnFoot) < 1){
                throw new CreateObjException("Значение поля должно быть больше  0");
            }
        }

        System.out.println("Введите timeToMetroByTransport (Double):");
        String timeToMetroByTransport = obj.nextLine();
        if(!timeToMetroByTransport.isEmpty()){
            if(Integer.parseInt(timeToMetroByTransport) < 1){
                throw new CreateObjException("Значение поля должно быть больше  0");
            }
        }

        System.out.println("Введите furnish (Enum [NONE, DESIGNER, FINE, LITTLE]):");
        String furnish = obj.nextLine();

        if(furnish.isEmpty()){
            throw new CreateObjException("Поле не может быть null");
        }

        if(!Objects.equals(furnish, "NONE") || !Objects.equals(furnish, "DESIGNER") || !Objects.equals(furnish, "FINE") || !Objects.equals(furnish, "LITTLE")){
            furnish = "NONE";
        }

        System.out.println("Введите name of House (String):");
        String hname = obj.nextLine();

        if(hname.isEmpty()){
            hname = null;
        }

        System.out.println("Введите year of House (Long):");
        long hyear = obj.nextLong();

        if(hyear < 1){
            throw new CreateObjException("Значение поля должно быть больше  0");
        }
        else if(hyear > 853){
            throw new CreateObjException("Максимальное значение поля: 19");
        }

        System.out.println("Введите numberOfFloors of House (Long):");
        int hnumberOfFloors = obj.nextInt();

        if(hnumberOfFloors < 1){
            throw new CreateObjException("Значение поля должно быть больше  0");
        }

        System.out.println("Введите numberOfLifts of House (Long):");
        int hnumberOfLifts = obj.nextInt();

        if(hnumberOfLifts < 1){
            throw new CreateObjException("Значение поля должно быть больше  0");
        }

        String[] coordinates = cord.split("\\s+");

        Flat flat = new Flat(
                    name,
                    new Coordinates(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])),
                    area,
                    Integer.parseInt(numberOfRooms),
                    Float.valueOf(timeToMetroOnFoot),
                    Double.parseDouble(timeToMetroByTransport),
                    Furnish.valueOf(furnish),
                    new House(hname, hyear, hnumberOfFloors, hnumberOfLifts)
                );

        XMLManager.addElement(flat);

        System.out.println("Объект добавлен");

        System.out.println(XMLManager.getData().getLast());

    }
}
