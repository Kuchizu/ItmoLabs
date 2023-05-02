package managers;

import commands.*;
import exceptions.CreateObjException;
import main.Main;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

class EofIndicatorClass implements Serializable{}
/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private void saveDB() throws ParserConfigurationException {
        XMLManager.writeToFile(Main.ENV_KEY);
    }

    class JTread extends Thread{
        public void run(){

            Scanner scan = new Scanner(System.in);
            while(true){
                if(scan.nextLine().equals("save")){
                    try {
                        saveDB();
                        System.out.println("[Server]: Изменения сохранены.");
                    } catch (ParserConfigurationException ignored){}
                }
                else{
                    System.err.println("Неизвестная команда");
                }
            }
        }
    }

    private byte[] buffer = new byte[10000];

    private static final Map<String, Command> commands = new HashMap<>(){
        {
            put("help", new Help());
            put("info", new Info());
            put("show", new Show());
            put("clear", new Clear());
            put("head", new Head());
            put("remove_head", new RemoveHead());
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

    public Map<String, Command> getCommands(){
        return commands;
    }
    /**
     *
     */
    public void run() throws SocketException {

        JTread t = new JTread();
        t.start();

        DatagramSocket datagramSocket = new DatagramSocket(1234);
        while(true){
            try {
                DatagramPacket cmdPacket = new DatagramPacket(new byte[10000],10000);
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
                        XMLManager.writeToFile(Main.ENV_KEY);
                        continue;
                    }
                }


                System.out.printf(
                        "[%s][%s]: Got message:\n%s\n%s: %s\n",
                        inetAddress, port, inf,
                        Arrays.toString(cmdPacket.getData()).length(),
                        Arrays.toString(cmdPacket.getData())
                );

                String resp;

                switch (inf.getCmd()) {
                    case "add" -> {
                        XMLManager.addElement(inf.getFlat());
                        resp = "Объект " + inf.getFlat().getName() + " добавлен в коллекцию.";
                    }
                    case "update" -> {
                        XMLManager.changeElement(Integer.parseInt(inf.getArg()), inf.getFlat());
                        resp = "Объект " + inf.getFlat().getName() + " изменён";
                    }
                    default -> resp = commands.get(inf.getCmd()).execute(inf.getArg());
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                InfoPacket infoPacket = new InfoPacket(resp, null);
                oos.writeObject(infoPacket);
                oos.flush();
                oos.close();

                buffer = baos.toByteArray();
                DatagramPacket respPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                datagramSocket.send(respPacket);

                System.out.printf("[%s][%s]: Sent response.\n\n", inetAddress, port);

            } catch (IOException | ClassNotFoundException | ParserConfigurationException | TransformerException | CreateObjException | SAXException e) {
                System.err.println("Какие-то траблы:");
                e.printStackTrace();
            }
        }
    }

}