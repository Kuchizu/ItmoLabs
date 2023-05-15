package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

/**
 * Removes all elements in collection that lower than arg.
 */
public class RemoveIfLover extends Command {
    @Override
    public String getName() {
        return "remove_lower";
    }

    @Override
    public String getDesc() {
        return "Удалить из коллекции все элементы, меньшие, чем заданный";
    }
    @Override
    public String execute(String arg) {
        if (arg == null) {
            System.err.println("Usage: remove_lower {arg}");
            return null;
        }

        long val = 0;
        try {
            val = Long.parseLong(arg.trim());
        } catch (NumberFormatException n) {
            System.err.println("Invalid argument, " + arg + " can not be parsed to int");
            return null;
        }

        ArrayDeque<Flat> flat = XMLManager.getData();
        int nz = flat.size();
        long finalVal = val;
        flat.removeIf((x) -> x.getId() < finalVal);

        if (flat.size() != nz) {
            System.out.println("Removed " + (nz - flat.size()) + " elements.");
        } else {
            System.out.println("Elements which id lower than " + val + " not found.");
        }
        return null;

    }
}
