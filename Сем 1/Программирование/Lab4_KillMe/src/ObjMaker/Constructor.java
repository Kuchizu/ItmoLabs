package ObjMaker;

import Exceptions.TemperatureException;
import Machines.Machine;
import Enums.Core;

public abstract class Constructor {
    private String type;
    private int temp;
    public Constructor(Machine obj) {
        this.type = obj.getName();
        System.out.println("Создаётся машина " + obj);
    }
    public Constructor(Core obj) {
        this.type = obj.getName();
        System.out.println("Создаётся материал " + obj);
    }

    public void tempwhat(int temp) throws TemperatureException {
        if (temp  > 5000) {
            throw new TemperatureException("Всё сгорело");
        }
        else if (temp < 0){
            throw new TemperatureException("Вы лёд.");
        }
        else{
            this.temp = temp;
        }

    }

    public String getType(){
        return this.type;
    }

}

