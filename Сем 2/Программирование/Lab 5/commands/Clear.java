package commands;

import managers.XMLManager;

/**
 * Clears collection.
 */
public class Clear extends Command {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDesc() {
        return "Очистить коллекцию";
    }

    /**
     * Clears collection
     * @param arg
     */
    @Override
    public void execute(String arg) {
        XMLManager.dropAll();
        System.out.println("Cleared all collection.");
    }
}
