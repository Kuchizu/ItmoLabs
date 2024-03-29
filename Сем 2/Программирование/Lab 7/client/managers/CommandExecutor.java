package managers;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import commands.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

class EofIndicatorClass implements Serializable{}

/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private final DatagramSocket datagramSocket = new DatagramSocket();
    private static String host = null;
    private static String login;
    private static String pass;
    private static byte[] buffer;
    private static int port;
    private final InetAddress inetAddress;
    private static final Logger LOGGER = Logger.getLogger(CommandExecutor.class.getName());
    private static final Map<String, Command> commands = new HashMap<>() {
        {
            put("help", new Help());
            put("info", new Info());
            put("show", new Show());
            put("add", new Add());
            put("clear", new Clear());
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
            put("/login", new Login());
            put("/reg", new Login());
        }
    };

    public CommandExecutor() throws SocketException, UnknownHostException {
        host = "localhost";
        port = 1234;
        this.inetAddress = InetAddress.getByName(host);
    }

    public CommandExecutor(String[] args) throws SocketException, UnknownHostException {
        host = args[0];
        port = Integer.parseInt(args[1]);
        this.inetAddress = InetAddress.getByName(host);
    }

    public static Map<String, Command> getCommands() {
        return commands;
    }

    static class JTread extends Thread{
        public void run(String arg, DatagramSocket datagramSocket, InetAddress inetAddress) throws IOException, InterruptedException {

            BufferedReader reader = new BufferedReader(new FileReader(arg));
            String eline = reader.readLine();
            while(eline != null){
                if(eline.equals("add")){
                    StringBuilder eadd = new StringBuilder("add");
                    for(int i = 0; i < 11; i++){
                        eadd.append(" ").append(reader.readLine());
                    }

                    System.out.println(eadd);
                    eline = reader.readLine();

                    String[] eadda = eadd.toString().split(" ");

                    Flat f =  new Flat(
                            -1,
                            eadda[1],
                            new Coordinates(Long.parseLong(eadda[2]), Double.parseDouble(eadda[2])),
                            ZonedDateTime.now(),
                            Integer.parseInt(eadda[3]),
                            Integer.parseInt(eadda[4]),
                            Float.valueOf(eadda[5]),
                            Double.parseDouble(eadda[6]),
                            Furnish.valueOf(eadda[8]),
                            new House(eadda[9], Long.parseLong(eadda[10]), Long.parseLong(eadda[11]), Integer.parseInt(eadda[12]))
                    );

                    InfoPacket addPacket = new InfoPacket("add", null);
                    addPacket.setFlat(f);

                    addPacket.setLogin(login);
                    addPacket.setPassword(pass);

                    ByteArrayOutputStream baoscc = new ByteArrayOutputStream();
                    ObjectOutputStream ooscc = new ObjectOutputStream(baoscc);
                    ooscc.writeObject(addPacket);
                    ooscc.writeObject(new EofIndicatorClass());
                    ooscc.flush();
                    ooscc.close();

                    buffer = baoscc.toByteArray();
                    datagramSocket.send(new DatagramPacket(buffer, buffer.length, inetAddress, port));
                    Thread.sleep(50);
                }

                else{
                    System.out.println(": " + eline + " :");
                    eline = reader.readLine();

                }
            }

        }
    }


    public static void userprint() {
        if (login == null) {
            System.out.print(">>> ");
        } else {
            System.out.printf("[%s] >>> ", login);
        }
    }

    /**
     * @param input Input stream commands
     */
    public void run(InputStream input) throws IOException {

        System.setOut(new LogPrinter(System.out));

        System.out.printf("Client started running on [%s][%s]\n\n", host, port);

        ByteArrayOutputStream baosc = new ByteArrayOutputStream();
        ObjectOutputStream oosc = new ObjectOutputStream(baosc);
        InfoPacket connectmePacket = new InfoPacket("connectme", null);
        oosc.writeObject(connectmePacket);
        oosc.writeObject(new EofIndicatorClass());
        oosc.flush();
        oosc.close();

        buffer = baosc.toByteArray();
        datagramSocket.setSoTimeout(3000);

        datagramSocket.send(new DatagramPacket(buffer, buffer.length, inetAddress, port));

        userprint();
        Scanner cmdScanner = new Scanner(input);

        while (true) {
            String line = cmdScanner.nextLine().trim();
            LOGGER.info(line);

            if (line.isEmpty()) {
                userprint();
                continue;
            }

            String cmd = line.split(" ")[0];
            String arg = null;
            if (line.split(" ").length == 2) {
                arg = line.split(" ")[1];
            }

            if (!(commands.containsKey(cmd))) {
                System.out.println("Неизвестная команда");
                userprint();
                continue;
            }
            if (login == null && !cmd.equals("/login") && !cmd.equals("/reg")) {
                System.out.println("Для использования команд войдите в систему (/login) или зарегистрируйтесь (/reg)");
                userprint();
                continue;
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);

                InfoPacket infoPacket = new InfoPacket(cmd, arg);
                infoPacket.setLogin(login);
                infoPacket.setPassword(pass);

                if (cmd.equals("/login") || cmd.equals("/reg")) {
                    String[] logargs = new Login().login(cmd.equals("/reg"));
                    infoPacket.setLogin(logargs[0]);
                    infoPacket.setPassword(logargs[1]);
                }

                if (cmd.equals("update") && arg == null) {
                    System.err.println("Usage: update {arg}");
                    continue;
                }

                if (cmd.equals("add") || cmd.equals("update")) {
                    Flat flat = new Add().create();
                    if (flat == null) {
                        System.out.print("Canceled.\n");
                        userprint();
                        continue;
                    }
                    infoPacket.setFlat(flat);
                }

                if (cmd.equals("execute_script")) {
                    if (arg == null) {
                        System.out.print("Usage: execute_script: {file_name}");
                        userprint();
                        continue;
                    } else {
                        JTread t = new JTread();
                        t.run(arg, datagramSocket, inetAddress);
                    }
                }

                oos.writeObject(infoPacket);
                oos.writeObject(new EofIndicatorClass());
                oos.flush();
                oos.close();

                buffer = baos.toByteArray();
                DatagramPacket cmdPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);

                datagramSocket.send(cmdPacket);

                if (cmd.equals("exit")) {
                    System.out.println("Closing client application, bye!");
                    System.exit(0);
                }

                DatagramPacket respPacket = new DatagramPacket(new byte[3000], 3000, inetAddress, port);

                try {
                    datagramSocket.receive(respPacket);

                } catch (SocketTimeoutException e) {
                    System.err.println("Took too long for server to respond; The server might be experiencing issues or downtime. Try again later.\nServer: " + host);
                    continue;
                }

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(respPacket.getData()));
                InfoPacket inf = (InfoPacket) ois.readObject();

                if (inf.getCmd() != null && inf.getCmd().equals("/login") || inf.getCmd().equals("/reg")) {
                    System.out.println(inf.getArg());
                    login = inf.getLogin();
                    pass = inf.getPassword();
                } else {
                    System.out.println(inf.getCmd());
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            userprint();
        }
    }

}