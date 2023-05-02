import java.io.IOException;
import java.io.Serial;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Testserver {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[256];

    public Testserver(DatagramSocket datagramSocket){
        this.datagramSocket = datagramSocket;
    }

    public void receive() {
        while (true){
            try {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                InetAddress inetAddress = datagramPacket.getAddress();
                int port = datagramPacket.getPort();
                String clientData = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                System.out.println("Got " + clientData);
                datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                datagramSocket.send(datagramPacket);
            } catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket(1234);
        Testserver server = new Testserver(datagramSocket);
        server.receive();
    }

}
