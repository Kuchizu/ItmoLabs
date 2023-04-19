package commands;


import exceptions.CreateObjException;

public class UpdateElement extends Command {
    @Override
    public String getName() {
        return "Update";
    }

    @Override
    public String getDesc() {
        return "Update element.";
    }

    @Override
    public void execute(String arg) throws CreateObjException {
        if(arg == null){
            System.err.println("Usage: update {arg}");
            return;
        }
        Add add = new Add();
        add.execute(arg);
    }
}
