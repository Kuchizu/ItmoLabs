package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

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
    public void execute(String arg) {
        if(arg == null){
            System.err.println("Usage: remove_lower {arg}");
            return;
        }

        int val = Integer.parseInt(arg);

        ArrayDeque<Flat> flat = XMLManager.getData();
        int nz = flat.size();
        flat.removeIf((x) -> x.getId() < val);

        if(flat.size() != nz){
            System.out.println("Removed.");
        }
        else{
            System.out.println("Element by id: " + val + " not found.");
        }

    }
}
