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
    public void execute(String arg) throws IOException, CreateObjException, ParserConfigurationException, TransformerException, SAXException {
        if(arg == null){
            System.err.println("Usage: execute_script: {file_name}");
            return;
        }



        Map<String, Command> commands = CommandExecutor.getCommands();


        Scanner scanner;
        try {
            scanner = new Scanner(new File(arg));
        }
        catch (java.io.FileNotFoundException f){
            System.err.println("Файл не существует, но суч файл ор директорииииии");
            return;
        }
        catch (java.lang.NullPointerException n) {
            System.err.println("Ошибка чтения файла");
            return;
        }
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
                            name,
                            new Coordinates(Long.parseLong(coordinates[0]), Double.parseDouble(coordinates[1])),
                            area,
                            numberOfRooms,
                            timeToMetroOnFoot,
                            timeToMetroByTransport,
                            furnish,
                            new House(hname, hyear, hnumberOfFloors, hnumberOfLifts)
                    );
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.err.println("Ошибка при выполнении команды add.: " + e.getMessage());
                    return;
                }
                if(cmd.equals("add")){
                    XMLManager.addElement(flat);
                    System.out.println("Объект " + flat.getName() + " добавлен в коллекцию.");
                }
                else{
                    XMLManager.changeElement(Integer.parseInt(arg), flat);
                    System.out.println("Объект " + flat.getName() + " изменён");
                }
                continue;
            }

            if (commands.containsKey(cmd)) {
                commands.get(cmd).execute(sarg);
            }
            else{
                System.err.println("Неизевстная комманда: " + line);
            }
        }

    }
}
