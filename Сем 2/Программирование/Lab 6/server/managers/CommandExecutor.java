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

class JTread extends Thread{
    public void run(){

        System.out.printf("Server started running \n", Thread.currentThread().getName());
        Scanner scan = new Scanner(System.in);
        while(true){
            if(scan.nextLine().equals("save")){
                try {
                    XMLManager.writeToFile(Main.ENV_KEY);
                } catch (ParserConfigurationException ignored){}
            }
            else{
                System.err.println("Неизвестная команда");
            }
        }
        System.out.printf("%s fiished... \n", Thread.currentThread().getName());
    }
}

/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private byte[] buffer = new byte[10000];

    private static final Map<String, Command> commands = new HashMap<>(){
        {
            put("help", new Help());
            put("info", new Info());
            put("show", new Show());
            put("add", new Add());
            put("clear", new Clear());
            put("save", new Save());
            put("exit", new Exit());
            put("head", new Head());
            put("remove_head", new RemoveHead());
            put("average_of_time_to_metro_by_transport", new Averagemetrotime());
            put("print_descending", new ReversePrint());
            put("update", new UpdateElement());
            put("remove_by_id", new RemoveById());
            put("execute_script", new ExecuteScript());
            put("remove_lower", new RemoveIfLover());
            put("count_by_time_to_metro_on_foot", new CountMetroFootTime());
        }
    };

    public Map<String, Command> getCommands(){
        return commands;
    }
    /**
     *
     */
    public void run() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket(1234);

        HashMap connectedClients = new HashMap();

        while(true){
            try {
                DatagramPacket cmdPacket = new DatagramPacket(new byte[10000],10000);
                datagramSocket.receive(cmdPacket);

                InetAddress inetAddress = cmdPacket.getAddress();
                int port = cmdPacket.getPort();

                if(connectedClients.contains(<>)){

                }

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cmdPacket.getData()));

                Object obj;
                InfoPacket inf = null;
                while (!((obj = ois.readObject()) instanceof EofIndicatorClass)) {
                    inf = (InfoPacket) obj;
                }
                assert inf != null;

                System.out.printf(
                        "[%s][%s]: Got message:\n%s\n%s: %s\n",
                        inetAddress, port, inf,
                        Arrays.toString(cmdPacket.getData()).length(),
                        Arrays.toString(cmdPacket.getData())
                );

                String resp;

                if (inf.getCmd().equals("add")) {
                    XMLManager.addElement(inf.getFlat());
                    resp = "Объект " + inf.getFlat().getName() + " добавлен в коллекцию.";
                } else if (inf.getCmd().equals("update")) {
                    XMLManager.changeElement(Integer.parseInt(inf.getArg()), inf.getFlat());
                    resp = "Объект " + inf.getFlat().getName() + " изменён";
                } else {
                    resp = commands.get(inf.getCmd()).execute(inf.getArg());
                }

                // System.out.println("Executed: " + resp);

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