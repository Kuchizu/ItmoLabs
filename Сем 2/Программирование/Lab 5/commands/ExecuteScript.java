package commands;

import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ExecuteScript extends Command {
    @Override
    public String getName() {
        return "Head";
    }

    @Override
    public String getDesc() {
        return "Show first element of the collection.";
    }

    @Override
    public void execute(String arg) throws IOException, CreateObjException, ParserConfigurationException, TransformerException, SAXException {

        final Map<String, Command> commands = new HashMap<>();
        commands.put("help", new Help());
        commands.put("info", new Info());
        commands.put("show", new Show());
        commands.put("add", new Add());
        commands.put("clear", new Clear());
        commands.put("save", new Save());
        commands.put("exit", new Exit());
        commands.put("head", new Head());
        commands.put("remove_head", new RemoveHead());
        commands.put("average_of_time_to_metro_by_transport", new Averagemetrotime());
        commands.put("print_descending", new ReversePrint());
        commands.put("update", new UpdateElement());
        commands.put("remove_by_id", new RemoveById());
        commands.put("execute_script", new ExecuteScript());
        commands.put("remove_lower", new RemoveIfLover());
        commands.put("count_by_time_to_metro_on_foot", new CountMetroFootTime());

        Scanner scanner = new Scanner(new File(arg));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String cmd = line.split(" ")[0];
            String argg = null;
            if (line.split(" ").length == 2){
                argg = line.split(" ")[1];
            }

            commands.get(cmd).execute(argg);
        }
        scanner.close();
    }
}
