//import collection.City.City;
//import collectionManagers.CityManager;
//import collectionManagers.CollectionManager;
//import commandManagers.CommandExecutor;
//import commandManagers.CommandMode;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import commands.Add;
import exceptions.CreateObjException;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/***

 * AYO MAIN
 * @Author Kuchizu

 ***/

public class Main {
    /**
     * The environment key to the CSV file for storing the collection.
     */
    private static final String ENV_KEY = "Data.xml";

    /**
     The main method that loads the collection from the XML file and starts the user interface.
     @param args an array of command-line arguments for the application
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException, InterruptedException, CreateObjException {

        Flat f1 = new Flat("Name1", new Coordinates(2, 4), 1, 12, (float) 123.3, 123.3, Furnish.FINE, new House("Khona", (long) 123, 1, 2));
        Flat f2 = new Flat("Name2", new Coordinates(2, 4), 1, 12, (float) 123.3, 123.3, Furnish.FINE, new House("Khona", (long) 123, 1, 2));
//        Flat f3 = new Flat("Name3", new Coordinates(2, 4), 1, 12, (float) 123.3, 123.3, Furnish.FINE, new House("Khona", (long) 123, 1, 2));
//        ArrayDeque<Flat> kero = new ArrayDeque<>();
//
//        kero.add(f1);
//        kero.add(f3);
//        kero.add(f2);

        XMLManager.loadData(ENV_KEY);

        System.out.println(XMLManager.getData().size());
        XMLManager.addElement(f1);
        System.out.println(XMLManager.getData().size());

        new Add().execute();



//        CityManager loader = new CityManager();
//        loader.loadCollection(ENV_KEY);
//        CollectionManager<TreeSet<City>, City> manager = CityManage
//        r.getInstance();
//
//        // commands
//        System.out.println("Welcome to CLI! Now you are operating with collection of \n  type: " + manager.getCollection().getClass().getName() + ", \n  filled with elements of type: " + manager.getFirstOrNew().getClass().getName());
//        System.out.println("Now you can enter the commands. Use help for reference.");

//        CommandExecutor executor = new CommandExecutor();
//        executor.startExecuting(System.in, CommandMode.CLI_UserMode);

    }
}
