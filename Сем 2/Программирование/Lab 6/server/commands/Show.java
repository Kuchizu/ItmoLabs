package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

/**
 * Shows all collection elements.
 */
public class Show extends Command{
    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDesc() {
        return "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    }

    @Override
    public String execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            return "Clear collection, nothing to show.";
        }

        return flat.stream().map(Flat::toString).collect(Collectors.joining());
    }
}
