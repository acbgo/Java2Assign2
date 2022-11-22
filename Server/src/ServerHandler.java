import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends Thread {

    public Socket socket;
    public ConcurrentHashMap<String, Socket> clients;
    public ConcurrentHashMap<String, String> matches;
    public ConcurrentHashMap<DataInputStream, PrintStream> input_print;
    ConcurrentHashMap<String, Boolean> waiting;
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
    boolean have_restart = false;

    int my_turn = 0;

    Statement statement;
    Connection con;

    {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tic_tac_toe?characterEncoding=UTF8&autoReconnect=true&useSSL=false");
            statement = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ServerHandler(Socket socket, ConcurrentHashMap<String, Socket> clients, ConcurrentHashMap<String, String> matches, ConcurrentHashMap<DataInputStream, PrintStream> input_print, ConcurrentHashMap<String, Boolean> waiting) {
        if (socket != null) {
            System.out.println("new client connected");
            try {
                this.socket = socket;
                this.clients = clients;
                this.matches = matches;
                this.input_print = input_print;
                this.waiting = waiting;
                dataInputStream = new DataInputStream(socket.getInputStream());
                printStream = new PrintStream(socket.getOutputStream());
                input_print.put(dataInputStream, printStream);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void match() throws IOException {
        matches.put(op_name, name);
        matches.put(name, op_name);
        can_match = true;
        Socket op_socket = clients.get(op_name);
        System.out.println(op_name);
        PrintStream op_ps = new PrintStream(op_socket.getOutputStream());
        op_ps.println("op_name:" + name);
    }

    public void send_match_msg() throws IOException {
        String match_to = matches.get(name);
        Socket match_socket = clients.get(match_to);
        PrintStream match_ps = new PrintStream(match_socket.getOutputStream());
        match_ps.println("match to " + name);
        printStream.println("match to " + match_to);
    }

    public void change_board(String data) throws IOException {
        if (have_restart) {
            have_restart = false;
        }
        if (data.startsWith("last")) {
            int x = Integer.parseInt(data.substring(5, 6));
            int y = Integer.parseInt(data.substring(7, 8));
            chessBoard[y][x] = op_number;
        } else {
            String opponent = matches.get(name);
            Socket op_socket = clients.get(opponent);
            PrintStream op_print = new PrintStream(op_socket.getOutputStream());
            String msg = "position:" + data.substring(9, 12) + "1";
            int x = Integer.parseInt(data.substring(9, 10));
            int y = Integer.parseInt(data.substring(11, 12));
            chessBoard[y][x] = my_number;
            op_print.println(msg);
            printStream.println("1");
        }
    }

    public void send_winner() {
        if (winner == 0) {
            System.out.println("game tie!");
            printStream.println("game tie!");
        } else if (winner == my_number) {
            System.out.println(name + " win!");
            printStream.println("you win!");
        } else if (winner == op_number) {
            printStream.println("you lose...");
        }
    }

    public int check_winner() {
        //first col
        if (check_triple_equality(chessBoard[0][0], chessBoard[0][1], chessBoard[0][2])) {
            return chessBoard[0][0];
        }
        //second col
        if (check_triple_equality(chessBoard[1][0], chessBoard[1][1], chessBoard[1][2])) {
            return chessBoard[1][0];
        }
        //third col
        if (check_triple_equality(chessBoard[2][0], chessBoard[2][1], chessBoard[2][2])) {
            return chessBoard[2][0];
        }

        //first row
        if (check_triple_equality(chessBoard[0][0], chessBoard[1][0], chessBoard[2][0])) {
            return chessBoard[0][0];
        }
        //second row
        if (check_triple_equality(chessBoard[0][1], chessBoard[1][1], chessBoard[2][1])) {
            return chessBoard[0][1];
        }
        //third row
        if (check_triple_equality(chessBoard[0][2], chessBoard[1][2], chessBoard[2][2])) {
            return chessBoard[0][2];
        }

        //main diagonal
        if (check_triple_equality(chessBoard[0][0], chessBoard[1][1], chessBoard[2][2])) {
            return chessBoard[0][2];
        }
        if (check_triple_equality(chessBoard[2][0], chessBoard[1][1], chessBoard[0][2])) {
            return chessBoard[0][2];
        }
        if (is_end() && winner == -1) {
            return 0;
        }
        return -1;
    }

    private boolean check_triple_equality(int p1, int p2, int p3) {
        return (p1 == p2) && (p2 == p3) && (p1 != 0);
    }

    public boolean is_end() {
        for (int[] ints : chessBoard) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (ints[j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void print_board() {
        for (int[] ints : chessBoard) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                System.out.print(ints[j] + ",");
            }
            System.out.println();
        }
    }

    public void restart() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                chessBoard[i][j] = 0;
            }
        }
        winner = -1;
        game_end = false;
    }

    public int is_my_turn() {
        int my_chess = 0;
        int op_chess = 0;
        for (int[] ints : chessBoard) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (ints[j] == my_number) {
                    my_chess++;
                } else if (ints[j] == op_number) {
                    op_chess++;
                }
            }
        }
        if (my_chess > op_chess) {
            return 0;
        } else {
            return 1;
        }
    }

    public void inform_opponent(String msg) {
        if (!op_name.equals("")) {
            if (!msg.equals("op_restart")) {
                matches.put(op_name, "");
            }
            Socket op_socket = clients.get(op_name);
            System.out.println(op_name);
            PrintStream op_ps;
            try {
                op_ps = new PrintStream(op_socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            op_ps.println(msg);
            if (!msg.equals("op_restart")) {
                op_ps.println("op_shutdown");
            }
            System.out.println(matches);
        } else {
            System.out.println(name + " has not been matched");
            System.out.println(matches);
        }
    }

    public void update() {
        String msg = "list:";
        for (String s : clients.keySet()) {
            if (!s.equals(name) && waiting.get(s)) {
                msg += s + ",";
            }
        }
        if (msg.endsWith(",")) {
            msg = msg.substring(0, msg.length() - 1);
        }
        System.out.println(msg);
        printStream.println(msg);
    }

    public void update_others() throws IOException {
        for (String s : clients.keySet()) {
            if (!s.equals(name)) {
                Socket socket1 = clients.get(s);
                PrintStream printStream1 = new PrintStream(socket1.getOutputStream());
                printStream1.println("update list");
            }
        }
    }

    public String buildBoardStr() {
        String str = "";
        for (int[] ints : chessBoard) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                str += ints[j] + ",";
            }
        }
        if (str.endsWith(",")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
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
                    String sql = "select * from game_on where name = '" + name + "';";
                    ResultSet resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        op_name = resultSet.getString("op_name");
                        my_number = resultSet.getInt("my_num");
                        int my_turn = resultSet.getInt("my_turn");
                        String board = resultSet.getString("board");
                        System.out.println(name + " has an unfinished game, reconnecting...");
                        if (my_number == 1) {
                            op_number = 2;
                        } else if (my_number == 2) {
                            op_number = 1;
                        }
                        if (my_turn == 1) {
                            printStream.println("reconnect your turn");
                        } else if (my_turn == 0) {
                            printStream.println("reconnect not your turn");
                        }
                        printStream.println("reconnect my number" + my_number);
                        printStream.println("board:" + board);
                        matches.put(name, op_name);
                        matches.put(op_name, name);
                        clients.put(name, this.socket);
                        waiting.put(name, false);
                        Socket op_socket = clients.get(op_name);
                        PrintStream op_ps = new PrintStream(op_socket.getOutputStream());
                        op_ps.println("op_name:" + name);
                        sql = "delete from game_on where name = '" + name + "';";
                        statement.execute(sql);
                    } else {
                        matches.put(name, "");
                        clients.put(name, this.socket);
                        waiting.put(name, true);
                        update_others();
                        if (clients.size() % 2 != 0) {
                            printStream.println("your turn");
                            my_number = 2;
                            op_number = 1;
                        } else {
                            printStream.println("0");
                            my_number = 1;
                            op_number = 2;
                        }
                        if (clients.size() > 1) {
                            update();
                        } else {
                            System.out.println("waiting....");
                            printStream.println("waiting....");
                        }
                    }
                } else if (data.startsWith("board:")) {
                    String board = data.substring(6);
                    String[] boards = board.split(",");
                    for (int i = 0; i < chessBoard.length; i++) {
                        for (int j = 0; j < chessBoard[0].length; j++) {
                            int index = i * 3 + j;
                            chessBoard[i][j] = Integer.parseInt(boards[index]);
                        }
                    }
                    System.out.println("重新连接后的棋盘");
                    print_board();
                } else if (data.startsWith("position:") && !game_end && !op_name.equals("")) {
                    if (clients.size() == 1) {
                        System.out.println("waiting....");
                        printStream.println("waiting....");
                    } else if (data.endsWith("0")) {
                        String opponent = matches.get(name);
                        System.out.println("this is " + opponent + "'s turn");
                        printStream.println("0" + op_name);
                    } else {
                        change_board(data);
                        print_board();
                        winner = check_winner();
//                        System.out.println(winner);
                        if (winner != -1) {
                            game_end = true;
                            send_winner();
                        }
                    }
                } else if (data.startsWith("last") && !game_end) {
                    change_board(data);
                    winner = check_winner();
                    System.out.println(winner);
                    if (winner != -1) {
                        game_end = true;
                        send_winner();
                    }
                } else if (data.startsWith("op_name:")) {
                    op_name = data.substring(8);
                } else if (data.equals("op_shutdown")) {
                    op_name = "";
                } else if (data.equals("restart")) {
                    if (!have_restart) {
                        have_restart = true;
                        restart();
                        System.out.println(name);
                        print_board();
                        inform_opponent("op_restart");
                    }
                } else if (data.equals("op_restart")) {
                    have_restart = true;
                    restart();
                    System.out.println(name);
                    print_board();
                } else if (data.startsWith("match:") && !data.contains("null")) {
                    op_name = data.substring(6);
                    System.out.println("before match: " + op_name);
                    System.out.println(clients);
                    match();
                    if (can_match) {
                        waiting.put(name, false);
                        waiting.put(op_name, false);
                        send_match_msg();
                        can_match = false;
                        update_others();
//                        update();
                    } else {
                        matches.put(name, "");
                    }
                } else if (data.equals("update list")) {
                    update();
                } else if (game_end) {
                    System.out.println("The game is over");
                    printStream.println("The game is over.");
                }
            }
        } catch (SocketException socketException) {
            System.out.println("client" + name + "shutdown");
            String msg = "Your opponent dropped out unexpectedly";
            if (!game_end) {
                my_turn = is_my_turn();
                String board = buildBoardStr();
                String sql = "insert into game_on value ('" + name + "', '" + op_name + "', " + my_number + ", " + my_turn + ", '" + board + "');";
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            clients.remove(name);
            matches.remove(name);
            try {
                update_others();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inform_opponent(msg);
        } catch (Exception e) {
            System.out.println("--------------------------------");
            throw new RuntimeException(e);
        }
    }
}
