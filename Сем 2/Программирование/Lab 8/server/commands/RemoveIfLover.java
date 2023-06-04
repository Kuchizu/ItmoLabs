package commands;

import collections.Flat;
import managers.DBManager;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Removes all elements in collection that lower than arg.
 */
public class RemoveIfLover extends Command {
    @Override
    public String getName() {
        return "remove_lower";
    }

    @Override
    public String getDesc() {
        return "Удалить из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public String execute(String arg, String login) throws SQLException {
        if (arg == null) {
            return "Usage: remove_lower {arg}";
        }

        long val;
        try {
            val = Long.parseLong(arg.trim());
        } catch (NumberFormatException n) {
            return "Invalid argument, " + arg + " can not be parsed to int";
        }

        int x = 0;
        for (Flat f : new ArrayDeque<>(DBManager.getData())) {
            if (f.getId() < val && Objects.equals(DBManager.getFlatOwnerLogin(f.getId()), login)) {
                DBManager.removeflat(f.getId());
                x++;
            }
        }

        return String.format("Removed %s objects in collection.", x);
    }
}
