import javafx.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.CharBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class UDPServer extends Thread {
    private List<Integer> portSequence;
    private Map<Integer, DatagramSocket> sockets;
    private byte[] buf = new byte[256];
    private String testString;

    public UDPServer(int n, int[] ports) throws SocketException {
        sockets = new HashMap<>();
        portSequence = new ArrayList<>();
        StringBuilder testString = new StringBuilder();
        portSequence.addAll(Arrays.stream(ports).boxed().collect(Collectors.toList()));
        ports = Arrays.stream(ports).distinct().toArray();
        for (int port : ports) {
            sockets.put(port, new DatagramSocket(port)); //opening UDP ports
        }
        for (int port : portSequence) {
            testString.append(port).append(","); //generating string to log in client
        }
        this.testString = testString.toString();
    }

    public void run(){
        while (true){
            Pair<InetAddress, Integer> client = verify();
            if(client == null) continue;
            int port = new Random().nextInt(65535 - 1024) + 1024;
            buf = Integer.toString(port).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, client.getKey(), client.getValue());
            try {
                sockets.get(portSequence.get(portSequence.size()-1)).send(packet);
                System.out.println("Client " + client.getKey() + ":" + client.getValue() + " is authorized." +
                        " Initiating sending protocols...");
                openAndSend(client, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openAndSend(Pair<InetAddress, Integer> client, int port) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(buf,buf.length);
            File file = new File("tes\\test.java");
            System.out.println("Waiting for confirmation to accept file");
            socket.receive(packet);
            System.out.println("Get confirmation to accept file");
            if(packet.getAddress().equals(client.getKey()) && packet.getPort() == client.getValue()) {
                buf = file.getName().getBytes();
                packet = new DatagramPacket(buf, buf.length, client.getKey(), client.getValue());
                socket.send(packet);
                System.out.println("Filename was send to " + client.getKey() + ":" + client.getValue());

                buf = Long.toString(file.length()).getBytes();
                packet = new DatagramPacket(buf, buf.length, client.getKey(), client.getValue());
                socket.send(packet);
                System.out.println("File size was send to " + client.getKey() + ":" + client.getValue());

                FileReader reader = new FileReader(file);
                char[] buffer = new char[(int) file.length()];
                int res = reader.read(buffer);
                for (int i = 0; i <= (res/256); i++) {
                    buf = new byte[256];
                    System.out.println("Sending " + i + "/" + res/256 + " packet");
                    for (int j = 0; j < 256; j++) {
                        int index = (i*256)+j;
                        if(index >= res) break;
                        buf[j] = (byte) buffer[index];
                    }
                    packet = new DatagramPacket(buf, buf.length, client.getKey(), client.getValue());
                    socket.send(packet);
                    System.out.println("Packet "+ i + "/" + res/256 + " was send");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pair<InetAddress, Integer> verify(){
        List<Pair<String, String>> verifyingMap = new ArrayList<>();
        Pair<InetAddress, Integer> client = null;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        for (int i = 0; i < portSequence.size(); i++) {
            try {
                System.out.println("Waiting for authorization packet: " + (i+1) + "/" + portSequence.size());
                sockets.get(portSequence.get(i)).receive(packet);
                System.out.println("Get authorization packet: " + (i+1) + "/" + portSequence.size());
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                if(i+1 != portSequence.size()){
                    sockets.get(portSequence.get(i)).send(packet);
                }
                verifyingMap.add(new Pair<>(portSequence.get(i) + ",", address.toString() + ":" + port));
                client = new Pair<>(address, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(client == null) return null;
        Pair<InetAddress, Integer> tmpClient = client;
        if(verifyingMap.stream().filter(pair -> (tmpClient.getKey().toString()+ ":" + tmpClient.getValue())
                .equals(pair.getValue())).count() == portSequence.size()) { //checking if all IPs are equals
            StringBuilder portOrder = new StringBuilder();
            verifyingMap.forEach(pair -> portOrder.append(pair.getKey()));
            if(testString.equals(portOrder.toString())){
                return client;
            }
        }
        return null;
    }
}
