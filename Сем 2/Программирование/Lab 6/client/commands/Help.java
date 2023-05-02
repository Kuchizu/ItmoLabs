package commands;

import managers.CommandExecutor;

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
    public String execute(String arg) {
        System.out.println();
        for(Command cmd: CommandExecutor.getCommands().values()){
            System.out.println(cmd.getName() + ": " + cmd.getDesc());
        }
        System.out.println();
        return null;
    }

}
