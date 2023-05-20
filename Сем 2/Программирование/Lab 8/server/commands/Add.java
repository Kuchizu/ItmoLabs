package commands;

import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Add class used to add elements to collection.
 */
public class Add extends Command {
    public String getName() {
        return "add {element}";
    }

    public String getDesc() {
        return "Добавить новый элемент в коллекцию";
    }

    @Override
    public String execute(String arg, String login) throws ParserConfigurationException, IOException, TransformerException, SAXException, CreateObjException {
        return null;
    }
}
