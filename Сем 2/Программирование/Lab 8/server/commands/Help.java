package commands;


import managers.CommandExecutor;

import java.net.SocketException;

/**
 * Shows all available commands.
 */
public class Help extends Command {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDesc() {
        return "Вывести справку по доступным командам";
    }

    @Override
    public String execute(String arg, String login) throws SocketException {

        StringBuilder res = new StringBuilder().append("\n");
        for(Command cmd: CommandExecutor.getCommands().values()){
            res.append(cmd.getName()).append(": ").append(cmd.getDesc()).append("\n");
        }

        return res.toString();
    }

}
