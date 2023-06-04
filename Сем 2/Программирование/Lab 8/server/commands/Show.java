package commands;

import collections.Flat;
import managers.DBManager;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

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
    public String execute(String arg, String login) {
        ArrayDeque<Flat> flats = DBManager.getData();

        if (flats.size() == 0) {
            return "Clear collection, nothing to show.";
        }

        return flats.stream().map(Flat::toString).collect(Collectors.joining());
    }
}
