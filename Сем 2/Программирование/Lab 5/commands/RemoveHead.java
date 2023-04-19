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

        if(flat.size() == 0){
            System.err.println("Clear collection, nothing to return and delete.");
            return;
        }

        System.out.println(flat.pop());

    }
}
