package managers;

import collections.Flat;
import commands.*;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

class EofIndicatorClass implements Serializable{}

/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private DatagramSocket datagramSocket = new DatagramSocket();
    private static String host = null;
    private static int port;
    private InetAddress inetAddress;
    private static final Logger LOGGER = Logger.getLogger(CommandExecutor.class.getName());
    private byte[] buffer = new byte[2048];
    private static final Map<String, Command> commands = new HashMap<>(){
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

    public static Map<String, Command> getCommands(){
        return commands;
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

        System.out.print(">>> ");
        Scanner cmdScanner = new Scanner(input);

        while(true) {
            String line = cmdScanner.nextLine().trim();
            LOGGER.info(line);

            if (line.isEmpty()){
                System.out.print(">>> ");
                continue;
            }

            String cmd = line.split(" ")[0];
            String arg = null;
            if (line.split(" ").length == 2){
                arg = line.split(" ")[1];
            }

            if(!(commands.containsKey(cmd))){
                System.out.println("Неизвестная команда");
                System.out.print(">>> ");
                continue;
            }
            try{
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);

                InfoPacket infoPacket = new InfoPacket(cmd, arg);

                if(cmd.equals("update") && arg == null){
                    System.err.println("Usage: update {arg}");
                    continue;
                }
                if(cmd.equals("add") || cmd.equals("update")){
                    Flat flat = new Add().create();
                    if(flat == null){
                        System.out.print("Canceled.\n>>> ");
                        continue;
                    }
                    System.out.println(flat);
                    infoPacket.setFlat(flat);
                }

                if(cmd.equals("execute_script")){
                    if(arg == null){
                        System.out.print("Usage: execute_script: {file_name}\n>>> ");
                        continue;
                    }
                    else{
                        try {
                            infoPacket.setArg(Files.readString(Paths.get(arg)));
                            System.out.println(Files.readString(Paths.get(arg)));
                        }
                        catch (NoSuchFileException e){
                            System.out.print("Файл не существует.\n>>> ");
                            continue;
                        }
                        catch (AccessDeniedException e) {
                            System.out.print("Ошибка чтения файла.\n>>> ");
                            continue;
                        }
                    }
                }

                oos.writeObject(infoPacket);
                oos.writeObject(new EofIndicatorClass());
                oos.flush();
                oos.close();

                buffer = baos.toByteArray();
                DatagramPacket cmdPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);

                datagramSocket.send(cmdPacket);

                if(cmd.equals("exit")){
                    System.out.println("Closing client application, bye!");
                    System.exit(0);
                }

                DatagramPacket respPacket = new DatagramPacket(new byte[2048], 2048, inetAddress, port);

                try {
                    datagramSocket.receive(respPacket);

                } catch (SocketTimeoutException e){
                    System.err.println("Took too long for server to respond; The server might be experiencing issues or downtime. Try again later.\nServer: " + host);
                    continue;
                }

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(respPacket.getData()));
                InfoPacket inf = (InfoPacket) ois.readObject();
                System.out.println("\n" + inf.getCmd());


            } catch (IOException e ){
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            System.out.print(">>> ");
        }
    }

}