package commands;

import exceptions.CreateObjException;

public abstract class Command {

    public abstract String getName();
    public abstract String getDesc();
    public abstract void execute() throws CreateObjException;
}
