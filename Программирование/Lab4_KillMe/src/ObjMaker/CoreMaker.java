package ObjMaker;

import Enums.Core;
public class CoreMaker extends Constructor{
    public CoreMaker(Core type) {
        super(type);
        System.out.println(type.getName());
    }
}
