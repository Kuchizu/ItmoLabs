package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;


/**
 * Removes element from collection by it id.
 */
public class RemoveById extends Command {
    @Override
    public String getName() {
        return "remove_by_id id";
    }

    @Override
    public String getDesc() {
        return "Удалить элемент из коллекции по его id";
    }

    public String dec(Object o){
        System.out.println("1");
        return null;
    }

    @Override
    public String execute(String arg) {
        if(arg == null){
            return "Usage: remove_by_id {arg}";
        }

        int val = Integer.parseInt(arg);

        ArrayDeque<Flat> flat = XMLManager.getData();
        int nz = flat.size();
        flat.removeIf((x) -> x.getId() == val);

        if(flat.size() != nz){
            System.out.println("Removed.");
        }
        else{
            System.out.println("Element by id: " + val + " not found.");
        }
        return arg;

    }
}
