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
    public void execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            System.out.println("Clear collection, nothing to show.");
            return;
        }

        Iterator<Flat> it = flat.descendingIterator();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
