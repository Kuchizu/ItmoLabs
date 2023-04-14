package managers;

import commands.*;
import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandExecutor {
    public void run(InputStream input) throws CreateObjException, ParserConfigurationException, IOException, TransformerException, SAXException {
        Scanner cmdScanner = new Scanner(input);

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


        while (cmdScanner.hasNext()) {
            String line = cmdScanner.nextLine().trim();
            String cmd = line.split(" ")[0];

            String arg = null;
            if (line.split(" ").length == 2){
                arg = line.split(" ")[1];
            }

            if (line.isEmpty()) continue;
            if (commands.containsKey(line)) {
                commands.get(line).execute(arg);
            }
            else{
                System.err.println("Неизевстная комманда");
            }
        }
    }
}