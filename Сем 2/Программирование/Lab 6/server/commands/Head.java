package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

/**
 * Shows first element of the collection.
 */
public class Head extends Command {
    @Override
    public String getName() {
        return "head";
    }

    @Override
    public String getDesc() {
        return "Вывести первый элемент коллекции";
    }

    @Override
    public String execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            return "Clear collection, nothing to show.";
        }

        return flat.getFirst().toString();
    }
}
