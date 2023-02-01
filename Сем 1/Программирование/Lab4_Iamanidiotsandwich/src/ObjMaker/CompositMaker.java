package ObjMaker;

import Enums.Core;
public class CompositMaker extends Constructor{

    Core type;
    public CompositMaker(Core type) {
        super(type);
        this.type = type;
        System.out.println(type.getName());
    }

    public Core getConsistance(){
        return this.type;
    }
}
