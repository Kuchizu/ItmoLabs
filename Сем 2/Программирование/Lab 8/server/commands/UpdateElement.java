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
    public String execute(String arg, String login) throws CreateObjException {
        return null;
    }

}
