package commands;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Saves new changes in file.
 */
public class Save extends Command {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDesc() {
        return "Сохранить коллекцию в файл";
    }

    @Override
    public String execute(String arg, String login) throws ParserConfigurationException, IOException, TransformerException, SAXException {
//        if (DBManager.saveDB()){
//            return "Успешно сохранено.";
//        }
//        return "Ошибка при сохранении";

        return null;
    }
}
