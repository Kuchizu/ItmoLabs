package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.HashMap;
import java.util.Map;

public class UpdateElement extends Command {
    @Override
    public String getName() {
        return "Head";
    }

    @Override
    public String getDesc() {
        return "Show first element of the collection.";
    }

    @Override
    public void execute(String arg){
        for(Flat flat: XMLManager.getData()){
            if(flat.getId() == Integer.parseInt(arg)){
                System.out.println("Введите значение которое хотите поменять");
                System.out.println("Введите новое значение");
            }
        }
    }
}
