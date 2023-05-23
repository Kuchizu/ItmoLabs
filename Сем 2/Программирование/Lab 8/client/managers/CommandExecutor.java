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

    private static final DatagramSocket datagramSocket;

    static {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static String host = null;
    private static String login;
    private static String pass;
    private static byte[] buffer;
    private static int port;
    private static InetAddress inetAddress = 1234;
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
        inetAddress = InetAddress.getByName(host);
    }

    public CommandExecutor(String[] args) throws SocketException, UnknownHostException {
        host = args[0];
        port = Integer.parseInt(args[1]);
        inetAddress = InetAddress.getByName(host);
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

    public static InfoPacket request(InfoPacket inf) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        InfoPacket infoPacket = new InfoPacket(inf.getCmd(), inf.getArg());
        infoPacket.setLogin(login);
        infoPacket.setPassword(pass);

        if (inf.getCmd().equals("/login") || inf.getCmd().equals("/reg")) {
            infoPacket.setLogin(inf.getLogin());
            infoPacket.setPassword(inf.getPassword());
        }

        if (inf.getCmd().equals("add") || inf.getCmd().equals("update")) {
            infoPacket.setFlat(inf.getFlat());
        }

//        if (cmd.equals("execute_script")) { // TODO
//            if (arg == null) {
//                System.out.print("Usage: execute_script: {file_name}");
//                userprint();
//                continue;
//            } else {
//                JTread t = new JTread();
//                t.run(arg, datagramSocket, inetAddress);
//            }
//        }

        oos.writeObject(infoPacket);
        oos.writeObject(new EofIndicatorClass());
        oos.flush();
        oos.close();

        buffer = baos.toByteArray();
        DatagramPacket cmdPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);

        datagramSocket.send(cmdPacket);

//        if (inf.getCmd().equals("exit")) { # TODO
//            System.out.println("Closing client application, bye!");
//            System.exit(0);
//        }

        DatagramPacket respPacket = new DatagramPacket(new byte[3000], 3000, inetAddress, port);

        try {
            datagramSocket.receive(respPacket);

        } catch (SocketTimeoutException e) {
            return new InfoPacket("Err", "\"Took too long for server to respond; The server might be experiencing issues or downtime. Try again later.\\nServer: \" + host");
        }

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(respPacket.getData()));
        InfoPacket resinf = (InfoPacket) ois.readObject();

        if (resinf.getCmd() != null && resinf.getCmd().equals("/login") || resinf.getCmd().equals("/reg")) {
            System.out.println(inf.getArg());
            login = inf.getLogin();
            pass = inf.getPassword();
            return resinf;

        } else {
            return new InfoPacket(inf.getCmd(), null);
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

            userprint();
        }
    }

}