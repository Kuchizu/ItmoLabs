package ObjMaker;

import Exceptions.CapacityException;
import Furnitures.Furniture;
import Enums.Core;

public abstract class Constructor {
    private String type;
    private int size;
    public Constructor(Furniture obj) {
        this.type = obj.getName();
        System.out.println("Создаётся изделие " + obj);
    }
    public Constructor(Core obj) {
        this.type = obj.getName();
        System.out.println("Создаётся материал " + obj);
    }

    public void size(int size) throws CapacityException {
        if (size  > 1000) {
            throw new CapacityException("Очень большой объект");
        }
        else if (size < 0){
            throw new CapacityException("Минусное значение объекта");
        }
        else{
            this.size = size;
        }

    }

    public String getType(){
        return this.type;
    }

}

