import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends Thread {

    public Socket socket;
    public ConcurrentHashMap<String, Socket> clients;
    ConcurrentHashMap<String, String> matches;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public boolean can_match = false;

    public ServerHandler(Socket socket, ConcurrentHashMap<String, Socket> clients, ConcurrentHashMap<String, String> matches) {
        if (socket != null) {
            System.out.println("new client connected");
            try {
                this.socket = socket;
                this.clients = clients;
                this.matches = matches;
                dataInputStream = new DataInputStream(socket.getInputStream());
                printStream = new PrintStream(socket.getOutputStream());
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String data = dataInputStream.readLine();
                System.out.println("receive:" + data);
                String name = "";
                if (data.startsWith("name")) {
                    name = data.substring(6);
                    System.out.println("connect to " + name);
                    if (clients.size() == 0) {
                        matches.put(name, "");
                    }
                    clients.put(name, this.socket);
                    if (clients.size()%2 != 0){
                        printStream.println("your turn");
                    }
                    if (clients.size()>1){
                        for (Map.Entry<String, String> socketStringEntry : matches.entrySet()) {
                            Map.Entry entry = (Map.Entry) socketStringEntry;
                            if (entry.getValue().equals("")) {
                                String match_name = (String)entry.getKey();
                                if (name.equals(match_name)){
                                    continue;
                                }
                                matches.put((String) entry.getKey(), name);
                                matches.put(name, (String) entry.getKey());
                                can_match = true;
                            }
                        }
                        if (can_match){
                            String match_to = matches.get(name);
                            Socket match_socket = clients.get(match_to);
                            PrintStream match_ps = new PrintStream(match_socket.getOutputStream());
                            match_ps.println("match to " + name);
                            printStream.println("match to " + match_to);
                        } else {
                            matches.put(name, "");
                        }
                    } else {
                        System.out.println("waiting....");
                        printStream.println("waiting....");
                    }
                } else if (data.startsWith("position:")) {
                    if (clients.size() == 1) {
                        System.out.println("waiting....");
                        printStream.println("waiting....");
                    } else if (data.endsWith("0")) {
                        System.out.println("this is not his turn");
                        printStream.println("0");
                    } else {
                        String cur_player = data.substring(15, data.length()-1);
                        String opponent = matches.get(cur_player);
                        Socket op_socket = clients.get(opponent);
                        PrintStream op_print = new PrintStream(op_socket.getOutputStream());
                        String msg = "position:" + data.substring(9,12) + "1";
                        op_print.println(msg);
                        printStream.println("1");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
