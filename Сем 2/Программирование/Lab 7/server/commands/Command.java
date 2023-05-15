package commands;

import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Abstract class for command classes ran by CLI.
 */
public abstract class Command {

    public abstract String getName();
    public abstract String getDesc();
    public abstract String execute(String arg, String pass) throws ParserConfigurationException, IOException, TransformerException, SAXException, CreateObjException, SQLException;
}
