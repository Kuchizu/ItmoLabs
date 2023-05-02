package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

import java.util.*;

/**
 * Prints all collection elements reversed.
 */
public class ReversePrint extends Command {
    @Override
    public String getName() {
        return "print_descending";
    }

    @Override
    public String getDesc() {
        return "Вывести элементы коллекции в порядке убывания";
    }

    @Override
    public String execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            return "Clear collection, nothing to show.";
        }
        
        StringBuilder res = new StringBuilder();

        Iterator<Flat> it = flat.descendingIterator();
        while(it.hasNext()) {
            res.append(it.next()).append("\n");
        }

        return res.toString();
    }
}
