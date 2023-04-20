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
    public void execute(String arg) {
        System.out.println("BYE!");
        System.exit(0);
    }
}
