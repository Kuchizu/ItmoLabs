package commands;


import exceptions.CreateObjException;

/**
 * Updates element in collection which id = arg.
 */
public class UpdateElement extends Command {
    @Override
    public String getName() {
        return "update id {element}";
    }

    @Override
    public String getDesc() {
        return "Обновить значение элемента коллекции, id которого равен заданному";
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
