import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler implements Runnable {

    public Socket socket;
    public ConcurrentHashMap<String, Socket> clients;
    ConcurrentHashMap<String, String> matches;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public ServerHandler(Socket socket, ConcurrentHashMap<String, Socket> clients, ConcurrentHashMap<String, String> matches) {
        if (socket != null) {
            System.out.println("new client connected");
            try {
                this.socket = socket;
                this.clients = clients;
                this.matches = matches;
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
            String name = "";
            if (data.startsWith("name")) {
                name = data.substring(6);
                System.out.println("connect to " + name);
                printStream.println("connect to " + name);
                if (clients.size() == 0){
                    matches.put(name, "");
                }
                clients.put(name, this.socket);
            }

            if (clients.size() == 1){
                System.out.println("waiting....");
                printStream.println("waiting....");
            }
            else {
                for (Map.Entry<String, String> socketStringEntry : matches.entrySet()) {
                    Map.Entry entry = (Map.Entry) socketStringEntry;
                    if (entry.getValue().equals("")){
                        matches.put((String) entry.getKey(), name);
                        matches.put(name, (String) entry.getKey());
                    }
                }
                String match_to = matches.get(name);
                Socket match_socket = clients.get(match_to);
                PrintStream match_ps = new PrintStream(match_socket.getOutputStream());
                match_ps.println("match to " + name);
                printStream.println("match to " + match_to);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
