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
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
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

    static class JTread extends Thread{
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

    private static final ConcurrentMap<String, Command> commands = new ConcurrentHashMap<>(){
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

    private final ExecutorService sendResponseExecutor = Executors.newFixedThreadPool(100);
    private final ForkJoinPool readAndHandleRequestPool = new ForkJoinPool();

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
            DatagramSocket finalDatagramSocket = datagramSocket;
            try {
                readAndHandleRequestPool.submit(() -> {
                    try {
                        DatagramPacket cmdPacket = new DatagramPacket(new byte[3000], 3000);
                        finalDatagramSocket.receive(cmdPacket);

                        InetAddress inetAddress = cmdPacket.getAddress();
                        int port = cmdPacket.getPort();

                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cmdPacket.getData()));

                        Object obj;
                        InfoPacket inf = null;
                        while (!((obj = ois.readObject()) instanceof EofIndicatorClass)) {
                            inf = (InfoPacket) obj;
                        }
                        assert inf != null;

                        InfoPacket finalInf = inf;
                        ForkJoinPool.commonPool().submit(() -> {
                                    switch (finalInf.getCmd()) {
                                        case "connectme" -> System.out.printf("[%s][%s]: [Connected]\n", inetAddress, port);
                                        case "exit" -> System.out.printf("[%s][%s]: [Disconnected]\n", inetAddress, port);
                                    }


                                    System.out.printf(
                                            "[%s][%s]: Got message:\n%s\n",
                                            inetAddress, port, finalInf
                                    );

                                    System.out.println();

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ObjectOutputStream oos;
                                    try {
                                        oos = new ObjectOutputStream(baos);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    InfoPacket infoPacket = new InfoPacket(null, null);

                                    try {
                                        switch (finalInf.getCmd()) {
                                            case "add" -> {
                                                DBManager.addElement(finalInf.getFlat(), finalInf.getLogin(), finalInf.getPassword());
                                                System.out.println("+++++");
                                                infoPacket.setCmd("Объект " + finalInf.getFlat().getName() + " добавлен в коллекцию.");
                                            }

                                            case "update" -> {
                                                String owner;
                                                owner = DBManager.getFlatOwnerLogin(Integer.parseInt(finalInf.getArg()));
                                                if (owner == null) {
                                                    infoPacket.setCmd(String.format("Object by id %s not found", finalInf.getArg()));

                                                } else if (!owner.equals(finalInf.getLogin())) {
                                                    infoPacket.setCmd("You don't have permission to modify this object.");

                                                } else {
                                                    DBManager.changeElement(Integer.parseInt(finalInf.getArg()), finalInf.getFlat(), finalInf.getLogin(), finalInf.getPassword());
                                                    infoPacket.setCmd("Объект " + finalInf.getFlat().getName() + " изменён");
                                                }
                                            }

                                            case "execute_script" -> {
                                                infoPacket.setCmd(new ExecuteScript().execute(finalInf.getArg(), finalInf.getLogin(), finalInf.getPassword()));
                                            }

                                            case "/login" -> {
                                                String[] log;
                                                log = DBManager.check_user(finalInf.getLogin(), finalInf.getPassword());

                                                if (log[0].equals("Succ")) {
                                                    infoPacket.setCmd("/login");
                                                    infoPacket.setArg(log[1]);
                                                    infoPacket.setLogin(finalInf.getLogin());
                                                    infoPacket.setPassword(finalInf.getPassword());
                                                } else {
                                                    infoPacket.setCmd(log[1]);
                                                }
                                            }

                                            case "/reg" -> {
                                                String[] reg;
                                                reg = DBManager.reg_user(finalInf.getLogin(), finalInf.getPassword());

                                                if (reg[0].equals("Succ")) {
                                                    infoPacket.setCmd("/reg");
                                                    infoPacket.setArg(reg[1]);
                                                    infoPacket.setLogin(finalInf.getLogin());
                                                    infoPacket.setPassword(finalInf.getPassword());
                                                } else {
                                                    infoPacket.setCmd(reg[1]);
                                                }
                                            }

                                            case "/loadDB" -> {
                                                System.out.println("DB");
                                                System.out.println(DBManager.getData().size());
                                                infoPacket.setDB(DBManager.getData());
                                            }

                                            default -> infoPacket.setCmd(commands.get(finalInf.getCmd()).execute(finalInf.getArg(), finalInf.getLogin()));

                                        }

                                    } catch (SQLException | ParserConfigurationException | IOException | TransformerException |
                                             SAXException | CreateObjException e) {
                                        infoPacket.setCmd("Ошибка при выполнении SQL запроса");

                                    }

                                    try {
                                        oos.writeObject(infoPacket);
                                        oos.flush();
                                        oos.close();

                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    sendResponseExecutor.submit(() -> {
                                        try {
                                            byte[] buffer = baos.toByteArray();
                                            DatagramPacket respPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                                            finalDatagramSocket.send(respPacket);

                                            System.out.printf("[%s][%s]: Sent response.\n\n", inetAddress, port);
                                        } catch (IOException e) {
                                            System.err.println("Ошибка при отправки запроса");
                                            e.printStackTrace();
                                        }
                                    });
                                }
                        );
                    } catch (Exception e) {
                        System.err.println("Unknown error");
                        e.printStackTrace();
                    }
                });
            } catch (RejectedExecutionException e){
                break;
            }
        }
    }
}
