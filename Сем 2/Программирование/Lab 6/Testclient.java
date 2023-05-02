import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Testclient {
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    private byte[] buffer;

    public Testclient(DatagramSocket datagramSocket, InetAddress inetAddress){
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
    }

    public void send(){
        Scanner scanner = new Scanner(System.in);
        while (true){
            try{
                String sendData = scanner.nextLine();
                buffer = sendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, 1234);
                datagramSocket.send(datagramPacket);
                datagramSocket.receive(datagramPacket);
                String recievedData = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                System.out.println("Got from server " + recievedData);
            } catch (IOException e ){
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        Testclient client = new Testclient(datagramSocket, inetAddress);
        System.out.println();
        client.send();
    }
}
