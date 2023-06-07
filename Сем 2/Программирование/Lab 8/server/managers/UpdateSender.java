package managers;

import commands.InfoPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashSet;

public class UpdateSender {
    public static HashSet<InetSocketAddress> connectedUsers = new HashSet<>();
    static DatagramSocket updateDatagramSocket;

    static {
        try {
            updateDatagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    static public void sendUpdate(InfoPacket upd) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(upd);
        oos.flush();
        oos.close();

        byte[] buffer = baos.toByteArray();
        for (InetSocketAddress client : connectedUsers) {
            DatagramPacket updatePacket = new DatagramPacket(buffer, buffer.length, client.getAddress(), client.getPort());
            updateDatagramSocket.send(updatePacket);
            System.out.println("Sent update to: " + client);
        }
    }
}
