package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

/**
 * Returns first collection element and delete it.
 */
public class RemoveHead extends Command {
    @Override
    public String getName() {
        return "remove_head";
    }

    @Override
    public String getDesc() {
        return "Вывести первый элемент коллекции и удалить его";
    }

    @Override
    public String execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            return "Clear collection, nothing to return and delete.";
        }

        return flat.pop().toString();
    }
}
