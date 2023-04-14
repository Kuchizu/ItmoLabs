package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

public class Show extends Command {
    @Override
    public String getName() {
        return "Info";
    }

    @Override
    public String getDesc() {
        return "Show information about colleciton.";
    }

    @Override
    public void execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();
        if(flat.size() == 0){
            System.out.println("Clear collection, nothing to show.");
            return;
        }
        flat.forEach(System.out::println);
    }
}
