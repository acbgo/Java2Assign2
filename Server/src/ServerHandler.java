import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends Thread {

    public Socket socket;
    public ConcurrentHashMap<String, Socket> clients;
    public ConcurrentHashMap<String, String> matches;
    public ConcurrentHashMap<DataInputStream, PrintStream> input_print;
    public DataInputStream dataInputStream;
    public PrintStream printStream;

    public String name;
    public String op_name = "";

    public int my_number;
    public int op_number;

    public boolean can_match = false;

    int[][] chessBoard = new int[3][3];
    int winner = -1;
    boolean game_end = false;

    public ServerHandler(Socket socket, ConcurrentHashMap<String, Socket> clients, ConcurrentHashMap<String, String> matches, ConcurrentHashMap<DataInputStream, PrintStream> input_print) {
        if (socket != null) {
            System.out.println("new client connected");
            try {
                this.socket = socket;
                this.clients = clients;
                this.matches = matches;
                this.input_print = input_print;
                dataInputStream = new DataInputStream(socket.getInputStream());
                printStream = new PrintStream(socket.getOutputStream());
                input_print.put(dataInputStream, printStream);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void auto_match() throws IOException {
        for (Map.Entry<String, String> socketStringEntry : matches.entrySet()) {
            Map.Entry entry = (Map.Entry) socketStringEntry;
            if (entry.getValue().equals("")) {
                String match_name = (String)entry.getKey();
                if (name.equals(match_name)){
                    continue;
                }
                op_name = (String) entry.getKey();
                matches.put(op_name, name);
                matches.put(name, op_name);
                can_match = true;
                Socket op_socket = clients.get(op_name);
                PrintStream op_ps = new PrintStream(op_socket.getOutputStream());
                op_ps.println("op_name:" + name);
            }
        }
    }
    public void send_match_msg() throws IOException {
        String match_to = matches.get(name);
        Socket match_socket = clients.get(match_to);
        PrintStream match_ps = new PrintStream(match_socket.getOutputStream());
        match_ps.println("match to " + name);
        printStream.println("match to " + match_to);
    }

    public void change_board(String data) throws IOException {
        if (data.startsWith("last")){
            int x = Integer.parseInt(data.substring(5,6));
            int y = Integer.parseInt(data.substring(7,8));
            chessBoard[y][x] = op_number;
        } else {
            String opponent = matches.get(name);
            Socket op_socket = clients.get(opponent);
            PrintStream op_print = new PrintStream(op_socket.getOutputStream());
            String msg = "position:" + data.substring(9,12) + "1";
            int x = Integer.parseInt(data.substring(9,10));
            int y = Integer.parseInt(data.substring(11,12));
            chessBoard[y][x] = my_number;
            op_print.println(msg);
            printStream.println("1");
        }
    }
    
    public void send_winner(){
        if (winner == 0){
            System.out.println("game tie!");
            printStream.println("game tie!");
        } else if (winner == my_number){
            System.out.println(name + " win!");
            printStream.println("you win!");
        } else if (winner == op_number) {
            printStream.println("you lose...");
        }
    }

    public int check_winner(){
        //first col
        if (check_triple_equality(chessBoard[0][0],chessBoard[0][1],chessBoard[0][2])){
            return chessBoard[0][0];
        }
        //second col
        if (check_triple_equality(chessBoard[1][0],chessBoard[1][1],chessBoard[1][2])){
            return chessBoard[1][0];
        }
        //third col
        if (check_triple_equality(chessBoard[2][0],chessBoard[2][1],chessBoard[2][2])){
            return chessBoard[2][0];
        }

        //first row
        if (check_triple_equality(chessBoard[0][0],chessBoard[1][0],chessBoard[2][0])){
            return chessBoard[0][0];
        }
        //second row
        if (check_triple_equality(chessBoard[0][1],chessBoard[1][1],chessBoard[2][1])){
            return chessBoard[0][1];
        }
        //third row
        if (check_triple_equality(chessBoard[0][2],chessBoard[1][2],chessBoard[2][2])){
            return chessBoard[0][2];
        }

        //main diagonal
        if (check_triple_equality(chessBoard[0][0],chessBoard[1][1],chessBoard[2][2])){
            return chessBoard[0][2];
        }
        if (check_triple_equality(chessBoard[2][0],chessBoard[1][1],chessBoard[0][2])){
            return chessBoard[0][2];
        }
        if (is_end() && winner == -1){
            return 0;
        }
        return -1;
    }

    private boolean check_triple_equality(int p1, int p2, int p3){
        return (p1 == p2) && (p2 == p3) && (p1 != 0);
    }

    public boolean is_end(){
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (chessBoard[i][j] == 0){
                    return false;
                }
            }
        }
        return true;
    }

    public void print_board(){
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                System.out.print(chessBoard[i][j] + ",");
            }
            System.out.println();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String data = dataInputStream.readLine();
                System.out.println("receive:" + data);
                if (data.startsWith("name")) {
                    name = data.substring(6);
                    System.out.println("connect to " + name);
                    if (clients.size() == 0) {
                        matches.put(name, "");
                    }
                    clients.put(name, this.socket);
                    if (clients.size()%2 != 0){
                        printStream.println("your turn");
                        my_number = 2;
                        op_number = 1;
                    } else {
                        printStream.println("0");
                        my_number = 1;
                        op_number = 2;
                    }
                    if (clients.size()>1){
                        auto_match();
                        if (can_match){
                            send_match_msg();
                        } else {
                            matches.put(name, "");
                        }
                    } else {
                        System.out.println("waiting....");
                        printStream.println("waiting....");
                    }
                } else if (data.startsWith("position:") && !game_end) {
                    if (clients.size() == 1) {
                        System.out.println("waiting....");
                        printStream.println("waiting....");
                    } else if (data.endsWith("0")) {
                        String opponent = matches.get(name);
                        System.out.println("this is " + opponent + "'s turn");
                        printStream.println("0");
                    } else {
                        change_board(data);
                        print_board();
                        winner = check_winner();
                        System.out.println(winner);
                        if (winner != -1){
                            game_end = true;
                            send_winner();
                        }
                    }
                } else if (data.startsWith("last") && !game_end) {
                    change_board(data);
                    winner = check_winner();
                    System.out.println(winner);
                    if (winner != -1){
                        game_end = true;
                        send_winner();
                    }
                } else if (data.startsWith("op_name:")) {
                    op_name = data.substring(8);
                } else if (data.equals("op_shutdown")) {
                    op_name = "";
                } else if (game_end) {
                    System.out.println("The game is over");
                    printStream.println("The game is over.");
                }
            }
        } catch (SocketException socketException){
            System.out.println("client" + name + "shutdown");
            String msg = "Your opponent dropped out unexpectedly";
            clients.remove(name);
            matches.remove(name);
            if (!op_name.equals("")){
                matches.put(op_name,"");
            }
            if (!op_name.equals("")){
                Socket op_socket = clients.get(op_name);
                System.out.println(op_name);
                PrintStream op_ps = null;
                try {
                    op_ps = new PrintStream(op_socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                op_ps.println(msg);
                op_ps.println("op_shutdown");
                System.out.println(matches);
            } else {
                System.out.println(name + " has not been matched");
                System.out.println(matches);
            }
        } catch (Exception e) {
            System.out.println("--------------------------------");
            throw new RuntimeException(e);
        }
    }
}
