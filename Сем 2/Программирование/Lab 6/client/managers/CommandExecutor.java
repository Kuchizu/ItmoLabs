package managers;

import collections.Flat;
import commands.*;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class EofIndicatorClass implements Serializable{}

/**
 * CommandExecutor for handling CLI commands
 */
public class CommandExecutor {

    private DatagramSocket datagramSocket = new DatagramSocket();
    private InetAddress inetAddress = InetAddress.getByName("localhost");
    private byte[] buffer = new byte[10000];
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
    }

    public static Map<String, Command> getCommands(){
        return commands;
    }

    /**
     * @param input Input stream commands
     */
    public void run(InputStream input){
        System.out.print(">>> ");
        Scanner cmdScanner = new Scanner(input);

        while(cmdScanner.hasNext()) {
            String line = cmdScanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String cmd = line.split(" ")[0];

            String arg = null;
            if (line.split(" ").length == 2){
                arg = line.split(" ")[1];
            }

            if (commands.containsKey(cmd)) {
                System.out.println(commands.get(cmd));

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
                            System.out.println("Exited.");
                            continue;
                        }
                        System.out.println(flat);
                        infoPacket.setFlat(flat);
                    }

                    System.out.println(infoPacket);
                    System.out.println(infoPacket.getFlat());

                    oos.writeObject(infoPacket);
                    oos.writeObject(new EofIndicatorClass());
                    oos.flush();
                    oos.close();

                    buffer = baos.toByteArray();
                    DatagramPacket cmdPacket = new DatagramPacket(buffer, buffer.length, inetAddress, 1234);

                    datagramSocket.send(cmdPacket);

                    if(cmd.equals("exit")){
                        System.out.println("Closing client application, bye!");
                        System.exit(0);
                    }

                    DatagramPacket respPacket = new DatagramPacket(new byte[10000], 10000, inetAddress, 1234);
                    datagramSocket.receive(respPacket);

                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(respPacket.getData()));
                    InfoPacket inf = (InfoPacket) ois.readObject();
                    System.out.println("Got from server:\n\n" + inf.toString());


                } catch (IOException e ){
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
            else{
                System.err.println("Неизевстная комманда");
            }
            System.out.print(">>> ");
        }
    }

}