package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

public class RemoveHead extends Command {
    @Override
    public String getName() {
        return "remove_head";
    }

    @Override
    public String getDesc() {
        return "Вывести первый элемент коллекции и удалить его";
    }

    @Override
    public void execute(String arg) {
        ArrayDeque<Flat> flat = XMLManager.getData();

        System.out.println(flat.pop());

    }
}
