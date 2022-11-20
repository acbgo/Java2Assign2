import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

    public Socket socket;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public String name;


    private Controller controller;

    public Client(Controller controller) {
        this.controller = controller;
        name = controller.player_name;
        StartConnection();
    }

    public void StartConnection() {
        try {
            socket = new Socket("127.0.0.1", 8088);
            dataInputStream = new DataInputStream(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            System.out.println("name: " + name);
            printStream.println("name: " + name);
            controller.setSocket(socket);
            controller.setPlayer_name(name);
            controller.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
