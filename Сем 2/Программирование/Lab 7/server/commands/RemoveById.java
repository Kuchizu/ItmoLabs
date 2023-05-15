package commands;

import collections.Flat;
import managers.DBManager;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Removes element from collection by its id.
 */
public class RemoveById extends Command {
    @Override
    public String getName() {
        return "remove_by_id id";
    }

    @Override
    public String getDesc() {
        return "Удалить элемент из коллекции по его id";
    }
    @Override
    public String execute(String arg, String login) throws SQLException {
        if(arg == null){
            return "Usage: remove_by_id {arg}";
        }

        int flatid = Integer.parseInt(arg);

        String owner = DBManager.getFlatOwnerLogin(flatid);

        if(owner == null){
            return "Element by id: " + flatid + " not found.";
        }
        if(!owner.equals(login)){
            return "You don't have permission to modify this object.";
        }

        ArrayDeque<Flat> flats = DBManager.getData();

        int nz = flats.size();
        DBManager.removeflat(flatid);
        if(flats.size() != nz){
            return "Object by id: " + flatid + " was deleted.";
        }

        return "Element by id: " + flatid + " not found.";
    }
}
