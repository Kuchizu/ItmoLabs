package managers;

import commands.InfoPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static managers.CommandExecutor.datagramSocket;

public class UpdateSender {
    private static List<InetSocketAddress> connectedUsers = new ArrayList<>();

    static public void sendUpdate(InfoPacket upd) throws IOException {
        for (InetSocketAddress client: connectedUsers) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            InfoPacket infoPacket = new InfoPacket(null, null);
            oos.writeObject(infoPacket);
            oos.flush();
            oos.close();


            byte[] buffer = baos.toByteArray();
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, client.getAddress(), client.getPort());

            datagramSocket.send(responsePacket);
        }
    }

}
