import java.net.SocketException;

public class ServerStart {
    public static void main(String[] args) {
        int[] ports = new int[args.length-1];
        for (int i = 1; i < args.length; i++) {
            ports[i-1] = Integer.parseInt(args[i]);
        }
        try {
            UDPServer server = new UDPServer(Integer.parseInt(args[0]), ports);
            server.start();

        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
