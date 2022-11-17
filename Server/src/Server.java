import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public ServerSocket myServerSocket;
    public ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> matches = new ConcurrentHashMap<>();
    public Server() {
        startConnection();
    }

    public void startConnection() {
        try {
            myServerSocket = new ServerSocket(8088);
            System.out.println("server started: <http://127.0.0.1:8088>\n");
            while (true){
                Socket socket = myServerSocket.accept();
                new ServerHandler(socket, clients,matches);
                System.out.println(clients);
                System.out.println(matches);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
