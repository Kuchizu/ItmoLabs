package managers;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import commands.InfoPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.ZonedDateTime;

public class ExecuteThread {
    private static byte[] buffer;
    static class JTread extends Thread{
        public void run(String arg, DatagramSocket datagramSocket, InetAddress inetAddress, int port, String login, String pass) throws IOException, InterruptedException {

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
}
