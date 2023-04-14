//import collection.City.City;
//import collectionManagers.CityManager;
//import collectionManagers.CollectionManager;
//import commandManagers.CommandExecutor;
//import commandManagers.CommandMode;

import exceptions.CreateObjException;
import managers.CommandExecutor;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/***

 * AYO MAIN
 * @Author Kuchizu
 * Var: 3131015

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
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException, CreateObjException {


        XMLManager.loadData(ENV_KEY);
        CommandExecutor executor = new CommandExecutor();
        executor.run(System.in);

    }
}
