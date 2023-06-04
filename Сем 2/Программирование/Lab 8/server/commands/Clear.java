package commands;

import collections.Flat;
import managers.DBManager;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Objects;

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
        for (Flat f : new ArrayDeque<>(DBManager.getData())) {
            if (Objects.equals(DBManager.getFlatOwnerLogin(f.getId()), login)) {
                DBManager.removeflat(f.getId());
                x++;
            }
        }

        return String.format("Removed %s objects from collection.", x);
    }
}
