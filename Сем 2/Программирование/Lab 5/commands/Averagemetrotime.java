package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

public class Averagemetrotime extends Command {
    @Override
    public String getName() {
        return "average_of_time_to_metro_by_transport";
    }

    @Override
    public String getDesc() {
        return "Вывести среднее значение поля timeToMetroByTransport для всех элементов коллекции";
    }

    @Override
    public void execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            System.out.println("Clear collection, no average stats to show.");
            return;
        }

        AtomicReference<Double> avgm = new AtomicReference<>(0.0);

        flat.forEach((x) -> avgm.updateAndGet(v -> v + x.getTimeToMetroByTransport()));

        System.out.println("Среднее значение timeToMetroByTransport: " + avgm);

    }

}
