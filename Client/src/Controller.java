import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;//circle
    private static final int PLAY_2 = 2;//line
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    int which_player = 0;
    public boolean my_turn = false;

    private Socket socket;
    public static DataInputStream dataInputStream;
    public PrintStream printStream;

    public String player_name;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    @FXML
    private Button button;

    //ture -> circle; false -> line
    private static boolean TURN = false;
    public boolean op_click = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    public ArrayList<Node> nodes = new ArrayList<>();

    public int x_axis;
    public int y_axis;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(event -> {
            x_axis = (int) (event.getX() / BOUND);
            y_axis = (int) (event.getY() / BOUND);
            String msg = "position:" + x_axis + "," + y_axis + "by " + player_name + (my_turn ? 1 : 0);
            printStream.println(msg);
        });
        button.setOnMouseClicked(mouseEvent -> {
            restart();
        });
    }

    public void restart(){
        TURN = !TURN;
        System.out.println("click restart");
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                chessBoard[i][j] = EMPTY;
                flag[i][j] = false;
            }
        }
        for (Node node : nodes){
            base_square.getChildren().remove(node);
        }
        if (which_player == 2){
            my_turn = true;
        }
        if (!op_click){
            printStream.println("restart");
        } else {
            printStream.println("op_restart");
            op_click = false;
        }
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            drawChess();
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        nodes.add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        nodes.add(line_a);
        nodes.add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }


    public void setSocket(Socket s) throws IOException {
        socket = s;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    public void setPlayer_name(String n) {
        player_name = n;
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    String data = dataInputStream.readLine();
                    if (data.equals("1")) {
                        Platform.runLater(() -> {
                            if (my_turn) {
                                refreshBoard(x_axis, y_axis);
                                TURN = !TURN;
                                my_turn = false;
                            } else {
                                System.out.println("this not your turn");
                            }
                        });
                    } else if (data.equals("0")) {
                        System.out.println("Wait for your opponent to play");
                    } else if (data.startsWith("position:")) {
                        int x = Integer.parseInt(data.substring(9, 10));
                        int y = Integer.parseInt(data.substring(11, 12));
                        if (data.endsWith("1")) {
                            my_turn = true;
                            Platform.runLater(() -> {
                                refreshBoard(x, y);
                                TURN = !TURN;
                            });
                            String msg = "last:" + x + "," + y;
                            printStream.println(msg);
                        }
                    } else if (data.startsWith("your")) {
                        my_turn = true;
                        TURN = false;
                        which_player = 2;
                    } else if (data.startsWith("op_name:")) {
                        printStream.println(data);
                    } else if (data.equals("op_shutdown")) {
                        printStream.println(data);
                    } else if (data.equals("op_restart")) {
                        Platform.runLater(this::restart);
                        op_click = true;
                    } else {
                        System.out.println(data);
                    }
                }
            } catch (SocketException sc){
                System.out.println("-------------------------");
                System.out.println("The server shutdown");
                System.exit(0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
