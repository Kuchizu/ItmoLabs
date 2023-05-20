package main;

import collections.Flat;
import exceptions.CreateObjException;
import managers.CommandExecutor;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/***

 * AYO MAIN
 * Author Kuchizu
 * Var: 3131704
 * <p>
 * Passed 17.05.2023 90 / 100

 ***/

public class Main {
    /**
     * The environment key to the CSV file for storing the collection.
     */
    public static final String ENV_KEY = "Data.xml";

    /**
     * The main method that loads the collection from the XML file and starts the user interface.
     *
     * @param args an array of command-line arguments for the application
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException {

        XMLManager.loadData(ENV_KEY);

        CommandExecutor executor;
        if (args.length == 2) {
            executor = new CommandExecutor(args);
        } else {
            executor = new CommandExecutor();
        }

        System.out.println("Welcome to CLI!\nCollection: " + XMLManager.getData().getClass().getName() + "\nDB type: XML" + "\nElement type: " + Flat.class.getName());
        System.out.println("Enter commands below. Use help for more information.\n\nUse /login to login to the system.\nUse /reg to register into the system.\n");

        executor.run(System.in);

    }
}
