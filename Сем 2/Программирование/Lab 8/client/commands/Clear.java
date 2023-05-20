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
    public String execute(String arg) {
        XMLManager.dropAll();
        return "Cleared all collection.";
    }
}
