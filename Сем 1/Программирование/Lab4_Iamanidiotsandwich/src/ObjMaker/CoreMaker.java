package ObjMaker;

import Enums.Core;
import Exceptions.TemperatureException;

public class CoreMaker extends Constructor{
    private int temp;
    public CoreMaker(Core type) {
        super(type);
        System.out.println(type.getName());
    }
    public void tempset(int temp) throws TemperatureException {
        if (temp  > 5000) {
            throw new TemperatureException(temp, 1, 4999);
        }
        else if (temp < 0){
            throw new TemperatureException(temp, 1, 4999);
        }
        else{
            this.temp = temp;
        }
    }

}
