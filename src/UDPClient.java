import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class UDPClient  {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public UDPClient(String address) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
    }

    public String send(String msg, int port) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    private void getFile(int port) throws IOException {
        buf = new byte[256];
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String fname = new String(packet.getData(), 0, packet.getLength());
        socket.receive(packet);
        long length = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));
        System.out.println(fname + " " + length);
        StringBuilder fileStore = new StringBuilder();

        for (int i = 0; i <= length / 256; i++) {
            socket.receive(packet);
            fileStore.append(new String(packet.getData(), 0, packet.getLength()));
        }

        File file = new File(fname);
        FileWriter writer = new FileWriter(file);
        writer.write(fileStore.toString());
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        int port = 0;
        UDPClient client = new UDPClient(args[0]);
        for (int i = 1; i < args.length; i++) {
            try {
                port = Integer.parseInt(client.send("knock", Integer.parseInt(args[i])));
            } catch (Exception e){
                continue;
            }
            System.out.println("I'm in!");
        }
        client.getFile(port);
    }
}
