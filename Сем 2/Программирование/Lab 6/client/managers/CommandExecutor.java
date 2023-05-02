package managers;

import collections.Flat;
import commands.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public void run(InputStream input) throws IOException {

        ByteArrayOutputStream baosc = new ByteArrayOutputStream();
        ObjectOutputStream oosc = new ObjectOutputStream(baosc);
        InfoPacket connectmePacket = new InfoPacket("connectme", null);
        oosc.writeObject(connectmePacket);
        oosc.writeObject(new EofIndicatorClass());
        oosc.flush();
        oosc.close();
        buffer = baosc.toByteArray();
        datagramSocket.send(new DatagramPacket(buffer, buffer.length, inetAddress, 1234));


        System.out.print(">>> ");
        Scanner cmdScanner = new Scanner(input);

        while(true) {
            String line = cmdScanner.nextLine().trim();
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
                System.out.println("Неизевстная комманда");
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

                System.out.println(infoPacket);

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

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                DatagramPacket respPacket = new DatagramPacket(new byte[10000], 10000, inetAddress, 1234);
                executorService.execute(() -> {
                    try {
                        datagramSocket.receive(respPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                        System.err.println("Took too long for server to respond; the server might be experiencing issues or downtime. Try again later.");
                        System.exit(0);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(respPacket.getData()));
                InfoPacket inf = (InfoPacket) ois.readObject();
                System.out.println("Got response:\n\n" + inf.getCmd());


            } catch (IOException e ){
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            System.out.print(">>> ");
        }
    }

    private DatagramPacket getResponse() throws IOException {
        DatagramPacket respPacket = new DatagramPacket(new byte[10000], 10000, inetAddress, 1234);
        datagramSocket.receive(respPacket);

        return respPacket;
    }

}