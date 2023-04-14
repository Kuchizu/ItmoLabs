package commands;

public class Exit extends Command {
    @Override
    public String getName() {
        return "Exit";
    }

    @Override
    public String getDesc() {
        return "Exit CLI.";
    }

    @Override
    public void execute(String arg) {
        System.out.println("BYE!");
        System.exit(0);
    }
}
