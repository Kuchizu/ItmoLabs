package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

/**
 * Shows all collection elements.
 */
public class Show extends Command {
    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDesc() {
        return "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
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
