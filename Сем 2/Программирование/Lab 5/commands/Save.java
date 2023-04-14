package commands;

import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Save extends Command {
    @Override
    public String getName() {
        return "Save";
    }

    @Override
    public String getDesc() {
        return "Сохранить коллекцию в файл";
    }

    @Override
    public void execute(String arg) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        XMLManager.writeToFile("Data.xml");
        System.out.println("Успешно сохранено.");
    }
}
