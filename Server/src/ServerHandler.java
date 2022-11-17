import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler implements Runnable {

    public Socket socket;
    //    public HashMap hashMap;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public ServerHandler(Socket socket, HashMap hashMap) {
        if (socket != null) {
            System.out.println("client connected");
            try {
                this.socket = socket;
//                this.hashMap = hashMap;
                dataInputStream = new DataInputStream(socket.getInputStream());
                printStream = new PrintStream(socket.getOutputStream());
                run();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void run() {
        try {
            String data = dataInputStream.readLine();
            System.out.println("data:" + data);
            if (data.startsWith("name")) {
                System.out.println("connect to " + data.substring(6));
                printStream.println("connect to " + data.substring(6));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
