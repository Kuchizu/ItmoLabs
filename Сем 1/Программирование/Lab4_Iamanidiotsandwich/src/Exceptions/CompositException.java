package Exceptions;

import Enums.Core;

public class CompositException extends Exception{

    String name;
    private Core compthis;
    private Core compdone;
    public CompositException(String name, Core compthis, Core compdone) {
        super();
        this.name = name;
        this.compthis = compthis;
        this.compdone = compdone;
    }

    @Override
    public String getMessage(){
        return name + " can not be done from " + compthis + " it can be done from " + compdone;
    }
}
