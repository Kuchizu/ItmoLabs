package commands;

import collections.Flat;
import managers.XMLManager;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * Shows information about collection.
 */
public class Info extends Command implements Serializable {
    private static final long serialVersionUID = 1;
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDesc() {
        return "Вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    }

    @Override
    public String execute(String arg) {

        ArrayDeque<Flat> flat = XMLManager.getData();

        if(flat.size() == 0){
            return "Collection ArrayDeque\nSize: 0 elements\n";
        }

        return String.format(
            """            
            Collection ArrayDeque
            Size: %s elements
            CreationDate: %s
            LastElementID: %s
            """,
                flat.size(), flat.getFirst().getCreationDate(), flat.getLast().getId()
        );

    }

}
