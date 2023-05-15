package commands;

import collections.Flat;
import managers.DBManager;

import java.sql.SQLException;

/**
 * Clears collection.
 */
public class Clear extends Command {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDesc() {
        return "Очистить коллекцию";
    }

    /**
     * Clears collection
     *
     * @param arg
     * @return
     */
    @Override
    public String execute(String arg, String login) throws SQLException {
        int x = 0;
        for(Flat f: DBManager.getData()){
            if(DBManager.getFlatOwnerLogin(f.getId()).equals(login)){
                DBManager.removeflat(f.getId());
                x++;
            }
        }

        return String.format("Removed %s objects in collection.", x);
    }
}
