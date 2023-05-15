package commands;

import collections.Flat;
import managers.DBManager;

import java.sql.SQLException;
import java.util.ArrayDeque;

/**
 * Returns first collection element and delete it.
 */
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
    public String execute(String arg, String login) throws SQLException {
        ArrayDeque<Flat> flat = DBManager.getData();

        if(flat.size() == 0){
            return "Clear collection, nothing to return and delete.";
        }

        Flat head = flat.getFirst();

        if(!DBManager.getFlatOwnerLogin(head.getId()).equals(login)) {
            return "You don't have permission to modify this object.";
        }

        DBManager.removeflat(head.getId());

        return head.toString();
    }
}
