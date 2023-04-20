package commands;

import collections.Flat;
import managers.XMLManager;

import java.util.ArrayDeque;

/**
 * Shows information about collection.
 */
public class Info extends Command {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDesc() {
        return "Вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    }

    @Override
    public void execute(String arg) {

        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            System.out.println("Clear collection, nothing to show.");
            return;
        }

        System.out.println(
                String.format(
                    """
                    
                    Collection ArrayDeque
                    Size: %s elements
                    CreationDate: %s
                    LastElementID: %s
                    
                    """, flat.size(), flat.getFirst().getCreationDate(), flat.getLast().getId()
                )
        );
    }

}
