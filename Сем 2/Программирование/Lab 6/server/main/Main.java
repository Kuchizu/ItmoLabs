package main;

import collections.Flat;
import exceptions.CreateObjException;
import managers.CommandExecutor;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/***

 * AYO MAIN
 * Author Kuchizu
 * Var: 3131015
 * <p>
 * Passed 19.04.2023 85 / 100

 ***/

public class Main {
    /**
     * The environment key to the CSV file for storing the collection.
     */
    public static final String ENV_KEY = "Data.xml";
    /**
     The main method that loads the collection from the XML file and starts the user interface.
     @param args an array of command-line arguments for the application
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException {

        XMLManager.loadData(ENV_KEY);
        CommandExecutor executor = new CommandExecutor();

        System.out.println("Welcome to Server!\nCollection: " + XMLManager.getData().getClass().getName() + "\nDB type: XML" + "\nElement type: " + Flat.class.getName() + "\nConnection protocol: UDP");
        System.out.println("Server started handling.\n");

        executor.run();

    }
}
