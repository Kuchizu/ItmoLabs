package managers;

import commands.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

class EofIndicatorClass implements Serializable{

}

/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private static final DatagramSocket datagramSocket;

    static {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(3000);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static InetAddress inetAddress;
    private static String host;
    private static int port;
    private static String login;
    private static String pass;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        CommandExecutor.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        CommandExecutor.pass = pass;
    }

    private static byte[] buffer;
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

    public static Map<String, Command> getCommands(){
        return commands;
    }

    public CommandExecutor() throws IOException {
        host = "localhost";
        port = 1234;
        inetAddress = InetAddress.getByName(host);

        System.out.printf("Client started running on [%s][%s]\n\n", host, port);

        ByteArrayOutputStream baosc = new ByteArrayOutputStream();
        ObjectOutputStream oosc = new ObjectOutputStream(baosc);
        InfoPacket connectmePacket = new InfoPacket("connectme", null);
        oosc.writeObject(connectmePacket);
        oosc.writeObject(new EofIndicatorClass());
        oosc.flush();
        oosc.close();

        buffer = baosc.toByteArray();

        datagramSocket.send(new DatagramPacket(buffer, buffer.length, inetAddress, port));
    }

    public CommandExecutor(String[] args) throws SocketException, UnknownHostException {
        host = args[0];
        port = Integer.parseInt(args[1]);
        inetAddress = InetAddress.getByName(host);
    }

    public InfoPacket request(InfoPacket inf) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            inf.setLogin(login);
            inf.setPassword(pass);

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

            oos.writeObject(inf);
            oos.writeObject(new EofIndicatorClass());
            oos.flush();
            oos.close();

            buffer = baos.toByteArray();
            DatagramPacket cmdPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);

            datagramSocket.send(cmdPacket);
            System.out.println("Sent");

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

            return (InfoPacket) ois.readObject();

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return new InfoPacket("Critical Error", e.getMessage());
        }
    }

//    /**
//     * @param input Input stream commands
//     */
//    public void run(InputStream input) throws IOException {
//
//        System.setOut(new LogPrinter(System.out));
//
//
//        userprint();
//        Scanner cmdScanner = new Scanner(input);
//
//        while (true) {
//            String line = cmdScanner.nextLine().trim();
//            LOGGER.info(line);
//
//            if (line.isEmpty()) {
//                userprint();
//                continue;
//            }
//
//            String cmd = line.split(" ")[0];
//            String arg = null;
//            if (line.split(" ").length == 2) {
//                arg = line.split(" ")[1];
//            }
//
//            if (!(commands.containsKey(cmd))) {
//                System.out.println("Неизвестная команда");
//                userprint();
//                continue;
//            }
//            if (login == null && !cmd.equals("/login") && !cmd.equals("/reg")) {
//                System.out.println("Для использования команд войдите в систему (/login) или зарегистрируйтесь (/reg)");
//                userprint();
//                continue;
//            }
//
//            userprint();
//        }
//    }

}