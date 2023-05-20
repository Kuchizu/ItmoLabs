package commands;

/**
 * Exites CLI.
 */
public class Exit extends Command {
    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDesc() {
        return "Завершить программу (без сохранения в файл)";
    }

    @Override
    public String execute(String arg, String login) {
        System.out.println("BYE!");
        System.exit(0);
        return arg;
    }
}
