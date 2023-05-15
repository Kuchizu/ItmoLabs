package managers;

import commands.*;
import exceptions.CreateObjException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

class EofIndicatorClass implements Serializable{}
/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private static int port;
    private static final Logger LOGGER = Logger.getLogger(CommandExecutor.class.getName());

    public CommandExecutor(){
        port = 1234;
    }
    public CommandExecutor(String[] args){
        port = Integer.parseInt(args[0]);
    }

    class JTread extends Thread{
        public void run(){

            Scanner scan = new Scanner(System.in);
            while(true){
                String s = scan.nextLine();
                LOGGER.info(s);
                if(s.equals("save")){
                    System.out.println("[Server]: Изменения сохранены.");
                }
                else{
                    System.err.println("Неизвестная команда");
                }
            }
        }
    }

    private static final Map<String, Command> commands = new HashMap<>(){
        {
            put("help", new Help());
            put("info", new Info());
            put("show", new Show());
            put("clear", new Clear());
            put("head", new Head());
            put("remove_head", new RemoveHead());
            put("add", new Add());
            put("average_of_time_to_metro_by_transport", new Averagemetrotime());
            put("print_descending", new ReversePrint());
            put("update", new UpdateElement());
            put("remove_by_id", new RemoveById());
            put("execute_script", new ExecuteScript());
            put("remove_lower", new RemoveIfLover());
            put("count_by_time_to_metro_on_foot", new CountMetroFootTime());
            put("exit", new Exit());
        }
    };

    public static Map<String, Command> getCommands(){
        return commands;
    }
    /**
     *
     */
    public void run() throws IOException {

        System.setOut(new LogPrinter(System.out));

        JTread t = new JTread();
        t.start();

        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);

        } catch (BindException e){
            System.err.printf("Server already running in other place. Port %s is busy.\n", port);
            System.exit(0);

        }

        while(true){
            try {
                DatagramPacket cmdPacket = new DatagramPacket(new byte[3000],3000);
                datagramSocket.receive(cmdPacket);

                InetAddress inetAddress = cmdPacket.getAddress();
                int port = cmdPacket.getPort();

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cmdPacket.getData()));

                Object obj;
                InfoPacket inf = null;
                while (!((obj = ois.readObject()) instanceof EofIndicatorClass)) {
                    inf = (InfoPacket) obj;
                }
                assert inf != null;

                switch (inf.getCmd()){
                    case "connectme" -> {
                        System.out.printf("[%s][%s]: [Connected]\n", inetAddress, port);
                        continue;
                    }
                    case "exit" -> {
                        System.out.printf("[%s][%s]: [Disconnected]\n", inetAddress, port);
                        continue;
                    }
                }


                System.out.printf(
                        "[%s][%s]: Got message:\n%s\n",
                        inetAddress, port, inf
                );

                System.out.println();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                InfoPacket infoPacket = new InfoPacket(null, null);

                switch (inf.getCmd()) {
                    case "add" -> {
                        DBManager.addElement(inf.getFlat(), inf.getLogin(), inf.getPassword());
                        infoPacket.setCmd("Объект " + inf.getFlat().getName() + " добавлен в коллекцию.");
                    }
                    case "update" -> {
                        String owner = DBManager.getFlatOwnerLogin(Integer.parseInt(inf.getArg()));
                        if(owner == null) {
                            infoPacket.setCmd(String.format("Object by id %s not found", inf.getArg()));
                        } else if(!owner.equals(inf.getLogin())){
                            infoPacket.setCmd("You don't have permission to modify this object.");
                        } else {
                            DBManager.changeElement(Integer.parseInt(inf.getArg()), inf.getFlat(), inf.getLogin(), inf.getPassword());
                            infoPacket.setCmd("Объект " + inf.getFlat().getName() + " изменён");
                        }
                    }
                    case "execute_script" -> {
                        infoPacket.setCmd(new ExecuteScript().execute(inf.getArg(), inf.getLogin(), inf.getPassword()));
                    }
                    case "/login" -> {
                        String[] log = DBManager.check_user(inf.getLogin(), inf.getPassword());

                        if(log[0].equals("Succ")){
                            infoPacket.setCmd("/login");
                            infoPacket.setArg(log[1]);
                            infoPacket.setLogin(inf.getLogin());
                            infoPacket.setPassword(inf.getPassword());
                        }
                        else{
                            infoPacket.setCmd(log[1]);
                        }
                    }
                    case "/reg" -> {
                        String[] reg = DBManager.reg_user(inf.getLogin(), inf.getPassword());
                        if(reg[0].equals("Succ")){
                            infoPacket.setCmd("/reg");
                            infoPacket.setArg(reg[1]);
                            infoPacket.setLogin(inf.getLogin());
                            infoPacket.setPassword(inf.getPassword());
                        }
                        else{
                            infoPacket.setCmd(reg[1]);
                        }
                    }
                    default -> infoPacket.setCmd(commands.get(inf.getCmd()).execute(inf.getArg(), inf.getLogin()));
                }

                oos.writeObject(infoPacket);
                oos.flush();
                oos.close();

                byte[] buffer = baos.toByteArray();
                DatagramPacket respPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                datagramSocket.send(respPacket);

                System.out.printf("[%s][%s]: Sent response.\n\n", inetAddress, port);

            } catch (IOException | ClassNotFoundException | ParserConfigurationException | TransformerException | CreateObjException | SAXException e) {
                System.err.println("Какие-то траблы:");
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}