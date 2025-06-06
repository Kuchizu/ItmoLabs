package commands;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import exceptions.CreateObjException;
import managers.CommandExecutor;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Scanner;

/**
 * ExecuteScript class that reads and executes file.
 */
public class ExecuteScript extends Command {
    @Override
    public String getName() {
        return "execute_script file_name";
    }

    @Override
    public String getDesc() {
        return "Cчитать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.";
    }

    @Override
    public String execute(String arg) throws IOException, CreateObjException, ParserConfigurationException, TransformerException, SAXException {
        if(arg == null){
            System.err.println("Usage: execute_script: {file_name}");
            return null;
        }

        Map<String, Command> commands = CommandExecutor.getCommands();
        
        StringBuilder response = new StringBuilder();
        Scanner scanner = new Scanner(arg);

        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            System.out.println(line);
            if (line.isEmpty()) continue;

            String cmd = line.split(" ")[0];

            String sarg = null;
            if (line.split(" ").length == 2){
                sarg = line.split(" ")[1];
            }

            if (cmd.equals("add") || cmd.equals("update")) {
                Flat flat;
                try {

                    String name = scanner.nextLine().trim();
                    String cord = scanner.nextLine().trim();
                    int area = scanner.nextInt();
                    int numberOfRooms = scanner.nextInt();
                    float timeToMetroOnFoot = scanner.nextFloat();
                    double timeToMetroByTransport = scanner.nextDouble();
                    scanner.nextLine();
                    Furnish furnish = Furnish.valueOf(scanner.nextLine().trim());
                    String hname = scanner.nextLine();
                    long hyear = scanner.nextLong();
                    long hnumberOfFloors = scanner.nextLong();
                    int hnumberOfLifts = scanner.nextInt();
                    String[] coordinates = cord.split("\\s+");

                    flat = new Flat(
                            -1,
                            name,
                            new Coordinates(Long.parseLong(coordinates[0]), Double.parseDouble(coordinates[1])),
                            ZonedDateTime.now(),
                            area,
                            numberOfRooms,
                            timeToMetroOnFoot,
                            timeToMetroByTransport,
                            furnish,
                            new House(hname, hyear, hnumberOfFloors, hnumberOfLifts)
                    );
                } catch (Exception e) {
                    response.append("Ошибка при выполнении команды add.: ").append(e.getMessage());
                    return response.toString();
                }
                if(cmd.equals("add")){
                    XMLManager.addElement(flat);
                    response.append("Объект ").append(flat.getName()).append(" добавлен в коллекцию.");
                }
                else{
                    XMLManager.changeElement(Integer.parseInt(arg), flat);
                    response.append("Объект ").append(flat.getName()).append(" изменён");
                }
                continue;
            }

            if (commands.containsKey(cmd)) {
                response.append(commands.get(cmd).execute(sarg));
            }
            else{
                response.append("Неизевстная комманда: ").append(line);
            }
        }
        return response.toString();
    }
}
