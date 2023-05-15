package commands;

import collections.Flat;
import managers.DBManager;

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
    public String execute(String arg, String login) {
        ArrayDeque<Flat> flats = DBManager.getData();

        if(flats.size() == 0){
            return "Clear collection, nothing to show.";
        }
        
        StringBuilder res = new StringBuilder();

        Iterator<Flat> it = flats.descendingIterator();
        while(it.hasNext()) {
            res.append(it.next()).append("\n");
        }

        return res.toString();
    }
}
