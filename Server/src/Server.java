import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public ServerSocket myServerSocket;
    public ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> matches = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DataInputStream, PrintStream> input_print = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Boolean> waiting = new ConcurrentHashMap<>();
    public Server() {
        startConnection();
    }

    public void startConnection() {
        try {
            myServerSocket = new ServerSocket(8088);
            System.out.println("server started: <http://127.0.0.1:8088>\n");
            new Thread(()->{
                try {
                    while (true){
                        Socket socket = myServerSocket.accept();
                        new ServerHandler(socket, clients,matches,input_print,waiting);
                        Thread.sleep(500);
                        System.out.println(clients);
                        System.out.println(matches);
                        System.out.println();
                    }
                } catch (Exception e){
                    try {
                        myServerSocket.close();
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
