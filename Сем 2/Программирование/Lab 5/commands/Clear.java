package commands;

import managers.XMLManager;

public class Clear extends Command {
    @Override
    public String getName() {
        return "Clear";
    }

    @Override
    public String getDesc() {
        return "Clear collection";
    }

    @Override
    public void execute(String arg) {
        XMLManager.dropAll();
        System.out.println("Cleared all collection.");
    }
}
