package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

import java.util.*;

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

        Iterator<Flat> it = flat.descendingIterator();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
