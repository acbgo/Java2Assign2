import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    public Socket socket;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public Client(){
        StartConnection();
    }

    public void StartConnection(){
        try {
            socket = new Socket("127.0.0.1", 8088);
            dataInputStream = new DataInputStream(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            System.out.println("send: name: mark");
            printStream.println("name: mark");
            receiveGameThread();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void receiveGameThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("client start");
                    while (true){
                        String data =dataInputStream.readLine();
                        if (data != null){
                            System.out.println(data);
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
