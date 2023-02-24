package ObjMaker;

import Exceptions.CapacityException;
import Exceptions.TemperatureException;
import Furnitures.Furniture;
import Machines.Machine;
import Enums.Core;

public abstract class Constructor {
    private String type;
    private int temp;
    private int size;

    public Constructor(Machine obj) {
        this.type = obj.getName();
        System.out.println("Создаётся машина " + obj);
    }
    public Constructor(Core obj) {
        this.type = obj.getName();
        System.out.println("Создаётся материал " + obj);
    }

    public Constructor(Furniture obj) {
        this.type = obj.getName();
        System.out.println("Создаётся изделие " + obj);
    }

    public void size(int size) throws CapacityException {
        if (size  > 1000) {
            throw new CapacityException(size, 1, 999);
        }
        else if (size < 0){
            throw new CapacityException(size, 1, 999);
        }
        else{
            this.size = size;
        }

    }

    public String getType(){
        return this.type;
    }

}

