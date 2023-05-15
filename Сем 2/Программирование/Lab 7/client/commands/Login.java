package commands;

import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Scanner;

public class Login extends Command {
    public String getName() {
        return "login {element}";
    }

    public String getDesc() {
        return "Login to the system";
    }

    @Override
    public String execute(String arg) throws ParserConfigurationException, IOException, TransformerException, SAXException, CreateObjException {
        return null;
    }

    public String[] login(boolean reg) {
        Scanner obj = new Scanner(System.in);

        if(reg){
            System.out.println("Регистрация нового пользователя");
        }

        System.out.print("Введите логин: ");
        String login = obj.nextLine();
        System.out.print("Введите пароль: ");
        String pass = obj.nextLine();

        return new String[]{login, pass};
    }

}