package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

public class Head extends Command {
    @Override
    public String getName() {
        return "Head";
    }

    @Override
    public String getDesc() {
        return "Show first element of the collection.";
    }

    @Override
    public void execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();
        System.out.println(flat.getFirst());
    }
}
