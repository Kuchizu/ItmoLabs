package commands;

import collections.Flat;
import managers.DBManager;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Returns CountMetroFootTime of all elements in collection.
 */
public class CountMetroFootTime extends Command {
    @Override
    public String getName() {
        return "count_by_time_to_metro_on_foot timeToMetroOnFoot";
    }

    @Override
    public String getDesc() {
        return "Вывести количество элементов, значение поля timeToMetroOnFoot которых равно заданному";
    }

    @Override
    public String execute(String vall, String login) {
        if (vall == null) {
            return "Usage: count_by_time_to_metro_on_foot {arg}";
        }

        Float val = Float.parseFloat(vall);
        ArrayDeque<Flat> flats = DBManager.getData();

        int x = 0;
        for (Flat flat : flats) {
            if (Objects.equals(flat.getTimeToMetroOnFoot(), val)) {
                x++;
            }
        }

        return "Количество элементов, значение поля timeToMetroOnFoot которых равно заданному: " + x;
    }
}
