package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CountMetroFootTime extends Command {
    @Override
    public String getName() {
        return "count_by_time_to_metro_on_foot";
    }

    @Override
    public String getDesc() {
        return "Вывести количество элементов, значение поля timeToMetroOnFoot которых равно заданному";
    }

    public void execute(String vall) {
        if(vall == null){
            System.err.println("Usage: count_by_time_to_metro_on_foot {arg}");
            return;
        }

        Float val = Float.parseFloat(vall);
        ArrayDeque<Flat> flats = XMLManager.getData();

        int x = 0;

        for(Flat flat: flats){
            if(Objects.equals(flat.getTimeToMetroOnFoot(), val)){
                x++;
            }
        }

        System.out.println("Количество элементов, значение поля timeToMetroOnFoot которых равно заданному: " + x);

    }
}
