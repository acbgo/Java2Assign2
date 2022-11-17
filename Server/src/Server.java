import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public ServerSocket myServerSocket;
    public HashMap hashMap;
    public Server() {
        startConnection();
    }

    public void startConnection() {
        try {
            myServerSocket = new ServerSocket(8088);
            System.out.println("server started: <http://127.0.0.1:8088>\n");

            
            new Thread(() -> {
                try {
                    while (true){
                        Socket socket = myServerSocket.accept();
                        System.out.println(socket.getPort());
                        new ServerHandler(socket, hashMap);
                    }
                } catch (Exception e){
                    try {
                        myServerSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
