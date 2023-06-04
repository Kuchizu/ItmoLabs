package commands;

import collections.Flat;
import managers.DBManager;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Returns Averagemetrotime of all elements in collection.
 */
public class Averagemetrotime extends Command {
    @Override
    public String getName() {
        return "average_of_time_to_metro_by_transport";
    }

    @Override
    public String getDesc() {
        return "Вывести среднее значение поля timeToMetroByTransport для всех элементов коллекции";
    }

    /**
     * Return timeToMetroByTransport
     *
     * @param arg
     * @return
     */
    @Override
    public String execute(String arg, String login) {
        ArrayDeque<Flat> flats = DBManager.getData();

        if (flats.size() == 0) {
            System.out.println("Clear collection, no average stats to show.");
            return arg;
        }

        AtomicReference<Double> avgm = new AtomicReference<>(0.0);

        flats.forEach((x) -> avgm.updateAndGet(v -> v + x.getTimeToMetroByTransport()));

        return "Среднее значение timeToMetroByTransport: " + avgm.get() / flats.size();
    }

}
